package dev.kofeychi.pcpalleteextractor.voxel;

import java.util.ArrayList;

public class VoxelModel {

    public ArrayList<VoxelDimensions> dimension = new ArrayList<>();
    public ArrayList<VoxelEntry> voxels = new ArrayList<>();

    public static class VoxelEntry {
        public String id;
        public int x;
        public int y;
        public int z;
        public int red;
        public int green;
        public int blue;
    }

    public static class VoxelDimensions {
        public int width;
        public int height;
        public int depth;
    }
}
