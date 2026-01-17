package dev.kofeychi.pcpalleteextractor.image;

import dev.kofeychi.pcpalleteextractor.util.ARGBColor;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector2i;

import java.awt.image.BufferedImage;

public record PalletedImage(Object2ObjectRBTreeMap<ARGBColor, ObjectArrayList<Vector2i>> palletes, BufferedImage image) {
}