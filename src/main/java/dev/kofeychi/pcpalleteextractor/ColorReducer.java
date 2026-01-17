package dev.kofeychi.pcpalleteextractor;

import java.awt.image.BufferedImage;
import java.util.*;

public class ColorReducer {
    public static BufferedImage reduceColors(BufferedImage originalImage, int maxColors) {
        if (originalImage == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        if (maxColors < 2) {
            throw new IllegalArgumentException("Color count must be at least 2");
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 1. Extract all pixels into a flat array for processing
        int[] rgbArray = originalImage.getRGB(0, 0, width, height, null, 0, width);

        // Filter out fully transparent pixels if necessary, or just process them.
        // For this implementation, we treat pixels as ARGB integers.
        // We will strip the Alpha for the quantization calculation to avoid grey-ing out
        // semi-transparent areas, but preserve structure.

        List<Integer> pixelList = new ArrayList<>(rgbArray.length);
        for (int pixel : rgbArray) {
            // Ignore fully transparent pixels in the palette generation to prevent
            // "transparent" becoming a dominant color that skews the average.
            if ((pixel >>> 24) != 0) {
                pixelList.add(pixel);
            }
        }

        // If the image is empty or transparent, just return a copy
        if (pixelList.isEmpty()) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        // 2. Generate the Palette using Median Cut
        Set<Integer> palette = generatePalette(pixelList, maxColors);

        // 3. Map original pixels to the nearest color in the palette
        // We use a cache because real images have high spatial redundancy (repeated colors).
        Map<Integer, Integer> cache = new HashMap<>();
        int[] reducedRgbArray = new int[rgbArray.length];

        // Convert palette to array for faster iteration during mapping
        int[] paletteArray = palette.stream().mapToInt(Integer::intValue).toArray();

        for (int i = 0; i < rgbArray.length; i++) {
            int originalColor = rgbArray[i];
            int alpha = (originalColor >>> 24) & 0xFF;

            // Pass through full transparency logic
            if (alpha == 0) {
                reducedRgbArray[i] = originalColor;
                continue;
            }

            // Check cache
            // We mask off alpha for the lookup key so we map based on RGB color value only
            int rgbOnly = originalColor & 0x00FFFFFF;

            if (cache.containsKey(rgbOnly)) {
                // Reconstruct with original alpha
                reducedRgbArray[i] = (alpha << 24) | (cache.get(rgbOnly) & 0x00FFFFFF);
            } else {
                int nearest = findNearestColor(rgbOnly, paletteArray);
                cache.put(rgbOnly, nearest);
                reducedRgbArray[i] = (alpha << 24) | (nearest & 0x00FFFFFF);
            }
        }

        // 4. Create output image
        BufferedImage reducedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        reducedImage.setRGB(0, 0, width, height, reducedRgbArray, 0, width);

        return reducedImage;
    }

    /**
     * Implements the Median Cut algorithm to find representative colors.
     */
    private static Set<Integer> generatePalette(List<Integer> pixels, int maxColors) {
        PriorityQueue<ColorBox> queue = new PriorityQueue<>();

        // Initial box containing all pixels
        queue.add(new ColorBox(pixels));

        // Split boxes until we reach the desired count or cannot split further
        while (queue.size() < maxColors) {
            ColorBox largestBox = queue.poll();

            if (largestBox == null || !largestBox.canSplit()) {
                if (largestBox != null) queue.add(largestBox);
                break;
            }

            // Split the largest box into two new boxes
            ColorBox[] split = largestBox.split();
            queue.add(split[0]);
            queue.add(split[1]);
        }

        // Extract the average color from each box
        Set<Integer> palette = new HashSet<>();
        for (ColorBox box : queue) {
            palette.add(box.getAverageColor());
        }
        return palette;
    }

    /**
     * Finds the closest color in the palette using Euclidean distance in RGB space.
     */
    private static int findNearestColor(int target, int[] palette) {
        int r = (target >> 16) & 0xFF;
        int g = (target >> 8) & 0xFF;
        int b = target & 0xFF;

        int minDistance = Integer.MAX_VALUE;
        int closest = palette[0];

        for (int color : palette) {
            int pr = (color >> 16) & 0xFF;
            int pg = (color >> 8) & 0xFF;
            int pb = color & 0xFF;

            // Euclidean distance squared (no need for sqrt for comparison)
            int dr = r - pr;
            int dg = g - pg;
            int db = b - pb;
            int dist = dr * dr + dg * dg + db * db;

            if (dist < minDistance) {
                minDistance = dist;
                closest = color;
                // Optimization: Exact match
                if (dist == 0) return closest;
            }
        }
        return closest;
    }

    /**
     * Internal helper class representing a subset of colors in the 3D RGB space.
     */
    private static class ColorBox implements Comparable<ColorBox> {
        private final List<Integer> pixels;
        private int minR = 255, maxR = 0;
        private int minG = 255, maxG = 0;
        private int minB = 255, maxB = 0;

        public ColorBox(List<Integer> pixels) {
            this.pixels = pixels;
            calculateBounds();
        }

        private void calculateBounds() {
            for (int pixel : pixels) {
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                if (r < minR) minR = r;
                if (r > maxR) maxR = r;
                if (g < minG) minG = g;
                if (g > maxG) maxG = g;
                if (b < minB) minB = b;
                if (b > maxB) maxB = b;
            }
        }

        /**
         * The 'volume' (or largest dimension range) determines priority in the queue.
         * We prioritize splitting boxes with the widest color variance.
         */
        public int getVolume() {
            return (maxR - minR) * (maxG - minG) * (maxB - minB);
        }

        // Alternatively, sorting by pixel count can also yield good results,
        // but volume/variance is standard for Median Cut to preserve detail.
        @Override
        public int compareTo(ColorBox other) {
            return other.getVolume() - this.getVolume();
        }

        public boolean canSplit() {
            return pixels.size() > 1;
        }

        /**
         * Splits this box into two boxes along the longest dimension.
         */
        public ColorBox[] split() {
            int rRange = maxR - minR;
            int gRange = maxG - minG;
            int bRange = maxB - minB;

            // Determine longest dimension
            int channel; // 0=R, 1=G, 2=B
            if (rRange >= gRange && rRange >= bRange) {
                channel = 0;
            } else if (gRange >= rRange && gRange >= bRange) {
                channel = 1;
            } else {
                channel = 2;
            }

            // Sort pixels by that channel
            pixels.sort((p1, p2) -> {
                int val1, val2;
                switch (channel) {
                    case 0: // R
                        val1 = (p1 >> 16) & 0xFF;
                        val2 = (p2 >> 16) & 0xFF;
                        break;
                    case 1: // G
                        val1 = (p1 >> 8) & 0xFF;
                        val2 = (p2 >> 8) & 0xFF;
                        break;
                    default: // B
                        val1 = p1 & 0xFF;
                        val2 = p2 & 0xFF;
                        break;
                }
                return Integer.compare(val1, val2);
            });

            // Split at the median
            int medianIndex = pixels.size() / 2;
            List<Integer> list1 = new ArrayList<>(pixels.subList(0, medianIndex));
            List<Integer> list2 = new ArrayList<>(pixels.subList(medianIndex, pixels.size()));

            return new ColorBox[]{new ColorBox(list1), new ColorBox(list2)};
        }

        /**
         * Calculates the weighted average color of this box.
         */
        public int getAverageColor() {
            long sumR = 0, sumG = 0, sumB = 0;
            for (int pixel : pixels) {
                sumR += (pixel >> 16) & 0xFF;
                sumG += (pixel >> 8) & 0xFF;
                sumB += pixel & 0xFF;
            }
            int count = pixels.size();
            if (count == 0) return 0;

            int avgR = (int) (sumR / count);
            int avgG = (int) (sumG / count);
            int avgB = (int) (sumB / count);

            return (0xFF << 24) | (avgR << 16) | (avgG << 8) | avgB;
        }
    }

}
