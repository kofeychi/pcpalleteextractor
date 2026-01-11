package dev.kofeychi.pcpalleteextractor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kofeychi.pcpalleteextractor.image.PalletedImage;
import dev.kofeychi.pcpalleteextractor.model.ModelFile;
import dev.kofeychi.pcpalleteextractor.model.ParsedModelFile;
import dev.kofeychi.pcpalleteextractor.util.*;
import dev.kofeychi.pcpalleteextractor.image.PalleteExtractor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector2f;
import org.joml.Vector2i;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class Main {
    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .setLenient()
            .create();

    public static void main(String[] args) throws Exception {
        TexturePaths.prepare();
        var model = selectModel();
        var images = new Object2ObjectOpenHashMap<String,BufferedImage>();
        for (var tex : model.textures.values()) {
            images.put(tex.path,ImageIO.read(TexturePaths.TEXTURE.resolve(tex.path.split("/")[1] + ".png").toFile()));
        }
        var palletes = new HashMap<String, PalletedImage>();
        for (var image : images.entrySet()) {
            palletes.put(image.getKey(),PalleteExtractor.extractPalletedImage(image.getValue()));
        }

        gui(palletes);
    }
    public static ParsedModelFile selectModel() throws Exception {
        FileDialog dialog = new FileDialog((Frame)null, "Select model");
        dialog.setDirectory(TexturePaths.MODEL_ROOT.toAbsolutePath().toString());
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        String file = dialog.getFile();
        dialog.dispose();
        var model = parseModelFile(TexturePaths.MODEL.resolve(file));
        if(!model.parent.path.contains("cube")) {
            return selectModel();
        }
        return model;
    }
    public static ParsedModelFile parseModelFile(Path path) throws Exception {
        ModelFile model = GSON.fromJson(Files.readString(path), ModelFile.class);
        ParsedModelFile parsedModel = new ParsedModelFile();
        parsedModel.parent = Id.parse(model.parent);
        parsedModel.textures = new HashMap<>();
        for (var entry : model.textures.entrySet()) {
            parsedModel.textures.put(entry.getKey(), Id.parse(entry.getValue()));
        }
        return parsedModel;
    }

    public static void gui(HashMap<String,PalletedImage> palletes) {
        SwingUtilities.invokeLater(() -> {
            var overlay = new ScreenOverlay();
            overlay.setVisible(true);
            var bounds = overlay.getFrame().getBounds();
            var center = new Vector2i(bounds.width / 2, bounds.height / 2);
            var size = 256;
            TransformableGrid grid = new TransformableGrid(new Rectangle2D.Float(center.x-size, center.y-size, size*2, size*2));
            var m = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    grid.mousePressed(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    grid.mouseReleased(e);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    grid.mouseDragged(e);
                    overlay.getFrame().repaint();
                }
            };
            overlay.getDrawPanel().addMouseListener(m);
            overlay.getDrawPanel().addMouseMotionListener(m);
            overlay.getDrawPanel().on = grid::paint;
            var fram = new JFrame();
            fram.setLayout(null);
            fram.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fram.setBounds(0, 0, 256, 256);
            var autopaint = new JButton("Autopaint");
            var side = new JComboBox<>(palletes.keySet().toArray(new String[0]));
            autopaint.addActionListener(e -> {
                overlay.setVisible(false);
                fram.setVisible(false);
                AutoPaint.autoPaint(palletes,(String) side.getSelectedItem(),grid);
                overlay.setVisible(true);
                fram.setVisible(true);
            });
            autopaint.setBounds(0, 0, 256, 64);
            side.setBounds(0, 64, 256, 64);
            fram.add(autopaint);
            fram.add(side);
            fram.setVisible(true);
        });
    }
}
