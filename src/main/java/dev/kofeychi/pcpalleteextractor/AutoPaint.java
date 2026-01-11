package dev.kofeychi.pcpalleteextractor;

import dev.kofeychi.pcpalleteextractor.image.PalletedImage;
import org.joml.Vector2f;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class AutoPaint {
    public static void click(Vector2f pos) throws Exception {
        Robot bot = new Robot();
        bot.mouseMove((int) pos.x, (int) pos.y);
        Thread.sleep(10);
        bot.mouseMove((int) pos.x-5, (int) pos.y);
        Thread.sleep(10);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(10);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(10);
    }
    public static void type(String str,Vector2f pos) throws Exception {
        Robot bot = new Robot();
        bot.mouseMove((int) pos.x, (int) pos.y);
        Thread.sleep(10);
        bot.mouseMove((int) pos.x-5, (int) pos.y);
        Thread.sleep(10);
        for (int i = 0; i < str.length(); i++) {
            bot.keyPress(KeyEvent.VK_BACK_SPACE);
            Thread.sleep(10);
            bot.keyRelease(KeyEvent.VK_BACK_SPACE);
            Thread.sleep(10);
        }
        for (char ch : str.toCharArray()) {
            bot.keyPress(KeyEvent.getExtendedKeyCodeForChar(ch));
            Thread.sleep(10);
            bot.keyRelease(KeyEvent.getExtendedKeyCodeForChar(ch));
            Thread.sleep(10);
        }
    }
    public static void autoPaint(HashMap<String, PalletedImage> palletes,String side, TransformableGrid grid) {
        for (var pallete : palletes.get((String) side).palletes().keySet()) {
            for (int i = 0; i < 3; i++) {
                try {
                    click(new Vector2f(102,703));
                    Thread.sleep(10);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            try {
                type(String.format("#%06X", (0xFFFFFF & pallete.getColor())),new Vector2f(102,703));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            for (var pos : palletes.get((String) side).palletes().get(pallete)) {
                try {
                    click(grid.cached.get(pos));
                    Thread.sleep(100);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
    }
}
