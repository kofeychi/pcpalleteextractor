package dev.kofeychi.pcpalleteextractor.image;

import dev.kofeychi.pcpalleteextractor.util.ARGBColor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public record ExtractionResult(ObjectArrayList<ARGBColor> colors){}