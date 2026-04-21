package model;

public class StampParams {
    private int sides = 5;  //форма, число вершин
    private int radius = 30;
    private int rotationDeg = 90;
    private boolean isStar = false;
    public double k = 0.4;  //коэфициент внутреннего радиуса

    public int getSides() {
        return sides;
    }

    public void setSides(int sides) {
        this.sides = sides;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRotationDeg() {
        return rotationDeg;
    }

    public void setRotationDeg(int rotationDeg) {
        this.rotationDeg = rotationDeg;
    }

    public boolean isStar() {
        return isStar;
    }

    public void setStar(boolean star) {
        isStar = star;
    }
}
