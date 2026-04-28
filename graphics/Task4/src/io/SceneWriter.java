package io;

import model.Point2D;
import model.Scene;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class SceneWriter {
    public void write(Scene scene, File file) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("ICG_WIREFRAME_SCENE 1");

            out.printf(
                    Locale.US,
                    "PARAMS %d %d %d %d%n",
                    scene.getSplineParameters().getK(),
                    scene.getSplineParameters().getN(),
                    scene.getSplineParameters().getM(),
                    scene.getSplineParameters().getM1()
            );

            out.printf(
                    Locale.US,
                    "VIEW %.10f %.10f %.10f %.10f%n",
                    scene.getSceneSettings().getRotX(),
                    scene.getSceneSettings().getRotY(),
                    scene.getSceneSettings().getRotZ(),
                    scene.getSceneSettings().getZn()
            );

            out.println("POINTS " + scene.getControlPoints().size());

            for (Point2D p : scene.getControlPoints()) {
                out.printf(Locale.US, "%.10f %.10f%n", p.getU(), p.getV());
            }
        }
    }
}