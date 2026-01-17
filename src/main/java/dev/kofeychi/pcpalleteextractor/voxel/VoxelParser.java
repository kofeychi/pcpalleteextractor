package dev.kofeychi.pcpalleteextractor.voxel;

import com.google.gson.Gson;
import dev.kofeychi.pcpalleteextractor.image.PalletedImage;
import dev.kofeychi.pcpalleteextractor.util.ARGBColor;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector2i;

import java.util.HashMap;

public class VoxelParser {
    public static HashMap<String, PalletedImage> parse(VoxelModel model){
        var map = new HashMap<String, PalletedImage>();
        for(VoxelModel.VoxelEntry entry : model.voxels) {
            var color = ARGBColor.ofRGB(entry.red, entry.green, entry.blue);
            if(!map.containsKey(""+entry.y)) {
                var d = model.dimension.getFirst();
                map.put(""+entry.y,new PalletedImage(new Object2ObjectRBTreeMap<>(),new Vector2i(d.width,d.depth)));
            }
            var palletes = map.get(""+entry.y).palletes();
            if(!palletes.containsKey(color)) {
                palletes.put(color,new ObjectArrayList<>());
            }
            palletes.get(color).add(new Vector2i(entry.x,entry.z));
        }
        return map;
    }
}
