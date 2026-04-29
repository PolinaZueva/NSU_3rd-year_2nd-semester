package math;

import model.Camera;
import model.Point3D;

import java.awt.*;

public class ProjectionUtils {
    public static ProjectedPoint project(Point3D p, Camera camera, double zn, int Sh, int Sv) {
        double cameraX = camera.getPCam().getX();
        double depth = p.getX() - cameraX;  //глубина точки

        if (depth <= 0.001) return null;

        double projectedX = zn * p.getY() / depth;  //горизонтальная координата на экране = Y
        double projectedY = zn * p.getZ() / depth;  //вертикальная координата на экране = Z

        int scale = (int) Math.round(Math.min(Sh, Sv) * 0.9);

        int screenX = Sh / 2 + (int) Math.round(projectedX * scale);
        int screenY = Sv / 2 - (int) Math.round(projectedY * scale);

        return new ProjectedPoint(screenX, screenY, depth);
    }

    public static Color depthToColor(double depth) {
        double minDepth = 8.0;
        double maxDepth = 12.0;

        double t = (depth - minDepth) / (maxDepth - minDepth);
        t = Math.max(0.0, Math.min(1.0, t));

        int value = (int) Math.round(30 + 190 * t);

        return new Color(value, value, value);
    }

    public static class ProjectedPoint {  //контейнер для результата проекции
        private final int x;
        private final int y;
        private final double depth;

        public ProjectedPoint(int x, int y, double depth) {
            this.x = x;
            this.y = y;
            this.depth = depth;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public double getDepth() {
            return depth;
        }
    }
}