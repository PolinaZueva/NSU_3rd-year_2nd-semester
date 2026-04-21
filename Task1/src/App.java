import model.CurrentColorModel;
import model.LineParams;
import model.PixelBuffer;
import model.StampParams;
import tools.FillTool;
import tools.LineTool;
import tools.Tool;
import tools.StampTool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;

public class App extends JFrame {
    private final PixelBuffer pbuffer;
    private final PaintPanel ppanel;
    private final JScrollPane scrollPane;
    public enum ToolType { LINE, STAMP, FILL }

    private final CurrentColorModel currentColor = new CurrentColorModel();
    private final LineParams lineParams = new LineParams();
    private final StampParams stampParams = new StampParams();
    private final Tool lineTool = new LineTool(lineParams, currentColor);
    private final Tool fillTool  = new FillTool(currentColor);
    private final Tool stampTool = new StampTool(stampParams, currentColor);

    private JRadioButtonMenuItem mbLine, mbStamp, mbFill;
    private JToggleButton tbLine, tbStamp, tbFill;
    private JButton tbLineParamsBtn;
    private JButton tbStampParamsBtn;

    private JButton tbCurrentColorBtn;

    private int currentRgb = 0xFF000000;

    public App() {
        super("ICGPaint");
        ImageIcon icon = new ImageIcon(getClass().getResource("/resources/cat.png"));
        setIconImage(icon.getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());  //окно делится на 5 областей

        pbuffer = new PixelBuffer(640, 400);
        ppanel = new PaintPanel(pbuffer);

        setJMenuBar(createMenuBar());
        add(createToolBar(), BorderLayout.NORTH);
        scrollPane = new JScrollPane(ppanel);
        add(scrollPane, BorderLayout.CENTER);
        installAutoExpandPanel();

        pack();
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null);

