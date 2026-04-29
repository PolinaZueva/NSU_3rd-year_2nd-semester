import model.Camera;
import model.Point2D;
import model.Scene;
import model.SceneSettings;
import model.SplineParameters;
import ui.ScenePanel;
import ui.SplineEditorDialog;

import io.SceneFormatException;
import io.SceneReader;
import io.SceneWriter;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class App extends JFrame {
    private final Scene scene;
    private final ScenePanel scenePanel;
    private SplineEditorDialog splineEditorDialog;

    public App() {
        super("ICGApp");

        this.scene = createDefaultScene();
        this.scenePanel = new ScenePanel(scene);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setJMenuBar(createMenuBar());
        add(createToolBar(), BorderLayout.NORTH);
        add(scenePanel, BorderLayout.CENTER);

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App());
    }

    private Scene createDefaultScene() {
        Scene scene = new Scene();

        List<Point2D> controlPoints = Scene.createDefaultControlPoints();

        scene.setControlPoints(controlPoints);

        scene.setSplineParameters(new SplineParameters(controlPoints.size(), 10, 12, 1));
        scene.setSceneSettings(new SceneSettings(0.0, 0.0, 0.0, 3.0, true));
        scene.setCamera(new Camera());

        scene.rebuild();

        return scene;
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        JMenuItem openItem = new JMenuItem("Открыть...");
        JMenuItem saveItem = new JMenuItem("Сохранить сцену...");
        JMenuItem exitItem = new JMenuItem("Выйти");

        openItem.addActionListener(e -> openSceneFromFile());
        saveItem.addActionListener(e -> saveSceneToFile());
        exitItem.addActionListener(e -> dispose());

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Редактор");
        JMenuItem splineEditorItem = new JMenuItem("Сплайн редактор...");
        splineEditorItem.addActionListener(e -> openSplineEditor());
        editMenu.add(splineEditorItem);

        JMenu viewMenu = new JMenu("Вид");
        JMenuItem resetViewItem = new JMenuItem("Сбросить вид");
        resetViewItem.addActionListener(e -> resetView());
        viewMenu.add(resetViewItem);

        JMenu helpMenu = new JMenu("Справка");
        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "ICGApp\n\n" +
                        "Автор: Полина Зуева, группа 23206\n\n" +
                        "Приложение предназначено для построения и визуализации\n" +
                        "проволочной 3D-фигуры вращения на основе образующей,\n" +
                        "заданной B-сплайном.\n\n" +

                        "Функциональность:\n" +
                        "- Редактирование опорных точек образующей (добавление, перемещение, удаление)\n" +
                        "- Построение гладкой кривой B-сплайна\n" +
                        "- Формирование тела вращения вокруг оси\n" +
                        "- Перспективная проекция 3D-сцены\n" +
                        "- Вращение сцены с помощью мыши\n" +
                        "- Масштабирование (zoom)\n" +
                        "- Визуализация глубины с помощью цвета\n\n" +

                        "Параметры:\n" +
                        "K - число опорных точек\n" +
                        "N - детализация сплайна\n" +
                        "M - число образующих\n" +
                        "M1 - детализация окружностей\n\n" +

                        "Отрисовка линий выполняется алгоритмом Брезенхема.",
                "О программе",
                JOptionPane.INFORMATION_MESSAGE
        ));
        helpMenu.add(aboutItem);

        bar.add(fileMenu);
        bar.add(editMenu);
        bar.add(viewMenu);
        bar.add(helpMenu);

        return bar;
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton editSplineButton = new JButton("Редактор сплайна");
        editSplineButton.setToolTipText("Открыть редактор образующей");
        editSplineButton.addActionListener(e -> openSplineEditor());

        JButton resetViewButton = new JButton("Сбросить вид");
        resetViewButton.setToolTipText("Сбросить поворот и масштаб");
        resetViewButton.addActionListener(e -> resetView());

        toolBar.add(editSplineButton);
        toolBar.addSeparator();
        toolBar.add(resetViewButton);

        return toolBar;
    }

    private void openSplineEditor() {
        if (splineEditorDialog != null && splineEditorDialog.isDisplayable()) {
            splineEditorDialog.toFront();
            splineEditorDialog.requestFocus();
            return;
        }

        splineEditorDialog = new SplineEditorDialog(
                this,
                scene,
                () -> scenePanel.repaint()
        );

        arrangeWindowsForEditor(splineEditorDialog);

        splineEditorDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                restoreMainWindowAfterEditor();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                restoreMainWindowAfterEditor();
            }
        });

        splineEditorDialog.setVisible(true);
    }

    private void arrangeWindowsForEditor(SplineEditorDialog dialog) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int margin = 30;
        int gap = 10;

        int editorWidth = Math.min(860, (screen.width - 2 * margin - gap) / 2);
        int mainWidth = Math.min(860, (screen.width - 2 * margin - gap) / 2);
        int height = Math.min(750, screen.height - 2 * margin);

        int totalWidth = editorWidth + gap + mainWidth;
        int startX = (screen.width - totalWidth) / 2;
        int startY = (screen.height - height) / 2;

        dialog.setSize(editorWidth, height);
        dialog.setLocation(startX, startY);

        setSize(mainWidth, height);
        setLocation(startX + editorWidth + gap, startY);
    }

    private void restoreMainWindowAfterEditor() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        scenePanel.repaint();
    }

    private void resetView() {
        scene.getSceneSettings().setRotX(0.0);
        scene.getSceneSettings().setRotY(0.0);
        scene.getSceneSettings().setRotZ(0.0);
        scene.getSceneSettings().setZn(3.0);
        scenePanel.repaint();
    }

    private void saveSceneToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить сцену");

        int result = chooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {
            new SceneWriter().write(scene, file);
            JOptionPane.showMessageDialog(this, "Сцена успешно сохранена.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Не удалось сохранить файл.",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void openSceneFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Открыть сцену");

        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {
            Scene loadedScene = new SceneReader().read(file);
            copyScene(loadedScene, scene);
            scenePanel.repaint();

            JOptionPane.showMessageDialog(this, "Сцена успешно загружена.");
        } catch (SceneFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Некорректный файл сцены:\n" + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Не удалось открыть файл.",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void copyScene(Scene source, Scene target) {
        target.setControlPoints(source.getControlPoints());
        target.setSplinePoints(source.getSplinePoints());
        target.setFigurePoints(source.getFigurePoints());
        target.setSplineParameters(source.getSplineParameters());
        target.setSceneSettings(source.getSceneSettings());
        target.setCamera(source.getCamera());
    }
}