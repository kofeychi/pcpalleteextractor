package dev.kofeychi.pcpalleteextractor.painter;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kofeychi.pcpalleteextractor.AutoPaint;
import dev.kofeychi.pcpalleteextractor.TransformableGrid;
import dev.kofeychi.pcpalleteextractor.image.PalletedImage;
import dev.kofeychi.pcpalleteextractor.model.ModelFile;
import dev.kofeychi.pcpalleteextractor.model.ParsedModelFile;
import dev.kofeychi.pcpalleteextractor.util.*;
import dev.kofeychi.pcpalleteextractor.image.PalleteExtractor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.joml.Vector2i;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Predicate;

public class Main {
    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .setLenient()
            .create();
    public static Predicate<AutoPaint.PaintingData> painting;

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
        try {
            GlobalScreen.registerNativeHook();
            System.out.println("Native Hook Registered");
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
                    if(nativeEvent.getKeyCode() == 57420) {
                        System.exit(0);
                        try {
                            GlobalScreen.unregisterNativeHook();
                        } catch (NativeHookException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

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
            final boolean[] draw = {false,true,true};
            overlay.getDrawPanel().addMouseListener(m);
            overlay.getDrawPanel().addMouseMotionListener(m);
            var side = new JComboBox<>(palletes.keySet().stream().sorted((a,b)->{
                try {
                    return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                } catch (NumberFormatException e) {
                    return a.compareTo(b);
                }
            }).toArray(String[]::new));
            var dots = new JToggleButton("dots");
            dots.addActionListener(e -> {
                draw[1] = !draw[1];
                grid.dots = !draw[1];
                overlay.getFrame().repaint();
            });
            side.addActionListener(e -> {
                grid.recalc(palletes.get((String)side.getSelectedItem()).size());
                overlay.getFrame().repaint();
            });
            var drawTexture = new JToggleButton("Toggle texture");
            drawTexture.addActionListener(e -> {
                draw[0] = !draw[0];
                overlay.getFrame().repaint();
            });
            overlay.getDrawPanel().on = g -> grid.paint(g,palletes.get((String) side.getSelectedItem()),draw[0]);
            var fram = new JFrame();
            fram.setLayout(null);
            fram.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fram.setBounds(0, 0, 256, 256*2+64);
            var autopaint = new JButton("Autopaint");
            var place = new JButton("Place");
            var cells = new JToggleButton("cells");
            cells.addActionListener(e -> {
                grid.cells = draw[2];
                draw[2] = !draw[2];
                overlay.getFrame().repaint();
            });
            place.addActionListener(e -> {
                overlay.setVisible(false);
                fram.setVisible(false);
                AutoPaint.autoPlace(palletes,(String) side.getSelectedItem(),grid,painting != null ? painting : a -> false);
                overlay.setVisible(true);
                fram.setVisible(true);
            });
            autopaint.addActionListener(e -> {
                overlay.setVisible(false);
                fram.setVisible(false);
                try {
                    AutoPaint.autoPaint(palletes,(String) side.getSelectedItem(),grid);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                overlay.setVisible(true);
                fram.setVisible(true);
            });
            autopaint.setBounds(0, 0, 256, 64);
            side.setBounds(0, 64, 256, 64);
            drawTexture.setBounds(0, 128, 256, 64);
            dots.setBounds(0, 128+64, 256, 64);
            place.setBounds(0, 256, 256, 64);
            cells.setBounds(0, 256+64, 256, 64);
            fram.add(place);
            fram.add(dots);
            fram.add(autopaint);
            fram.add(drawTexture);
            fram.add(side);
            fram.add(cells);
            fram.setVisible(true);
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException e) {
                e.printStackTrace();
            }
        }));
    }
}
