package model;

import java.awt.image.BufferedImage;  //представляет растровое изображение в оперативной памяти -> массив пискелей
import java.awt.Graphics2D;
import java.awt.Color;

public class PixelBuffer {
    private BufferedImage image;

    public PixelBuffer(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        clear();
    }

    public void clear() {
        int white = 0xFFFFFF;
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                image.setRGB(x, y, white);
            }
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage newImage) {
        this.image = newImage;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int getPixel(int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return Integer.MIN_VALUE;
        }
        return image.getRGB(x, y);
    }

    public void drawPixel(int x, int y, int rgb) {
        if (x < 0 || x >= getWidth()) return;
        if (y < 0 || y >= getHeight()) return;

        image.setRGB(x, y, rgb);
    }

    public void resizePanel(int newWidth, int newHeight) {
        if (newWidth < 1 || newHeight < 1) return;

        int oldWidth = image.getWidth();
        int oldHeight = image.getHeight();

        if (newWidth <= oldWidth && newHeight <= oldHeight) return;

        int targetW = Math.max(oldWidth, newWidth);
        int targetH = Math.max(oldHeight, newHeight);

        BufferedImage resized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = resized.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, targetW, targetH);
            g.drawImage(image, 0, 0, null);  //копируем старое изображение
        } finally {
            g.dispose();  //освобождение системных ресурсов
        }
        image = resized;
    }
}
