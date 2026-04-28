package model;

import java.util.List;
import java.util.ArrayList;

import math.BSplineBuilder;
import math.FigureBuilder;

public class Scene {
    private List<Point2D> controlPoints;
    private List<Point2D> splinePoints;
    private List<List<Point3D>> figurePoints;

    private SplineParameters splineParameters;
    private SceneSettings sceneSettings;
    private Camera camera;

    public List<Point2D> getControlPoints() {
        return controlPoints;
    }

    public void setControlPoints(List<Point2D> controlPoints) {
        this.controlPoints = controlPoints;
    }

    public List<Point2D> getSplinePoints() {
        return splinePoints;
    }

    public void setSplinePoints(List<Point2D> splinePoints) {
        this.splinePoints = splinePoints;
    }

    public List<List<Point3D>> getFigurePoints() {
        return figurePoints;
    }

    public void setFigurePoints(List<List<Point3D>> figurePoints) {
        this.figurePoints = figurePoints;
    }

    public SplineParameters getSplineParameters() {
        return splineParameters;
    }

    public void setSplineParameters(SplineParameters splineParameters) {
        this.splineParameters = splineParameters;
    }

    public SceneSettings getSceneSettings() {
        return sceneSettings;
    }

    public void setSceneSettings(SceneSettings sceneSettings) {
        this.sceneSettings = sceneSettings;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public static List<Point2D> createDefaultControlPoints() {
        List<Point2D> points = new ArrayList<>();

        points.add(new Point2D(-3.0, 1.5));
        points.add(new Point2D(-2.2, 1.7));
        points.add(new Point2D(-1.6, 0.4));
        points.add(new Point2D(-0.3, 0.4));
        points.add(new Point2D(0.2, 1.4));
        points.add(new Point2D(1.8, 1.3));
        points.add(new Point2D(2.2, 0.9));
        points.add(new Point2D(2.6, 2.5));

        return points;
    }

    public void rebuild() {
        BSplineBuilder splineBuilder = new BSplineBuilder();
        FigureBuilder figureBuilder = new FigureBuilder();

        this.splinePoints = splineBuilder.buildSpline(
                controlPoints,
                splineParameters.getN()
        );

        this.figurePoints = figureBuilder.buildFigure(
                splinePoints,
                splineParameters.getM()
        );
    }
}
