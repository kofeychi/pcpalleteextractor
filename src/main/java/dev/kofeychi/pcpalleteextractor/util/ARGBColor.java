package dev.kofeychi.pcpalleteextractor.util;

import org.jetbrains.annotations.NotNull;

public final class ARGBColor implements Comparable<ARGBColor> {
    private final int color;

    private ARGBColor(int color) {
        this.color = color;
    }

    public static ARGBColor ofTransparent(int color) {
        return new ARGBColor(color);
    }

    public static ARGBColor ofOpaque(int color) {
        return new ARGBColor(0xFF000000 | color);
    }

    public static ARGBColor ofRGB(float r, float g, float b) {
        return ofRGBA(r, g, b, 1f);
    }

    public static ARGBColor ofRGB(int r, int g, int b) {
        return ofRGBA(r, g, b, 255);
    }

    public static ARGBColor ofRGBA(float r, float g, float b, float a) {
        return ofRGBA(
                (int) (r * 255 + 0.5),
                (int) (g * 255 + 0.5),
                (int) (b * 255 + 0.5),
                (int) (a * 255 + 0.5)
        );
    }

    public static ARGBColor ofRGBA(int r, int g, int b, int a) {
        return new ARGBColor(
                ((a & 0xFF) << 24) |
                        ((r & 0xFF) << 16) |
                        ((g & 0xFF) << 8) |
                        (b & 0xFF)
        );
    }

    public static ARGBColor ofHSB(float hue, float saturation, float brightness) {
        return ofOpaque(HSBtoRGB(hue, saturation, brightness));
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    public int getColor() {
        return color;
    }

    public int getAlpha() {
        return color >> 24 & 0xFF;
    }

    public int getRed() {
        return color >> 16 & 0xFF;
    }

    public int getGreen() {
        return color >> 8 & 0xFF;
    }

    public int getBlue() {
        return color & 0xFF;
    }

    /**
     * Returns a brighter color
     *
     * @param factor the higher the value, the brighter the color
     * @return the brighter color
     */
    public ARGBColor brighter(double factor) {
        int r = getRed(), g = getGreen(), b = getBlue();
        int i = (int) (1.0 / (1.0 - (1 / factor)));
        if (r == 0 && g == 0 && b == 0) {
            return ofRGBA(i, i, i, getAlpha());
        }
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;
        return ofRGBA(Math.min((int) (r / (1 / factor)), 255),
                Math.min((int) (g / (1 / factor)), 255),
                Math.min((int) (b / (1 / factor)), 255),
                getAlpha());
    }

    /**
     * Returns a darker color
     *
     * @param factor the higher the value, the darker the color
     * @return the darker color
     */
    public ARGBColor darker(double factor) {
        return ofRGBA(Math.max((int) (getRed() * (1 / factor)), 0),
                Math.max((int) (getGreen() * (1 / factor)), 0),
                Math.max((int) (getBlue() * (1 / factor)), 0),
                getAlpha());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return color == ((ARGBColor) other).color;
    }

    @Override
    public int hashCode() {
        return color;
    }

    @Override
    public String toString() {
        return String.valueOf(color);
    }

    public static int averageHSVColor(int[] colorsRGB) {
        int n = colorsRGB.length;
        if (n == 0) return 0; // или кинь исключение

        float sumS = 0f;
        float sumV = 0f;
        float sumX = 0f;
        float sumY = 0f;

        for (int rgb : colorsRGB) {
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            float[] hsv = java.awt.Color.RGBtoHSB(r, g, b, null);

            float hRad = (float)(2 * Math.PI * hsv[0]);
            sumX += Math.cos(hRad);
            sumY += Math.sin(hRad);

            sumS += hsv[1];
            sumV += hsv[2];
        }

        float avgH = (float)(Math.atan2(sumY, sumX) / (2 * Math.PI));
        if (avgH < 0) avgH += 1.0f;

        float avgS = sumS / n;
        float avgV = sumV / n;

        return ARGBColor.HSBtoRGB(avgH, avgS, avgV) & 0xFFFFFF; // вернём как 0xRRGGBB
    }
    public static int averageHSVColor(Integer[] colorsRGB) {
        int n = colorsRGB.length;
        if (n == 0) return 0;

        float sumS = 0f;
        float sumV = 0f;
        float sumX = 0f;
        float sumY = 0f;

        for (Integer rgbValue : colorsRGB) {
            if (rgbValue == null) continue;

            int rgb = rgbValue & 0xFFFFFF; // отсекаем альфу, если была

            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            float[] hsv = java.awt.Color.RGBtoHSB(r, g, b, null);

            float hRad = (float)(2 * Math.PI * hsv[0]);
            sumX += Math.cos(hRad);
            sumY += Math.sin(hRad);

            sumS += hsv[1];
            sumV += hsv[2];
        }

        float avgH = (float)(Math.atan2(sumY, sumX) / (2 * Math.PI));
        if (avgH < 0) avgH += 1.0f;

        float avgS = sumS / n;
        float avgV = sumV / n;

        return ARGBColor.HSBtoRGB(avgH, avgS, avgV) & 0xFFFFFF;
    }
    public static ARGBColor averageHSV(ARGBColor[] colors) {
        int n = colors.length;
        float sumS = 0f;
        float sumV = 0f;

        // Для усреднения H: переводим в вектор на окружности
        float sumX = 0f;
        float sumY = 0f;

        for (ARGBColor color : colors) {
            float[] hsv = java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

            float hRad = (float)(2 * Math.PI * hsv[0]); // hue в радианах
            sumX += (float) Math.cos(hRad);
            sumY += (float) Math.sin(hRad);

            sumS += hsv[1];
            sumV += hsv[2];
        }

        float avgH = (float)(Math.atan2(sumY, sumX) / (2 * Math.PI));
        if (avgH < 0) avgH += 1.0f; // нормализуем от 0 до 1

        float avgS = sumS / n;
        float avgV = sumV / n;

        return new ARGBColor(ARGBColor.HSBtoRGB(avgH, avgS, avgV));
    }
    public ARGBColor factor(float factor) {
        return ARGBColor.ofRGBA(
                this.getRed()*factor,
                this.getGreen()*factor,
                this.getBlue()*factor,
                this.getAlpha()*factor
        );
    }
    public ARGBColor factorR(float factor) {
        return ARGBColor.ofRGBA(
                this.getRed()*factor,
                this.getGreen(),
                this.getBlue(),
                this.getAlpha()
        );
    }
    public ARGBColor factorG(float factor) {
        return ARGBColor.ofRGBA(
                this.getRed(),
                this.getGreen()*factor,
                this.getBlue(),
                this.getAlpha()
        );
    }
    public ARGBColor factorB(float factor) {
        return ARGBColor.ofRGBA(
                this.getRed(),
                this.getGreen(),
                this.getBlue()*factor,
                this.getAlpha()
        );
    }
    public ARGBColor factorA(float factor) {
        return ARGBColor.ofRGBA(
                this.getRed(),
                this.getGreen(),
                this.getBlue(),
                this.getAlpha()*factor
        );
    }

    @Override
    public int compareTo(@NotNull ARGBColor color) {
        return Integer.compare(this.color, color.color);
    }
}