        setCurrentTool(ToolType.LINE);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App());  // Запускаем в потоке обработки событий
    }

    private void installAutoExpandPanel() {
        scrollPane.getViewport().addChangeListener(e -> {
            SwingUtilities.invokeLater(() -> {
                Dimension view = scrollPane.getViewport().getExtentSize();  //размер видимой области
                ppanel.ensurePanelAtLeast(view.width, view.height);  //если холст меньше, чем видимая область - увеличь холст
            });
        });
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("Файл");

        JMenuItem open = new JMenuItem("Открыть...");
        open.addActionListener(e -> openImage());
        file.add(open);

        JMenuItem save = new JMenuItem("Сохранить как PNG...");
        save.addActionListener(e -> saveAsPng());
        file.add(save);

        file.addSeparator();

        JMenuItem exit = new JMenuItem("Выход");
        exit.addActionListener(e -> dispose());
        file.add(exit);

        JMenu edit = new JMenu("Правка");
        JMenuItem clear = new JMenuItem("Очистить");
        clear.addActionListener(e -> ppanel.clear());
        edit.add(clear);

        JMenu tools = new JMenu("Инструменты");
        mbLine = new JRadioButtonMenuItem("Линия");
        mbStamp = new JRadioButtonMenuItem("Штамп");
        mbFill = new JRadioButtonMenuItem("Заливка");

        ButtonGroup toolGroup = new ButtonGroup();  //"залипание" - только один пункт может быть выбран в любой момент времени
        toolGroup.add(mbLine);
        toolGroup.add(mbStamp);
        toolGroup.add(mbFill);

        mbLine.addActionListener(e -> setCurrentTool(ToolType.LINE));
        mbStamp.addActionListener(e -> setCurrentTool(ToolType.STAMP));
        mbFill.addActionListener(e -> setCurrentTool(ToolType.FILL));

        tools.add(mbLine);
        tools.add(mbStamp);
        tools.add(mbFill);

        JMenu params = new JMenu("Параметры");

        JMenuItem colorItem = new JMenuItem("Цвет...");
        colorItem.addActionListener(e -> chooseColorDialog());
        colorItem.setToolTipText("Выбрать текущий цвет");

        JMenuItem lineParamsItem = new JMenuItem("Параметры линии...");
        lineParamsItem.addActionListener(e -> ui.LineParamsDialog.show(this, lineParams));

        JMenuItem stampParamsItem = new JMenuItem("Параметры штампа...");
        stampParamsItem.addActionListener(e -> ui.StampParamsDialog.show(this, stampParams));

        params.add(colorItem);
        params.addSeparator();
        params.add(lineParamsItem);
        params.add(stampParamsItem);

        JMenu help = new JMenu("Справка");
        JMenuItem about = new JMenuItem("О программе");
        about.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "ICGPaint\n\n" +
                        "Автор: Полина Зуева, группа 23206\n\n" +
                        "Назначение программы:\n" +
                        "Растровый графический редактор для рисования линий,\n" +
                        "многоугольников, звёзд и заливки областей.\n\n" +
                        "Инструменты:\n" +
                        "• Линия - рисуется двумя кликами (начало и конец).\n" +
                        "• Штамп - рисует правильный многоугольник или звезду\n" +
                        "  с заданным числом сторон, радиусом и поворотом.\n" +
                        "• Заливка - выполняет алгоритм построчной (span) заливки.\n\n" +
                        "Дополнительные возможности:\n" +
                        "• Выбор цвета (быстрые кнопки и диалог).\n" +
                        "• Настройка толщины линии.\n" +
                        "• Настройка параметров штампа (стороны, радиус, поворот).\n" +
                        "• Изменение размеров рабочей области мышью.\n" +
                        "• Открытие изображений (PNG, JPG, BMP, GIF).\n" +
                        "• Сохранение изображения в формате PNG.",
                "О программе",
                JOptionPane.INFORMATION_MESSAGE
        ));
        help.add(about);

        bar.add(file);
        bar.add(edit);
        bar.add(tools);
        bar.add(params);
        bar.add(help);

        return bar;
    }

    private void openImage() {
        FileDialog fd = new FileDialog (this, "Открыть изображение", FileDialog.LOAD);
        fd.setFile("*.png; *.jpg; *.jpeg; *.gif; *.bmp");
        fd.setVisible(true);
        if (fd.getFile() == null) return;

        File file = new File(fd.getDirectory(), fd.getFile());
        BufferedImage loaded;
        try {
            loaded = ImageIO.read(file);
            if (loaded == null) {
                JOptionPane.showMessageDialog(this,
                        "Формат не поддерживается или файл повреждён.",
                        "Ошибка открытия", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Не удалось открыть файл:\n" + e.getMessage(),
                    "Ошибка открытия", JOptionPane.ERROR_MESSAGE);
            return;
        }
        BufferedImage rgb = new BufferedImage(loaded.getWidth(), loaded.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(loaded, 0, 0, null);
        g.dispose();

        pbuffer.setImage(rgb);
        ppanel.onBufferSizeChanged();
    }

    private void saveAsPng() {
        FileDialog fd = new FileDialog(this, "Сохранить изображение", FileDialog.SAVE);
        fd.setFile("image.png");
        fd.setVisible(true);

        if (fd.getFile() == null) return;

        String name = fd.getFile();
        if (!name.toLowerCase().endsWith(".png")) {
            name += ".png";
        }
        File file = new File(fd.getDirectory(), name);

        try {
            ImageIO.write(pbuffer.getImage(), "png", file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Не удалось сохранить файл:\n" + e.getMessage(),
                    "Ошибка сохранения", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JToolBar createToolBar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        tbLine = new JToggleButton("Линия");
        tbStamp = new JToggleButton("Штамп");
        tbFill = new JToggleButton("Заливка");

        tbLine.setToolTipText("Инструмент: линия (2 клика)");
        tbStamp.setToolTipText("Инструмент: штамп");
        tbFill.setToolTipText("Инструмент: заливка");

        ButtonGroup toolsGroup = new ButtonGroup();
        toolsGroup.add(tbLine);
        toolsGroup.add(tbStamp);
        toolsGroup.add(tbFill);

        tbLine.addActionListener(e -> setCurrentTool(ToolType.LINE));
        tbStamp.addActionListener(e -> setCurrentTool(ToolType.STAMP));
        tbFill.addActionListener(e -> setCurrentTool(ToolType.FILL));

        JButton bClear = new JButton("Очистить");
        bClear.setToolTipText("Очистить холст");
        bClear.addActionListener(e -> ppanel.clear());

        tb.add(tbLine);
        tb.add(tbStamp);
        tb.add(tbFill);
        tb.addSeparator();
        tb.add(bClear);
        tb.addSeparator();

        tb.add(makeColorButton("Чёрный", 0xFF000000));         //Black
        tb.add(makeColorButton("Красный", 0xFFFF0000));        //R
        tb.add(makeColorButton("Жёлтый", 0xFFFFFF00));         //R+G
        tb.add(makeColorButton("Зелёный", 0xFF00FF00));        //G
        tb.add(makeColorButton("Голубой", 0xFF00FFFF));        //G+B
        tb.add(makeColorButton("Синий", 0xFF0000FF));          //B
        tb.add(makeColorButton("Пурпурный", 0xFFFF00FF));      //R+B
        tb.add(makeColorButton("Белый", 0xFFFFFFFF));          //White

        tb.addSeparator();
        tbCurrentColorBtn = createCurrentColorButton();
        tb.add(tbCurrentColorBtn);

        tbLineParamsBtn = new JButton("Параметры линии");
        tbLineParamsBtn.setToolTipText("Изменить толщину линии");
        tbLineParamsBtn.addActionListener(e -> ui.LineParamsDialog.show(this, lineParams));
        tb.addSeparator();
        tb.add(tbLineParamsBtn);

        tbStampParamsBtn = new JButton("Параметры штампа");
        tbStampParamsBtn.setToolTipText("Форма/радиус/поворот");
        tbStampParamsBtn.addActionListener(e -> ui.StampParamsDialog.show(this, stampParams));
        tb.add(tbStampParamsBtn);

        return tb;
    }

    private JButton makeColorButton(String name, int rgb) {
        JButton b = new JButton();
        b.setToolTipText("Цвет: " + name);
        b.setPreferredSize(new Dimension(24, 24));
        b.setMinimumSize(new Dimension(24, 24));
        b.setMaximumSize(new Dimension(24, 24));

        b.setBackground(new Color(rgb));
        b.setOpaque(true);
        b.setBorderPainted(true);
        b.setFocusPainted(false);

        b.addActionListener(e -> setCurrentColor(rgb));
        return b;
    }

    private JButton createCurrentColorButton() {
        JButton b = new JButton("Цвет");
        b.setToolTipText("Текущий цвет (нажми, чтобы выбрать новый)");
        b.setPreferredSize(new Dimension(24, 24));
        b.setMinimumSize(new Dimension(24, 24));
        b.setMaximumSize(new Dimension(24, 24));

        b.setOpaque(true);
        b.setBorderPainted(true);
        b.setFocusPainted(false);

        b.addActionListener(e -> chooseColorDialog());

        updateCurrentColorButton(b);
        return b;
    }

    private void setCurrentTool(ToolType type) {
        // ставим инструмент
        switch (type) {
            case LINE -> ppanel.setTool(lineTool);
            case STAMP -> ppanel.setTool(stampTool);
            case FILL -> ppanel.setTool(fillTool);
        }

        // синхронизация меню
        if (mbLine != null) mbLine.setSelected(type == ToolType.LINE);
        if (mbStamp != null) mbStamp.setSelected(type == ToolType.STAMP);
        if (mbFill != null) mbFill.setSelected(type == ToolType.FILL);

        // синхронизация toolbar
        if (tbLine != null) tbLine.setSelected(type == ToolType.LINE);
        if (tbStamp != null) tbStamp.setSelected(type == ToolType.STAMP);
        if (tbFill != null) tbFill.setSelected(type == ToolType.FILL);

        if (tbLineParamsBtn != null) {
            tbLineParamsBtn.setEnabled(type == ToolType.LINE);
        }

        if (tbStampParamsBtn != null) {
            tbStampParamsBtn.setEnabled(type == ToolType.STAMP);
        }
    }

    private void setCurrentColor(int rgb) {
        currentRgb = rgb;
        currentColor.setRgb(currentRgb);

        if (tbCurrentColorBtn != null) {
            updateCurrentColorButton(tbCurrentColorBtn);
        }
    }

    private void updateCurrentColorButton(JButton b) {
        b.setBackground(new Color(currentRgb, true));
        b.repaint();
    }

    private void chooseColorDialog() {
        Color initial = new Color(currentRgb, true);
        Color chosen = JColorChooser.showDialog(this, "Выбор цвета", initial);
        if (chosen != null) {
            setCurrentColor(chosen.getRGB());
        }
    }
}
