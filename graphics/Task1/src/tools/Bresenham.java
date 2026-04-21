package tools;

import model.PixelBuffer;

public class Bresenham {
    public static void drawLine(int x1, int y1, int x2, int y2, int rgb, PixelBuffer pbuffer) {
        int dx = x2 - x1;
        int dy = y2 - y1;

        int x = x1;
        int y = y1;
        int err;

        if (dx > 0 && dy >= 0 && dx >= dy) {  //1-ый октант
            err = 0;
            for (int i = 0; i <= dx; i++) {
                pbuffer.drawPixel(x, y, rgb);
                x++;
                err += 2 * dy;
                if (err > dx) {
                    y++;
                    err -= 2 * dx;
                }
            }
        }
        else if (dx > 0 && dy >= 0 && dx < dy) {  //2-ой октант
            err = 0;
            for (int i = 0; i <= dy; i++) {
                pbuffer.drawPixel(x, y, rgb);
                y++;
                err += 2 * dx;
                if (err > dy) {
                    x++;
                    err -= 2 * dy;
                }
            }
        }
        else if (dx <= 0 && dy > 0 && -dx <= dy) {  //3-ий октант
            err = 0;
            for (int i = 0; i <= dy; i++) {
                pbuffer.drawPixel(x, y, rgb);
                y++;
                err += 2 * (-dx);
                if (err > dy) {
                    x--;
                    err -= 2 * dy;
                }
            }
        }
        else if (dx < 0 && dy >= 0 && -dx > dy) {  //4-ый октант
            err = 0;
            for (int i = 0; i <= -dx; i++) {
                pbuffer.drawPixel(x, y, rgb);
                x--;
                err += 2 * dy;
                if (err > -dx) {
                    y++;
                    err -= 2 * (-dx);
                }
            }
        }
        else if (dx < 0 && dy < 0 && -dx >= -dy) {  //5-ый октант
            err = 0;
            for (int i = 0; i <= -dx; i++) {
                pbuffer.drawPixel(x, y, rgb);
                x--;
                err += 2 * (-dy);
                if (err > -dx) {
                    y--;
                    err -= 2 * (-dx);
                }
            }
        }
        else if (dx <= 0 && dy < 0 && -dx <= -dy) {  //6-ой октант
            err = 0;
            for (int i = 0; i <= -dy; i++) {
                pbuffer.drawPixel(x, y, rgb);
                y--;
                err += 2 * (-dx);
                if (err > -dy) {
                    x--;
                    err -= 2 * (-dy);
                }
            }
        }
        else if (dx > 0 && dy < 0 && dx >= -dy) {  //7-ой октант
            err = 0;
            for (int i = 0; i <= dx; i++) {
                pbuffer.drawPixel(x, y, rgb);
                x++;
                err += 2 * (-dy);
                if (err > dx) {
                    y--;
                    err -= 2 * dx;
                }
            }
        }
        else if (dx > 0 && dy < 0 && dx < -dy) {  //8-ой октант
            err = 0;
            for (int i = 0; i <= -dy; i++) {
                pbuffer.drawPixel(x, y, rgb);
                y--;
                err += 2 * dx;
                if (err > -dy) {
                    x++;
                    err -= 2 * (-dy);
                }
            }
        }
    }
}