package tools;

import model.PixelBuffer;

public interface Tool {
    default void mousePressed(int x, int y, PixelBuffer pbuffer) {}

    /*default void mouseDragged(int x, int y, PixelBuffer pbuffer) {}
    default void mouseReleased(int x, int y, PixelBuffer pbuffer) {}*/
}

//инструмент ластик, непрерывная линия при зажатии мыши