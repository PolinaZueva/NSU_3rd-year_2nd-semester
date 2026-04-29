package math;

import model.Point2D;
import model.Point3D;

import java.util.ArrayList;
import java.util.List;

public class FigureBuilder {
    public List<List<Point3D>> buildFigure(List<Point2D> splinePoints, int m) {
        List<List<Point3D>> figure = new ArrayList<>();

        if (splinePoints == null || splinePoints.size() < 2) return figure;
        if (m < 2) m = 2;

        for (int j = 0; j < m; j++) {  //строим m образующих
            double phi = 2.0 * Math.PI * j / m;  //угол поворота в радианах

            double cos = Math.cos(phi);
            double sin = Math.sin(phi);

            List<Point3D> generator = new ArrayList<>();  //образующая

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

    //строит промежуточную точку между двумя соседними образующими по дуге окружности
    public Point3D interpolateOnCircle(Point3D a, Point3D b, double t) {
        double angleA = Math.atan2(a.getY(), a.getX());  //возвращает угол точки на окружности
        double angleB = Math.atan2(b.getY(), b.getX());

        double delta = angleB - angleA;
        if (delta < 0) delta += 2.0 * Math.PI;

        double angle = angleA + delta * t;
        double radius = Math.sqrt(a.getX() * a.getX() + a.getY() * a.getY());  //r = корень(x^2 + y^2)

        double x = radius * Math.cos(angle);
        double y = radius * Math.sin(angle);

        return new Point3D(x, y, a.getZ());
    }
}
