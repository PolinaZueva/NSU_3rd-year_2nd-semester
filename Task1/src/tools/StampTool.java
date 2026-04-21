package tools;

import model.CurrentColorModel;
import model.PixelBuffer;
import model.StampParams;

public class StampTool implements Tool {
    private final StampParams params;
    private final CurrentColorModel colorModel;

    public StampTool(StampParams params, CurrentColorModel colorModel) {
        this.params = params;
        this.colorModel = colorModel;
    }

    @Override
    public void mousePressed(int x, int y, PixelBuffer pbuffer) {
        int n = params.getSides();
        int R = params.getRadius();
        if (n < 3 || R < 1) return;

        int rgb = colorModel.getRgb();
        double rotRad = Math.toRadians(params.getRotationDeg());  //поворот в радианах

        if (params.isStar()) {
            drawStar(x, y, n, R, rotRad, rgb, pbuffer);
        } else {
            drawPolygon(x, y, n, R, rotRad, rgb, pbuffer);
        }
    }

    private void drawPolygon(int x, int y, int n, int R, double rotRad, int rgb, PixelBuffer pbuffer) {
        int[] xPoints = new int[n];  //массив для хранения x координат всех вершин
        int[] yPoints = new int[n];
        double angleStep = 2 * Math.PI / n;  //угол между вершинами

        for (int i = 0; i < n; i++) {
            double angle = rotRad + i * angleStep;  //текущий угол = начальный поворот + смещение
            xPoints[i] = x + (int) Math.round(R * Math.cos(angle));
            yPoints[i] = y - (int) Math.round(R * Math.sin(angle));
        }

        for (int i = 0; i < n; i++) {  //соединяем вершины
            int next = (i + 1) % n;  //следующая вершина (для последней - первая)
            Bresenham.drawLine(xPoints[i], yPoints[i], xPoints[next], yPoints[next], rgb, pbuffer);
        }
    }

    private void drawStar(int x, int y, int n, int R, double rotRad, int rgb, PixelBuffer pbuffer) {
        int r = (int) (R * params.k);
        int m = 2 * n;  //вершин в звезде

        int[] xPoints = new int[m];
        int[] yPoints = new int[m];
        double angleStep = Math.PI / n;  //угол между соседними вершинами звезды

        for (int i = 0; i < m; i++) {
            int radius = (i % 2 == 0) ? R : r;
            double angle = rotRad + i * angleStep;
            xPoints[i] = x + (int) Math.round(radius * Math.cos(angle));
            yPoints[i] = y - (int) Math.round(radius * Math.sin(angle));
        }

        for (int i = 0; i < m; i++) {
            int next = (i + 1) % m;
            Bresenham.drawLine(xPoints[i], yPoints[i], xPoints[next], yPoints[next], rgb, pbuffer);
        }
    }
}
