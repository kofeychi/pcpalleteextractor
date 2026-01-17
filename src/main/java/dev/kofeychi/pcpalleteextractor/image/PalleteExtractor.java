package dev.kofeychi.pcpalleteextractor.image;

import dev.kofeychi.pcpalleteextractor.util.ARGBColor;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector2i;

import java.awt.image.BufferedImage;

public class PalleteExtractor {

    public static ExtractionResult extractPallete(BufferedImage image){
        var out = new ExtractionResult(new ObjectArrayList<>());
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                var c = ARGBColor.ofTransparent(image.getRGB(x, y));
                if(!out.colors().contains(c)){
                    out.colors().add(c);
                }
            }
        }
        return out;
    }
    public static PalletedImage extractPalletedImage(BufferedImage image){
        var out = new PalletedImage(new Object2ObjectRBTreeMap<>(),image);
        var pallete = extractPallete(image);
        for (var c : pallete.colors()){
            out.palletes().put(c,new ObjectArrayList<>());
        }
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                var c = ARGBColor.ofTransparent(image.getRGB(x, y));
                out.palletes().get(c).add(new Vector2i(x,y));
            }
        }
        return out;
    }
}
