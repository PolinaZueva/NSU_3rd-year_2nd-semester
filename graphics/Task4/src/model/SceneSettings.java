package model;

public class SceneSettings {
    private double rotX;
    private double rotY;
    private double rotZ;
    private double Zn;
    private boolean showAxis;

    public SceneSettings(double rotX, double rotY, double rotZ, double Zn, boolean showAxis) {
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.Zn = Zn;
        this.showAxis = showAxis;
    }

    public double getRotX() {
        return rotX;
    }

    public void setRotX(double rotX) {
        this.rotX = rotX;
    }

    public double getRotY() {
        return rotY;
    }

    public void setRotY(double rotY) {
        this.rotY = rotY;
    }

    public double getRotZ() {
        return rotZ;
    }

    public void setRotZ(double rotZ) {
        this.rotZ = rotZ;
    }

    public double getZn() {
        return Zn;
    }

    public void setZn(double Zn) {
        this.Zn = Zn;
    }
}
