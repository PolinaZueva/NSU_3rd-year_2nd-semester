package tools;

import model.CurrentColorModel;
import model.PixelBuffer;
import model.Span;

import java.util.Stack;

public class FillTool implements Tool {
    private final CurrentColorModel colorModel;

    public FillTool(CurrentColorModel colorModel) {
        this.colorModel = colorModel;
    }

    @Override
    public void mousePressed(int x, int y, PixelBuffer pbuffer) {
        if (x < 0 || x >= pbuffer.getWidth()) return;
        if (y < 0 || y >= pbuffer.getHeight()) return;

        int newColor = colorModel.getRgb();
        int oldColor = pbuffer.getPixel(x, y);
        if (oldColor == newColor) return;

        spanFill(x, y, oldColor, newColor, pbuffer);
    }

    private void spanFill(int x0, int y0, int oldColor, int newColor, PixelBuffer pbuffer) {
        Stack<Span> stack = new Stack<>();
        stack.push(new Span(x0, y0));

        while (!stack.isEmpty()) {
            Span span = stack.pop();
            int x = span.x;
            int y = span.y;

            if (pbuffer.getPixel(x, y) != oldColor) continue;  //переходим к следующей итерации: если точка перекрашена, точка попала в стек не в первый раз

            int left = x;
            while (left >= 0 && pbuffer.getPixel(left, y) == oldColor) left--;
            left++;

            int right = x;
            while (right < pbuffer.getWidth() && pbuffer.getPixel(right, y) == oldColor) right++;
            right--;

            for (int i = left; i <= right; i++) {
                pbuffer.drawPixel(i, y, newColor);
            }

            if (y > 0) {
                pushSpansOnRow(left, right, y - 1, oldColor, pbuffer, stack);
            }

            if (y < pbuffer.getHeight() - 1) {
                pushSpansOnRow(left, right, y + 1, oldColor, pbuffer, stack);
            }
        }
    }

    private void pushSpansOnRow(int left, int right, int y, int oldColor, PixelBuffer pbuffer, Stack<Span> stack) {
        int x = left;
        while (x <= right) {  //ищем начало нового участка старого цвета
            while (x <= right && pbuffer.getPixel(x, y) != oldColor) x++;
            if (x > right) break;

            stack.push(new Span(x, y));

            while (x <= right && pbuffer.getPixel(x, y) == oldColor) x++;  //пропускаем все пиксели старого цвета
        }
    }
}
