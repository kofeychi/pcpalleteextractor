package dev.kofeychi.pcpalleteextractor.util;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ScreenOverlay {

    @Getter
    private final JFrame frame;
    @Getter
    private final DrawPanel drawPanel;

    public ScreenOverlay() {
        frame = new JFrame("ScreenOverlay");
        
        // 1. Делаем окно полностью прозрачным и без рамок
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setAlwaysOnTop(true);
        
        // 2. Растягиваем на весь экран
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // 3. Создаем панель для рисования
        drawPanel = new DrawPanel();
        frame.add(drawPanel);

        // Позволяем окну игнорировать фокус, чтобы оно не мешало работе
        frame.setFocusableWindowState(false);
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    /**
     * Добавить фигуру для отрисовки.
     */
    public void addShape(Shape shape, Color color, float thickness) {
        drawPanel.addDrawingItem(new DrawingItem(shape, color, thickness));
        drawPanel.repaint();
    }

    public void clear() {
        drawPanel.clear();
        drawPanel.repaint();
    }

    /**
     * Внутренняя панель с переопределенным paintComponent.
     */
    public static class DrawPanel extends JPanel {
        private final ArrayList<DrawingItem> items = new ArrayList<>();
        public Consumer<Graphics> on;

        public DrawPanel() {
            setOpaque(false);
            setLayout(null);
        }

        public void addDrawingItem(DrawingItem item) {
            items.add(item);
        }

        public void clear() {
            items.clear();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(on != null) {
                on.accept(g);
            }
            Graphics2D g2d = (Graphics2D) g;

            // Включаем сглаживание для профессионального вида
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            for (DrawingItem item : items) {
                g2d.setColor(item.color);
                g2d.setStroke(new BasicStroke(item.thickness));
                g2d.draw(item.shape);
            }
        }
    }

    private record DrawingItem(Shape shape, Color color, float thickness) {}

    /**
     * Точка входа для демонстрации работы.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ScreenOverlay overlay = new ScreenOverlay();
            overlay.setVisible(true);

            // Пример: Рисуем красный круг в центре
            overlay.addShape(new Ellipse2D.Double(500, 300, 200, 200), Color.RED, 5.0f);
            
            // Пример: Рисуем синюю линию
            overlay.addShape(new Line2D.Double(100, 100, 800, 600), Color.BLUE, 3.0f);

            System.out.println("Overlay active. Drawing on screen...");
        });
    }
}