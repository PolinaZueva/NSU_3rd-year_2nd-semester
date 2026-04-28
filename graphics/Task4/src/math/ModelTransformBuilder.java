package math;

import model.Point3D;
import model.SceneSettings;

import java.util.List;

public class ModelTransformBuilder {

    public Matrix4 buildTransform(List<List<Point3D>> figure, SceneSettings settings) {
        Bounds bounds = calculateBounds(figure);

        double centerX = (bounds.minX + bounds.maxX) / 2.0;
        double centerY = (bounds.minY + bounds.maxY) / 2.0;
        double centerZ = (bounds.minZ + bounds.maxZ) / 2.0;

        double sizeX = bounds.maxX - bounds.minX;
        double sizeY = bounds.maxY - bounds.minY;
        double sizeZ = bounds.maxZ - bounds.minZ;

        double maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));

        if (maxSize == 0.0) {
            maxSize = 1.0;
        }

        double scale = 2.0 / maxSize;

        Matrix4 moveToCenter = Matrix4.translation(-centerX, -centerY, -centerZ);
        Matrix4 normalize = Matrix4.scale(scale);

        Matrix4 rotateX = Matrix4.rotationX(settings.getRotX());
        Matrix4 rotateY = Matrix4.rotationY(settings.getRotY());
        Matrix4 rotateZ = Matrix4.rotationZ(settings.getRotZ());

        return rotateZ
                .multiply(rotateY)
                .multiply(rotateX)
                .multiply(normalize)
                .multiply(moveToCenter);
    }

    private Bounds calculateBounds(List<List<Point3D>> figure) {
        Bounds bounds = new Bounds();

        for (List<Point3D> generator : figure) {
            for (Point3D p : generator) {
                bounds.include(p);
            }
        }

        return bounds;
    }

    private static class Bounds {
        private double minX = Double.POSITIVE_INFINITY;
        private double minY = Double.POSITIVE_INFINITY;
        private double minZ = Double.POSITIVE_INFINITY;

        private double maxX = Double.NEGATIVE_INFINITY;
        private double maxY = Double.NEGATIVE_INFINITY;
        private double maxZ = Double.NEGATIVE_INFINITY;

        private void include(Point3D p) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());

            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }
    }
}