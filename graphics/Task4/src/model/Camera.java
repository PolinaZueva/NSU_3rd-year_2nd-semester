package model;

public class Camera {
    private Point3D PCam;  //точка положения фокуса камеры
    private Point3D PView;  //точка, на которую смотрит камера
    private Point3D VUp;  //вектор направления вверх

    public Camera() {
        this.PCam = new Point3D(-10.0, 0.0, 0.0);
        this.PView = new Point3D(10.0, 0.0, 0.0);
        this.VUp = new Point3D(0.0, 1.0, 0.0);
    }

    public Point3D getPCam() {
        return PCam;
    }

    public void setPCam(Point3D PCam) {
        this.PCam = PCam;
    }

    public Point3D getPView() {
        return PView;
    }

    public void setPView(Point3D PView) {
        this.PView = PView;
    }

    public Point3D getVUp() {
        return VUp;
    }

    public void setVUp(Point3D VUp) {
        this.VUp = VUp;
    }
}
