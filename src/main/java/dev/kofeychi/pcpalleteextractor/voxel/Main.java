package dev.kofeychi.pcpalleteextractor.voxel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kofeychi.pcpalleteextractor.image.PalletedImage;
import dev.kofeychi.pcpalleteextractor.util.TexturePaths;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static dev.kofeychi.pcpalleteextractor.painter.Main.gui;

public class Main {
    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .setLenient()
            .create();

    public static void main(String[] args) throws Exception {
        TexturePaths.prepare();
        gui(selectModel());
    }
    public static HashMap<String, PalletedImage> selectModel() throws Exception {
        FileDialog dialog = new FileDialog((Frame)null, "Select model");
        dialog.setDirectory(Path.of(".").toAbsolutePath().toString());
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        String file = dialog.getFile();
        dialog.dispose();
        return VoxelParser.parse(GSON.fromJson(Files.readString(Path.of(".").resolve(file)),VoxelModel.class));
    }
}
