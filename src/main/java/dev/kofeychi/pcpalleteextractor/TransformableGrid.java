package dev.kofeychi.pcpalleteextractor;

import dev.kofeychi.pcpalleteextractor.image.PalletedImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.joml.Matrix3f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class TransformableGrid {
    public Vector2i grid_dimensions = new Vector2i(16,16);
    public int draggedCornerIndex = -1;
    public int HANDLE_SIZE = 12;
    public Point[] corners;

    public Object2ObjectOpenHashMap<Vector2i, Vector2f> cached = new Object2ObjectOpenHashMap<>();


    public TransformableGrid(Rectangle2D rectangle) {
        // Инициализируем углы квадрата по умолчанию
        // < left                    right >
        this.corners = new Point[4];
        var ix = (int)rectangle.getMinX();
        var iy = (int)rectangle.getMinY();
        var mx = (int)rectangle.getMaxX();
        var my = (int)rectangle.getMaxY();
        corners[0] = new Point(ix, iy); // Top-Left
        corners[1] = new Point(mx, iy); // Top-Right
        corners[2] = new Point(mx, my); // Bottom-Right
        corners[3] = new Point(ix, my); // Bottom-Left
    }

    public void mousePressed(MouseEvent e) {
        for (int i = 0; i < corners.length; i++) {
            if (e.getPoint().distance(corners[i]) < HANDLE_SIZE * 2) {
                draggedCornerIndex = i;
                break;
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        draggedCornerIndex = -1;
    }

    public void mouseDragged(MouseEvent e) {
        if (draggedCornerIndex != -1) {
            corners[draggedCornerIndex].setLocation(e.getPoint());
        }
    }

    private Matrix3f calculateHomography() {
        float x0 = corners[0].x, y0 = corners[0].y;
        float x1 = corners[1].x, y1 = corners[1].y;
        float x2 = corners[2].x, y2 = corners[2].y;
        float x3 = corners[3].x, y3 = corners[3].y;

        float dx1 = x1 - x2;
        float dy1 = y1 - y2;
        float dx2 = x3 - x2;
        float dy2 = y3 - y2;
        float sx = x0 - x1 + x2 - x3;
        float sy = y0 - y1 + y2 - y3;

        float g = (sx * dy2 - sy * dx2) / (dx1 * dy2 - dy1 * dx2);
        float h = (dx1 * sy - dy1 * sx) / (dx1 * dy2 - dy1 * dx2);
        float a = x1 - x0 + g * x1;
        float b = x3 - x0 + h * x3;
        float d = y1 - y0 + g * y1;
        float e = y3 - y0 + h * y3;

        return new Matrix3f(
                a, d, g,
                b, e, h,
                x0, y0, 1.0f
        );
    }

    public void paint(Graphics g, PalletedImage current,boolean shouldDrawImage) {
        recalculatePoints();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(shouldDrawImage) {
            for (var pallete : current.palletes().entrySet()) {
                for (Vector2i pos : pallete.getValue()) {
                    fillCell(g2d,calculateHomography(),pos.x,pos.y,new Color(pallete.getKey().getColor()));
                }
            }
        }

        // 1. Рисуем сетку точек
        g2d.setColor(new Color(0, 180, 255));
        for (int i = 0; i < grid_dimensions.x; i++) {
            for (int j = 0; j < grid_dimensions.y; j++) {
                var pos = cached.get(new Vector2i(i, j));

                int dotSize = 4;
                g2d.fill(new Ellipse2D.Float(pos.x - dotSize/2f, pos.y - dotSize/2f, dotSize, dotSize));
            }
        }

        // 2. Рисуем соединительные линии (рамку)
        g2d.setColor(new Color(255, 255, 255, 100));
        for (int i = 0; i < 4; i++) {
            Point p1 = corners[i];
            Point p2 = corners[(i + 1) % 4];
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // 3. Рисуем интерактивные ручки (углы)
        g2d.setColor(Color.WHITE);
        for (Point p : corners) {
            g2d.fillOval(p.x - HANDLE_SIZE/2, p.y - HANDLE_SIZE/2, HANDLE_SIZE, HANDLE_SIZE);
        }
    }

    private void fillCell(Graphics2D g2d, Matrix3f h, int col, int row, Color color) {
        // Вычисляем 4 угла ячейки в пространстве [0, 1]
        float u0 = (float) (col-.5f) / (grid_dimensions.x - 1);
        float v0 = (float) (row-.5f) / (grid_dimensions.y - 1);
        float u1 = (float) ((col-.5f) + 1) / (grid_dimensions.x - 1);
        float v1 = (float) ((row-.5f) + 1) / (grid_dimensions.y - 1);

        float[][] coords = {
                {u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}
        };

        Path2D path = new Path2D.Float();
        for (int i = 0; i < 4; i++) {
            Vector3f p = new Vector3f(coords[i][0], coords[i][1], 1.0f).mul(h);
            float px = p.x / p.z;
            float py = p.y / p.z;
            if (i == 0) path.moveTo(px, py);
            else path.lineTo(px, py);
        }
        path.closePath();

        g2d.setColor(color);
        g2d.fill(path);

        // Опционально: обводка ячейки
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.draw(path);
    }

    public void recalc(BufferedImage img) {
        grid_dimensions.x = img.getWidth();
        grid_dimensions.y = img.getHeight();
        recalculatePoints();
    }

    public void recalculatePoints() {
        var h = calculateHomography();
        cached.clear();
        for (int x = 0; x < grid_dimensions.x; x++) {
            for (int y = 0; y < grid_dimensions.y; y++) {
                // Нормализованные координаты (u, v) от 0 до 1
                float u = (float) x / (grid_dimensions.x - 1);
                float v = (float) y / (grid_dimensions.y - 1);

                // Применяем трансформацию: [x', y', w] = H * [u, v, 1]
                Vector3f projected = new Vector3f(u, v, 1.0f).mul(h);

                // Деление на W для перспективы
                float screenX = projected.x / projected.z;
                float screenY = projected.y / projected.z;

                cached.put(new Vector2i(x,y),new Vector2f(screenX, screenY));
            }
        }
    }

}
