package ui;

import model.Point2D;
import model.Scene;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class SplineEditorPanel extends JPanel {
    private final Scene scene;
    private final Runnable onPointsChanged;

    private static final int GRID_STEP = 40;
    private static final int POINT_RADIUS = 7;
    private static final int HIT_RADIUS = 12;

    private int selectedPointIndex = -1;

    public SplineEditorPanel(Scene scene, Runnable onPointsChanged) {
        this.scene = scene;
        this.onPointsChanged = onPointsChanged;

        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(700, 500));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectedPointIndex = -1;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawAxes(g2);
        drawControlPolyline(g2);
        drawSpline(g2);
        drawControlPoints(g2);

        g2.dispose();
    }

    private void drawGrid(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(18, 18, 18));

        for (int x = getCenterX(); x < w; x += GRID_STEP) {
            g2.drawLine(x, 0, x, h);
        }
        for (int x = getCenterX(); x >= 0; x -= GRID_STEP) {
            g2.drawLine(x, 0, x, h);
        }

        for (int y = getCenterY(); y < h; y += GRID_STEP) {
            g2.drawLine(0, y, w, y);
        }
        for (int y = getCenterY(); y >= 0; y -= GRID_STEP) {
            g2.drawLine(0, y, w, y);
        }
    }

    private void drawAxes(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        int cx = getCenterX();
        int cy = getCenterY();

        g2.setColor(new Color(170, 170, 170));
        g2.setStroke(new BasicStroke(1.2f));

        g2.drawLine(0, cy, w, cy);
        g2.drawLine(cx, 0, cx, h);

        drawTicks(g2);

        g2.setColor(new Color(210, 210, 210));
        g2.drawString("U", w - 20, cy - 8);
        g2.drawString("V", cx + 8, 18);
    }

    private void drawTicks(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        int cx = getCenterX();
        int cy = getCenterY();

        g2.setColor(new Color(190, 190, 190));

        for (int x = cx; x < w; x += GRID_STEP) {
            g2.drawLine(x, cy - 4, x, cy + 4);
        }
        for (int x = cx; x >= 0; x -= GRID_STEP) {
            g2.drawLine(x, cy - 4, x, cy + 4);
        }

        for (int y = cy; y < h; y += GRID_STEP) {
            g2.drawLine(cx - 4, y, cx + 4, y);
        }
        for (int y = cy; y >= 0; y -= GRID_STEP) {
            g2.drawLine(cx - 4, y, cx + 4, y);
        }
    }

    private void drawControlPolyline(Graphics2D g2) {
        List<Point2D> points = scene.getControlPoints();

        if (points == null || points.size() < 2) {
            return;
        }

        g2.setColor(new Color(95, 25, 180));
        g2.setStroke(new BasicStroke(1.3f));

        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = worldToScreen(points.get(i));
            Point p2 = worldToScreen(points.get(i + 1));
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void drawSpline(Graphics2D g2) {
        List<Point2D> points = scene.getSplinePoints();

        if (points == null || points.size() < 2) {
            return;
        }

        g2.setColor(new Color(230, 210, 40));
        g2.setStroke(new BasicStroke(2.0f));

        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = worldToScreen(points.get(i));
            Point p2 = worldToScreen(points.get(i + 1));

            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void drawControlPoints(Graphics2D g2) {
        List<Point2D> points = scene.getControlPoints();

        if (points == null) {
            return;
        }

        for (int i = 0; i < points.size(); i++) {
            Point2D point = points.get(i);
            Point screen = worldToScreen(point);

            boolean isLastPoint = (i == points.size() - 1);

            if (i == selectedPointIndex) {
                g2.setColor(new Color(0, 220, 0));
            } else if (isLastPoint) {
                g2.setColor(new Color(0, 220, 0));
            } else {
                g2.setColor(new Color(95, 25, 180));
            }

            g2.drawOval(
                    screen.x - POINT_RADIUS,
                    screen.y - POINT_RADIUS,
                    POINT_RADIUS * 2,
                    POINT_RADIUS * 2
            );

            g2.setColor(new Color(30, 30, 30));
            g2.fillOval(
                    screen.x - 2,
                    screen.y - 2,
                    4,
                    4
            );

            g2.setColor(new Color(210, 210, 210));
            g2.drawString(
                    "P" + i,
                    screen.x + POINT_RADIUS + 4,
                    screen.y - POINT_RADIUS - 4
            );
        }
    }

    private void handleMousePressed(MouseEvent e) {
        int index = findPointAt(e.getX(), e.getY());

        if (SwingUtilities.isLeftMouseButton(e)) {
            selectedPointIndex = index;
        }

        if (SwingUtilities.isRightMouseButton(e)) {
            if (index >= 0) {
                removePoint(index);
            } else {
                addPoint(e.getX(), e.getY());
            }

            rebuildModel();

            if (onPointsChanged != null) {
                onPointsChanged.run();
            }
        }

        repaint();
    }

    private void handleMouseDragged(MouseEvent e) {
        if (selectedPointIndex < 0) {
            return;
        }

        Point2D newPoint = screenToWorldClamped(e.getX(), e.getY());
        scene.getControlPoints().get(selectedPointIndex).setU(newPoint.getU());
        scene.getControlPoints().get(selectedPointIndex).setV(newPoint.getV());

        rebuildModel();
        if (onPointsChanged != null) {
            onPointsChanged.run();
        }
        repaint();
    }

    private int findPointAt(int mouseX, int mouseY) {
        List<Point2D> points = scene.getControlPoints();

        for (int i = 0; i < points.size(); i++) {
            Point screen = worldToScreen(points.get(i));

            double dx = mouseX - screen.x;
            double dy = mouseY - screen.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= HIT_RADIUS) {
                return i;
            }
        }

        return -1;
    }

    private void addPoint(int screenX, int screenY) {
        Point2D point = screenToWorldClamped(screenX, screenY);
        scene.getControlPoints().add(point);
    }

    private void removePoint(int index) {
        if (scene.getControlPoints().size() <= 4) {
            JOptionPane.showMessageDialog(
                    this,
                    "Нельзя удалить точку: для B-сплайна нужно минимум 4 опорные точки.",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        scene.getControlPoints().remove(index);
    }

    private Point worldToScreen(Point2D point) {
        int x = getCenterX() + (int) Math.round(point.getU() * GRID_STEP);
        int y = getCenterY() - (int) Math.round(point.getV() * GRID_STEP);

        return new Point(x, y);
    }

    private Point2D screenToWorld(int x, int y) {
        double u = (x - getCenterX()) / (double) GRID_STEP;
        double v = (getCenterY() - y) / (double) GRID_STEP;

        return new Point2D(u, v);
    }

    private Point2D screenToWorldClamped(int x, int y) {
        int margin = POINT_RADIUS + 2;

        int clampedX = clamp(x, margin, getWidth() - margin);
        int clampedY = clamp(y, margin, getHeight() - margin);

        return screenToWorld(clampedX, clampedY);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }


    private int getCenterX() {
        return getWidth() / 2;
    }

    private int getCenterY() {
        return getHeight() / 2;
    }

    public void rebuildModel() {
        scene.rebuild();
    }
}