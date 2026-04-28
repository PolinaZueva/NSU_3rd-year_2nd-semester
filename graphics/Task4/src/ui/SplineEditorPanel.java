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

    private static final int POINT_RADIUS = 7;
    private static final int HIT_RADIUS = 12;

    private static final double MIN_SCALE = 10.0;
    private static final double MAX_SCALE = 250.0;
    private static final double ZOOM_FACTOR = 1.15;

    private int selectedPointIndex = -1;

    private double scale = 40.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    private boolean panning = false;
    private int lastPanX;
    private int lastPanY;

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
                panning = false;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
        });

        addMouseWheelListener(this::handleMouseWheel);
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

    private void handleMousePressed(MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            panning = true;
            lastPanX = e.getX();
            lastPanY = e.getY();
            return;
        }

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
        if (panning) {
            int dx = e.getX() - lastPanX;
            int dy = e.getY() - lastPanY;

            offsetX += dx;
            offsetY += dy;

            lastPanX = e.getX();
            lastPanY = e.getY();

            repaint();
            return;
        }

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

    private void handleMouseWheel(MouseWheelEvent e) {
        double oldScale = scale;

        if (e.getWheelRotation() < 0) {
            scale *= ZOOM_FACTOR;
        } else {
            scale /= ZOOM_FACTOR;
        }

        scale = clampDouble(scale, MIN_SCALE, MAX_SCALE);

        double factor = scale / oldScale;

        int mouseX = e.getX();
        int mouseY = e.getY();

        offsetX = mouseX - getWidth() / 2.0 - factor * (mouseX - getWidth() / 2.0 - offsetX);
        offsetY = mouseY - getHeight() / 2.0 - factor * (mouseY - getHeight() / 2.0 - offsetY);

        repaint();
    }

    public void normalizeView() {
        List<Point2D> points = scene.getControlPoints();

        if (points == null || points.isEmpty()) {
            return;
        }

        double minU = Double.POSITIVE_INFINITY;
        double maxU = Double.NEGATIVE_INFINITY;
        double minV = Double.POSITIVE_INFINITY;
        double maxV = Double.NEGATIVE_INFINITY;

        for (Point2D p : points) {
            minU = Math.min(minU, p.getU());
            maxU = Math.max(maxU, p.getU());
            minV = Math.min(minV, p.getV());
            maxV = Math.max(maxV, p.getV());
        }

        double widthWorld = maxU - minU;
        double heightWorld = maxV - minV;

        if (widthWorld < 0.001) {
            widthWorld = 1.0;
        }

        if (heightWorld < 0.001) {
            heightWorld = 1.0;
        }

        int margin = 60;
        int availableWidth = Math.max(1, getWidth() - 2 * margin);
        int availableHeight = Math.max(1, getHeight() - 2 * margin);

        double scaleX = availableWidth / widthWorld;
        double scaleY = availableHeight / heightWorld;

        scale = Math.min(scaleX, scaleY);
        scale = clampDouble(scale, MIN_SCALE, MAX_SCALE);

        double centerU = (minU + maxU) / 2.0;
        double centerV = (minV + maxV) / 2.0;

        offsetX = -centerU * scale;
        offsetY = centerV * scale;

        repaint();
    }

    private void drawGrid(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        double gridStepWorld = chooseGridStepWorld();
        Rectangle2DWorld visible = getVisibleWorldBounds();

        int startU = (int) Math.floor(visible.minU / gridStepWorld) - 1;
        int endU = (int) Math.ceil(visible.maxU / gridStepWorld) + 1;

        int startV = (int) Math.floor(visible.minV / gridStepWorld) - 1;
        int endV = (int) Math.ceil(visible.maxV / gridStepWorld) + 1;

        g2.setStroke(new BasicStroke(1.0f));
        g2.setColor(new Color(18, 18, 18));

        for (int i = startU; i <= endU; i++) {
            double u = i * gridStepWorld;
            int x = worldToScreen(new Point2D(u, 0)).x;
            g2.drawLine(x, 0, x, h);
        }

        for (int i = startV; i <= endV; i++) {
            double v = i * gridStepWorld;
            int y = worldToScreen(new Point2D(0, v)).y;
            g2.drawLine(0, y, w, y);
        }
    }

    private void drawAxes(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        Point origin = worldToScreen(new Point2D(0, 0));

        g2.setColor(new Color(170, 170, 170));
        g2.setStroke(new BasicStroke(1.2f));

        g2.drawLine(0, origin.y, w, origin.y);
        g2.drawLine(origin.x, 0, origin.x, h);

        g2.setColor(new Color(210, 210, 210));
        g2.drawString("U", w - 20, origin.y - 8);
        g2.drawString("V", origin.x + 8, 18);
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
        int x = (int) Math.round(getWidth() / 2.0 + offsetX + point.getU() * scale);
        int y = (int) Math.round(getHeight() / 2.0 + offsetY - point.getV() * scale);

        return new Point(x, y);
    }

    private Point2D screenToWorld(int x, int y) {
        double u = (x - getWidth() / 2.0 - offsetX) / scale;
        double v = -(y - getHeight() / 2.0 - offsetY) / scale;

        return new Point2D(u, v);
    }

    private double chooseGridStepWorld() {
        double targetPixels = 45.0;
        double rawStep = targetPixels / scale;

        double power = Math.pow(10, Math.floor(Math.log10(rawStep)));
        double normalized = rawStep / power;

        if (normalized < 2) {
            return power;
        }

        if (normalized < 5) {
            return 2 * power;
        }

        return 5 * power;
    }

    private Rectangle2DWorld getVisibleWorldBounds() {
        Point2D topLeft = screenToWorld(0, 0);
        Point2D bottomRight = screenToWorld(getWidth(), getHeight());

        double minU = Math.min(topLeft.getU(), bottomRight.getU());
        double maxU = Math.max(topLeft.getU(), bottomRight.getU());
        double minV = Math.min(topLeft.getV(), bottomRight.getV());
        double maxV = Math.max(topLeft.getV(), bottomRight.getV());

        return new Rectangle2DWorld(minU, maxU, minV, maxV);
    }

    private double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public void rebuildModel() {
        scene.rebuild();
    }

    private static class Rectangle2DWorld {
        private final double minU;
        private final double maxU;
        private final double minV;
        private final double maxV;

        public Rectangle2DWorld(double minU, double maxU, double minV, double maxV) {
            this.minU = minU;
            this.maxU = maxU;
            this.minV = minV;
            this.maxV = maxV;
        }
    }

    private Point2D screenToWorldClamped(int x, int y) {
        int margin = POINT_RADIUS + 2;

        int clampedX = clampInt(x, margin, getWidth() - margin);
        int clampedY = clampInt(y, margin, getHeight() - margin);

        return screenToWorld(clampedX, clampedY);
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}