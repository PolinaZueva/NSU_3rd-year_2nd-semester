package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class PixelBuffer {
    private final BufferedImage image;

    public PixelBuffer(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        clear();
    }

    public void clear() {
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
        } finally {
            g.dispose();
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public void drawPixel(int x, int y, int rgb) {
        if (x < 0 || x >= getWidth()) {
            return;
        }

        if (y < 0 || y >= getHeight()) {
            return;
        }

        image.setRGB(x, y, rgb);
    }
}