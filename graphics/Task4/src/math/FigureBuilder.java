package math;

import model.Point2D;
import model.Point3D;

import java.util.ArrayList;
import java.util.List;

public class FigureBuilder {
    public List<List<Point3D>> buildFigure(List<Point2D> splinePoints, int m) {
        List<List<Point3D>> figure = new ArrayList<>();

        if (splinePoints == null || splinePoints.size() < 2) {
            return figure;
        }

        if (m < 2) {
            m = 2;
        }

        for (int j = 0; j < m; j++) {
            double phi = 2.0 * Math.PI * j / m;

            double cos = Math.cos(phi);
            double sin = Math.sin(phi);

            List<Point3D> generator = new ArrayList<>();

            for (Point2D p : splinePoints) {
                double u = p.getU();
                double v = p.getV();

                double x = v * cos;
                double y = v * sin;
                double z = u;

                generator.add(new Point3D(x, y, z));
            }
            figure.add(generator);
        }
        return figure;
    }
}
