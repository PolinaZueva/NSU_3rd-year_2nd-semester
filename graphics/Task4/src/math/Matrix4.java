package math;

import model.Point3D;

public class Matrix4 {
    private final double[][] m = new double[4][4];

    public Matrix4() {}

    public static Matrix4 identity() {  //единичная матрица
        Matrix4 matrix = new Matrix4();

        for (int i = 0; i < 4; i++) {
            matrix.m[i][i] = 1.0;
        }
        return matrix;
    }

    public static Matrix4 translation(double dx, double dy, double dz) {  //матрица переноса точки
        Matrix4 matrix = identity();

        matrix.m[0][3] = dx;
        matrix.m[1][3] = dy;
        matrix.m[2][3] = dz;

        return matrix;
    }

    public static Matrix4 scale(double s) {  //матрица масштаба
        Matrix4 matrix = identity();

        matrix.m[0][0] = s;
        matrix.m[1][1] = s;
        matrix.m[2][2] = s;

        return matrix;
    }

    //при повороте вокруг X координата x не меняется, меняются только y и z
    public static Matrix4 rotationX(double angleDegrees) {
        Matrix4 matrix = identity();

        double a = Math.toRadians(angleDegrees);
        double c = Math.cos(a);
        double s = Math.sin(a);

        matrix.m[1][1] = c;
        matrix.m[1][2] = -s;
        matrix.m[2][1] = s;
        matrix.m[2][2] = c;

        return matrix;
    }

    public static Matrix4 rotationY(double angleDegrees) {
        Matrix4 matrix = identity();

        double a = Math.toRadians(angleDegrees);
        double c = Math.cos(a);
        double s = Math.sin(a);

        matrix.m[0][0] = c;
        matrix.m[0][2] = s;
        matrix.m[2][0] = -s;
        matrix.m[2][2] = c;

        return matrix;
    }

    public static Matrix4 rotationZ(double angleDegrees) {
        Matrix4 matrix = identity();

        double a = Math.toRadians(angleDegrees);
        double c = Math.cos(a);
        double s = Math.sin(a);

        matrix.m[0][0] = c;
        matrix.m[0][1] = -s;
        matrix.m[1][0] = s;
        matrix.m[1][1] = c;

        return matrix;
    }

    public Matrix4 multiply(Matrix4 other) {
        Matrix4 result = new Matrix4();

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                double value = 0.0;

                for (int k = 0; k < 4; k++) {
                    value += this.m[row][k] * other.m[k][col];
                }

                result.m[row][col] = value;
            }
        }

        return result;
    }

    public Point3D transform(Point3D p) {
        double[] v = {p.getX(), p.getY(), p.getZ(), 1.0};
        double[] r = new double[4];

        for (int row = 0; row < 4; row++) {
            for (int k = 0; k < 4; k++) {
                r[row] += m[row][k] * v[k];
            }
        }
        return new Point3D(r[0], r[1], r[2]);
    }
}