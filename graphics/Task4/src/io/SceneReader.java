package io;

import model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SceneReader {
    public Scene read(File file) throws IOException, SceneFormatException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = readRequiredLine(reader, "Ожидался заголовок файла");

            if (!header.equals("ICG_WIREFRAME_SCENE 1")) {
                throw new SceneFormatException("Некорректный заголовок файла сцены.");
            }

            String[] params = split(readRequiredLine(reader, "Ожидались параметры PARAMS"));

            if (params.length != 5 || !params[0].equals("PARAMS")) {
                throw new SceneFormatException("Некорректная строка PARAMS.");
            }

            int k = parseInt(params[1], "K");
            int n = parseInt(params[2], "N");
            int m = parseInt(params[3], "M");
            int m1 = parseInt(params[4], "M1");

            validateParams(k, n, m, m1);

            String[] view = split(readRequiredLine(reader, "Ожидались параметры VIEW"));

            if (view.length != 5 || !view[0].equals("VIEW")) {
                throw new SceneFormatException("Некорректная строка VIEW.");
            }

            double rotX = parseDouble(view[1], "rotX");
            double rotY = parseDouble(view[2], "rotY");
            double rotZ = parseDouble(view[3], "rotZ");
            double zn = parseDouble(view[4], "Zn");

            if (zn <= 0.0) {
                throw new SceneFormatException("Zn должен быть положительным.");
            }

            String[] pointsHeader = split(readRequiredLine(reader, "Ожидалась строка POINTS"));

            if (pointsHeader.length != 2 || !pointsHeader[0].equals("POINTS")) {
                throw new SceneFormatException("Некорректная строка POINTS.");
            }

            int pointsCount = parseInt(pointsHeader[1], "POINTS");

            if (pointsCount != k) {
                throw new SceneFormatException("Количество точек не совпадает с K.");
            }

            List<Point2D> points = new ArrayList<>();

            for (int i = 0; i < pointsCount; i++) {
                String[] parts = split(readRequiredLine(reader, "Ожидалась точка"));

                if (parts.length != 2) {
                    throw new SceneFormatException("Некорректная строка точки.");
                }

                double u = parseDouble(parts[0], "u");
                double v = parseDouble(parts[1], "v");

                points.add(new Point2D(u, v));
            }

            Scene scene = new Scene();

            scene.setControlPoints(points);
            scene.setSplineParameters(new SplineParameters(k, n, m, m1));
            scene.setSceneSettings(new SceneSettings(rotX, rotY, rotZ, zn, true));
            scene.setCamera(new Camera());

            scene.rebuild();

            return scene;
        }
    }

    private String readRequiredLine(BufferedReader reader, String errorMessage)
            throws IOException, SceneFormatException {
        String line = reader.readLine();

        if (line == null) {
            throw new SceneFormatException(errorMessage);
        }

        return line.trim();
    }

    private String[] split(String line) {
        return line.trim().split("\\s+");
    }

    private int parseInt(String text, String name) throws SceneFormatException {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new SceneFormatException("Некорректное целое значение: " + name);
        }
    }

    private double parseDouble(String text, String name) throws SceneFormatException {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new SceneFormatException("Некорректное вещественное значение: " + name);
        }
    }

    private void validateParams(int k, int n, int m, int m1) throws SceneFormatException {
        if (k < 4) {
            throw new SceneFormatException("K должен быть >= 4.");
        }

        if (n < 1) {
            throw new SceneFormatException("N должен быть >= 1.");
        }

        if (m < 2) {
            throw new SceneFormatException("M должен быть >= 2.");
        }

        if (m1 < 1) {
            throw new SceneFormatException("M1 должен быть >= 1.");
        }
    }
}