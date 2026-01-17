package dev.kofeychi.pcpalleteextractor;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * Профессиональная реализация деформации сетки с возможностью заполнения ячеек.
 * Использует матрицу гомографии для корректного перспективного искажения содержимого ячеек.
 */
public class GridDeformation extends JPanel {

    private final int GRID_SIZE = 15; // Уменьшил для наглядности ячеек
    private final Point[] corners = new Point[4];
    private int draggedCornerIndex = -1;
    private final int HANDLE_SIZE = 12;

    public GridDeformation() {
        setBackground(new Color(20, 20, 30));
        
        // Инициализация углов
        corners[0] = new Point(250, 150); // TL
        corners[1] = new Point(750, 150); // TR
        corners[2] = new Point(850, 650); // BR
        corners[3] = new Point(150, 650); // BL

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (int i = 0; i < corners.length; i++) {
                    if (e.getPoint().distance(corners[i]) < HANDLE_SIZE * 2) {
                        draggedCornerIndex = i;
                        break;
                    }
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                draggedCornerIndex = -1;
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedCornerIndex != -1) {
                    corners[draggedCornerIndex].setLocation(e.getPoint());
                    repaint();
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
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

        float det = dx1 * dy2 - dy1 * dx2;
        float g = (sx * dy2 - sy * dx2) / det;
        float h = (dx1 * sy - dy1 * sx) / det;
        float a = x1 - x0 + g * x1;
        float b = x3 - x0 + h * x3;
        float c = x0;
        float d = y1 - y0 + g * y1;
        float e = y3 - y0 + h * y3;
        float f = y0;

        return new Matrix3f(a, d, g, b, e, h, c, f, 1.0f);
    }

    /**
     * Отрисовывает закрашенную ячейку сетки.
     */
    private void fillCell(Graphics2D g2d, Matrix3f h, int col, int row, Color color) {
        // Вычисляем 4 угла ячейки в пространстве [0, 1]
        float u0 = (float) col / (GRID_SIZE - 1);
        float v0 = (float) row / (GRID_SIZE - 1);
        float u1 = (float) (col + 1) / (GRID_SIZE - 1);
        float v1 = (float) (row + 1) / (GRID_SIZE - 1);

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Matrix3f homography = calculateHomography();

        // 1. Закрашиваем демонстрационные ячейки (например, шахматка или выделение)
        for (int i = 0; i < GRID_SIZE - 1; i++) {
            for (int j = 0; j < GRID_SIZE - 1; j++) {
                if ((i + j) % 2 == 0) {
                    fillCell(g2d, homography, i, j, new Color(0, 150, 255, 40));
                }
            }
        }

        // Выделяем одну конкретную "выбранную" ячейку ярким цветом
        fillCell(g2d, homography, 5, 5, new Color(255, 200, 0, 180));

        // 2. Рисуем точки узлов сетки
        g2d.setColor(new Color(0, 200, 255));
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                float u = (float) i / (GRID_SIZE - 1);
                float v = (float) j / (GRID_SIZE - 1);
                Vector3f projected = new Vector3f(u, v, 1.0f).mul(homography);
                float screenX = projected.x / projected.z;
                float screenY = projected.y / projected.z;
                g2d.fill(new Ellipse2D.Float(screenX - 2, screenY - 2, 4, 4));
            }
        }

        // 3. Рамка и ручки управления
        g2d.setColor(new Color(255, 255, 255, 80));
        for (int i = 0; i < 4; i++) {
            g2d.drawLine(corners[i].x, corners[i].y, corners[(i+1)%4].x, corners[(i+1)%4].y);
            g2d.fillOval(corners[i].x - 6, corners[i].y - 6, 12, 12);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Grid Cell Filling");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new GridDeformation());
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}