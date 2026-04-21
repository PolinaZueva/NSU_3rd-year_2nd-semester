import model.PixelBuffer;
import tools.Tool;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Cursor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class PaintPanel extends JPanel {
    private final PixelBuffer pbuffer;
    private Tool currentTool;
    private static final int RESIZE_MARGIN = 12;  //зона чувствительности у края панели
    private boolean resizing = false;
    private int resizeMode = 0;  //флаг состояния: 0-none, 1-right, 2-bottom, 3-corner
    private int pressX, pressY;
    private int startW, startH;

    public PaintPanel(PixelBuffer pbuffer) {
        this.pbuffer = pbuffer;

        setPreferredSize(new Dimension(pbuffer.getImage().getWidth(), pbuffer.getImage().getHeight()));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;

                int mode = hitTestResizeHandle(e.getX(), e.getY());
                if (mode != 0) {
                    resizing = true;
                    resizeMode = mode;
                    pressX = e.getX();
                    pressY = e.getY();
                    startW = pbuffer.getWidth();
                    startH = pbuffer.getHeight();
                    return;
                }

                if (currentTool != null) {
                    currentTool.mousePressed(e.getX(), e.getY(), pbuffer);
                }
                repaint();
            }

            //отпустили кнопку мыши
            @Override
            public void mouseReleased(MouseEvent e) {
                resizing = false;
                resizeMode = 0;
                updateCursor(e.getX(), e.getY());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            //мышь двигается без зажатой кнопки
            @Override
            public void mouseMoved(MouseEvent e) {
                updateCursor(e.getX(), e.getY());
            }

            //движение мыши при зажатой кнопке
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!resizing) return;
                handleResizeDrag(e.getX(), e.getY());  //пересчитываем новый размер холста и расширяем картинку
            }
        });
    }

    public void setTool(Tool tool) {
        this.currentTool = tool;
    }

    public void clear() {
        pbuffer.clear();
        repaint();
    }

    public void onBufferSizeChanged() {
        setPreferredSize(new Dimension(pbuffer.getWidth(), pbuffer.getHeight()));
        revalidate();  //т.е. размер панели изменился, пересчитай скроллы, JScrollPane
        repaint();  //перерисовка
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(pbuffer.getImage(), 0, 0, null);
    }

    //проверка, что "мышь сейчас рядом с краем ресайза или нет"
    private int hitTestResizeHandle(int x, int y) {
        boolean nearRight = (x >= pbuffer.getWidth() - RESIZE_MARGIN);
        boolean nearBottom = (y >= pbuffer.getHeight() - RESIZE_MARGIN);

        if (nearRight && nearBottom) return 3;  // угол
        if (nearRight) return 1;                // правый край
        if (nearBottom) return 2;               // нижний край
        return 0;
    }

    private void updateCursor(int x, int y) {
        int mode = hitTestResizeHandle(x, y);

        int cursorType = switch (mode) {
            case 1 -> Cursor.E_RESIZE_CURSOR;
            case 2 -> Cursor.S_RESIZE_CURSOR;
            case 3 -> Cursor.SE_RESIZE_CURSOR;
            default -> Cursor.DEFAULT_CURSOR;
        };

        setCursor(Cursor.getPredefinedCursor(cursorType));  //применяем курсор
    }

    private void handleResizeDrag(int x, int y) {
        int dx = x - pressX;
        int dy = y - pressY;

        int newW = startW;
        int newH = startH;

        if (resizeMode == 1 || resizeMode == 3) {
            newW = Math.max(1, startW + dx);
        }
        if (resizeMode == 2 || resizeMode == 3) {
            newH = Math.max(1, startH + dy);
        }

        pbuffer.resizePanel(newW, newH);  //расширяем картинку
        onBufferSizeChanged();  //обновляем панель
    }

    //если пользователь растянул окно больше, чем холст - холст автоматически увеличивается
    public void ensurePanelAtLeast(int minW, int minH) {
        int w = pbuffer.getWidth();
        int h = pbuffer.getHeight();

        int newW = Math.max(w, minW);
        int newH = Math.max(h, minH);

        if (newW != w || newH != h) {
            pbuffer.resizePanel(newW, newH);
            onBufferSizeChanged();
        }
    }
}
