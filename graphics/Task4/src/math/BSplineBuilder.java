package math;

import model.Point2D;

import java.util.ArrayList;
import java.util.List;

public class BSplineBuilder {
    public List<Point2D> buildSpline(List<Point2D> controlPoints, int n) {
        List<Point2D> splinePoints = new ArrayList<>();

        if (controlPoints == null || controlPoints.size() < 4) return splinePoints;
        if (n < 1) n = 1;

        int k = controlPoints.size();
        for (int i = 0; i <= k - 4; i++) {
            Point2D p0 = controlPoints.get(i);
            Point2D p1 = controlPoints.get(i + 1);
            Point2D p2 = controlPoints.get(i + 2);
            Point2D p3 = controlPoints.get(i + 3);

            int startJ = (i == 0) ? 0 : 1;
            for (int j = startJ; j <= n; j++) {
                double t = j / (double) n;
                splinePoints.add(evaluate(p0, p1, p2, p3, t));
            }
        }
        return splinePoints;
    }

    private Point2D evaluate(Point2D p0, Point2D p1, Point2D p2, Point2D p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;

        double inv6 = 1.0 / 6.0;

        double a1 = (-t3 + 3 * t2 - 3 * t + 1) * inv6;
        double a2 = (3 * t3 - 6 * t2 +4) * inv6;
        double a3 = (-3 * t3 + 3 * t2 + 3 * t + 1) * inv6;
        double a4 = t3 * inv6;

        double u = a1 * p0.getU() + a2 * p1.getU() + a3 * p2.getU() + a4 * p3.getU();
        double v = a1 * p0.getV() + a2 * p1.getV() + a3 * p2.getV() + a4 * p3.getV();

        return new Point2D(u, v);
    }
}
