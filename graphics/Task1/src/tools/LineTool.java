package tools;

import model.CurrentColorModel;
import model.LineParams;
import model.PixelBuffer;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;

public class LineTool implements Tool {
    private boolean hasFirstPixel = false;
    private int x0, y0;
    private final LineParams params;
    private final CurrentColorModel colorModel;

    public LineTool(LineParams params, CurrentColorModel colorModel) {
        this.params = params;
        this.colorModel = colorModel;
    }

    @Override
    public void mousePressed(int x, int y, PixelBuffer pbuffer) {
        if (!hasFirstPixel) {
            x0 = x;
            y0 = y;
            hasFirstPixel = true;
        } else {
            if (params.getThickness() == 1) {
                int rgb = colorModel.getRgb();
                tools.Bresenham.drawLine(x0, y0, x, y, rgb, pbuffer);
            } else {
                drawThickLineStandard(x0, y0, x, y, pbuffer);
            }
            hasFirstPixel = false;
        }
    }

    private void drawThickLineStandard(int x1, int y1, int x2, int y2, PixelBuffer pbuffer) {
        Graphics2D g2 = pbuffer.getImage().createGraphics();
        try {
            g2.setColor(new Color(colorModel.getRgb()));
            g2.setStroke(new BasicStroke(params.getThickness()));  //определяет параметры
            g2.drawLine(x1, y1, x2, y2);
        } finally {
            g2.dispose();
        }
    }
}
