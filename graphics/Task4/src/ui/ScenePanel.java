package ui;

import math.FigureBuilder;
import math.Matrix4;
import math.ModelTransformBuilder;
import math.ProjectionUtils;
import math.Bresenham;
import model.PixelBuffer;
import model.Point3D;
import model.Scene;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.*;

public class ScenePanel extends JPanel {
    private final Scene scene;
    private final ModelTransformBuilder transformBuilder = new ModelTransformBuilder();
    private final FigureBuilder figureBuilder = new FigureBuilder();

    private int lastMouseX;
    private int lastMouseY;
    private boolean rotating = false;

    private static final double ROTATION_SPEED = 0.5;
    private static final double ZOOM_FACTOR = 1.1;
    private static final double MIN_ZN = 0.5;
    private static final double MAX_ZN = 20.0;

    public ScenePanel(Scene scene) {
        this.scene = scene;

        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 550));
        setMinimumSize(new Dimension(640, 480));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    rotating = true;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                rotating = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!rotating) {
                    return;
                }

                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;

                scene.getSceneSettings().setRotZ(
                        scene.getSceneSettings().getRotZ() + dx * ROTATION_SPEED
                );

                scene.getSceneSettings().setRotY(
                        scene.getSceneSettings().getRotY() - dy * ROTATION_SPEED
                );

                lastMouseX = e.getX();
                lastMouseY = e.getY();

                repaint();
            }
        });

        addMouseWheelListener(this::handleMouseWheel);
    }

    private void handleMouseWheel(MouseWheelEvent e) {
        double zn = scene.getSceneSettings().getZn();

        if (e.getWheelRotation() < 0) {
            zn *= ZOOM_FACTOR;
        } else {
            zn /= ZOOM_FACTOR;
        }

        zn = clamp(zn, MIN_ZN, MAX_ZN);

        scene.getSceneSettings().setZn(zn);

        repaint();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        PixelBuffer pbuffer = new PixelBuffer(getWidth(), getHeight());
        drawFigure(pbuffer);

        g.drawImage(pbuffer.getImage(), 0, 0, null);

        Graphics2D g2 = (Graphics2D) g.create();
        drawMiniAxes(g2);
        g2.dispose();
    }

    private void drawFigure(PixelBuffer pbuffer) {
        List<List<Point3D>> figure = scene.getFigurePoints();

        if (figure == null || figure.isEmpty()) {
            return;
        }

        Matrix4 transform = transformBuilder.buildTransform(figure, scene.getSceneSettings());

        drawGenerators(pbuffer, figure, transform);
        drawRings(pbuffer, figure, transform);
    }

    private void drawGenerators(PixelBuffer pbuffer, List<List<Point3D>> figure, Matrix4 transform) {
        for (List<Point3D> generator : figure) {
            for (int i = 0; i < generator.size() - 1; i++) {
                Point3D p1 = transform.transform(generator.get(i));
                Point3D p2 = transform.transform(generator.get(i + 1));

                drawProjectedLine(pbuffer, p1, p2);
            }
        }
    }

    private void drawRings(PixelBuffer pbuffer, List<List<Point3D>> figure, Matrix4 transform) {
        int m = figure.size();
        int pointsCount = figure.get(0).size();

        int n = scene.getSplineParameters().getN();
        int m1 = scene.getSplineParameters().getM1();

        for (int i = 0; i < pointsCount; i += n) {
            drawOneRing(pbuffer, figure, transform, i, m, m1);
        }

        int lastIndex = pointsCount - 1;

        if ((lastIndex % n) != 0) {
            drawOneRing(pbuffer, figure, transform, lastIndex, m, m1);
        }
    }

    private void drawOneRing(
            PixelBuffer pbuffer,
            List<List<Point3D>> figure,
            Matrix4 transform,
            int pointIndex,
            int m,
            int m1
    ) {
        for (int j = 0; j < m; j++) {
            Point3D a = figure.get(j).get(pointIndex);
            Point3D b = figure.get((j + 1) % m).get(pointIndex);

            drawSmoothRingPart(pbuffer, transform, a, b, m1);
        }
    }

    private void drawSmoothRingPart(
            PixelBuffer pbuffer,
            Matrix4 transform,
            Point3D a,
            Point3D b,
            int m1
    ) {
        Point3D previous = a;

        for (int s = 1; s <= m1; s++) {
            double t = s / (double) m1;

            Point3D current = figureBuilder.interpolateOnCircle(a, b, t);

            Point3D p1 = transform.transform(previous);
            Point3D p2 = transform.transform(current);

            drawProjectedLine(pbuffer, p1, p2);

            previous = current;
        }
    }

    private void drawProjectedLine(PixelBuffer pbuffer, Point3D p1, Point3D p2) {
        ProjectionUtils.ProjectedPoint a = ProjectionUtils.project(
                p1,
                scene.getCamera(),
                scene.getSceneSettings().getZn(),
                getWidth(),
                getHeight()
        );

        ProjectionUtils.ProjectedPoint b = ProjectionUtils.project(
                p2,
                scene.getCamera(),
                scene.getSceneSettings().getZn(),
                getWidth(),
                getHeight()
        );

        if (a == null || b == null) {
            return;
        }

        double averageDepth = (a.getDepth() + b.getDepth()) / 2.0;
        int rgb = ProjectionUtils.depthToColor(averageDepth).getRGB();

        Bresenham.drawLine(
                a.getX(),
                a.getY(),
                b.getX(),
                b.getY(),
                rgb,
                pbuffer
        );
    }

    private void drawMiniAxes(Graphics2D g2) {
        int originX = 70;
        int originY = 75;
        int axisLength = 35;

        Matrix4 rotation = Matrix4.rotationZ(scene.getSceneSettings().getRotZ())
                .multiply(Matrix4.rotationY(scene.getSceneSettings().getRotY()))
                .multiply(Matrix4.rotationX(scene.getSceneSettings().getRotX()));

        Point3D origin = new Point3D(0, 0, 0);

        Point3D xAxis = rotation.transform(new Point3D(1, 0, 0));
        Point3D yAxis = rotation.transform(new Point3D(0, 1, 0));
        Point3D zAxis = rotation.transform(new Point3D(0, 0, 1));

        drawMiniAxis(g2, origin, xAxis, originX, originY, axisLength, Color.RED, "X");
        drawMiniAxis(g2, origin, yAxis, originX, originY, axisLength, Color.GREEN, "Y");
        drawMiniAxis(g2, origin, zAxis, originX, originY, axisLength, Color.BLUE, "Z");
    }

    private void drawMiniAxis(Graphics2D g2, Point3D origin, Point3D axis,
                              int originX, int originY, int axisLength, Color color, String label) {
        Point p1 = miniProject(origin, originX, originY, axisLength);
        Point p2 = miniProject(axis, originX, originY, axisLength);

        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.0f));

        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        g2.drawString(label, p2.x + 4, p2.y - 4);
    }

    private Point miniProject(Point3D p, int originX, int originY, int axisLength) {
        double screenX = p.getY() - 0.5 * p.getX();
        double screenY = p.getZ() - 0.5 * p.getX();

        int x = originX + (int) Math.round(screenX * axisLength);
        int y = originY - (int) Math.round(screenY * axisLength);

        return new Point(x, y);
    }
}