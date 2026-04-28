import model.Camera;
import model.Point2D;
import model.Scene;
import model.SceneSettings;
import model.SplineParameters;
import ui.ScenePanel;
import ui.SplineEditorDialog;
import math.BSplineBuilder;
import math.FigureBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class App extends JFrame {
    private final Scene scene;
    private final ScenePanel scenePanel;

    public App() {
        super("ICGApp");

        this.scene = createDefaultScene();
        this.scenePanel = new ScenePanel(scene);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());  //окно делится на 5 областей

        setJMenuBar(createMenuBar());
        add(createToolBar(), BorderLayout.NORTH);
        add(scenePanel, BorderLayout.CENTER);

        pack();
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

        BSplineBuilder splineBuilder = new BSplineBuilder();
        FigureBuilder figureBuilder = new FigureBuilder();

        scene.setSplinePoints(
                splineBuilder.buildSpline(controlPoints, scene.getSplineParameters().getN())
        );

        scene.setFigurePoints(
                figureBuilder.buildFigure(scene.getSplinePoints(), scene.getSplineParameters().getM())
        );

        return scene;
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        JMenuItem openItem = new JMenuItem("Открыть...");
        JMenuItem saveItem = new JMenuItem("Сохранить как PNG...");
        JMenuItem exitItem = new JMenuItem("Выйти");

        openItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Пока не реализовано")
        );
        saveItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Пока не реализовано")
        );
        exitItem.addActionListener(e -> dispose());

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Редактор");
        JMenuItem splineEditorItem = new JMenuItem("Сплайн редактор...");
        splineEditorItem.addActionListener(e -> openSplineEditor());
        editMenu.add(splineEditorItem);

        JMenu helpMenu = new JMenu("Справка");
        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "ICGApp\n\n" +
                        "Автор: Полина Зуева, группа 23206\n\n" +
                        "Редактор образующей B-сплайна и отображение\n" +
                        "проволочной фигуры вращения.",
                "О программе",
                JOptionPane.INFORMATION_MESSAGE
        ));
        helpMenu.add(aboutItem);

        bar.add(fileMenu);
        bar.add(editMenu);
        bar.add(helpMenu);

        return bar;
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton editSplineButton = new JButton("Редактор сплайна");
        editSplineButton.setToolTipText("Открыть редактор образующей");
        editSplineButton.addActionListener(e -> openSplineEditor());

        JButton resetViewButton = new JButton("Reset view");
        resetViewButton.setToolTipText("Сбросить углы поворота");
        resetViewButton.addActionListener(e -> {
            scene.getSceneSettings().setRotX(0.0);
            scene.getSceneSettings().setRotY(0.0);
            scene.getSceneSettings().setRotZ(0.0);
            scenePanel.repaint();
        });

        toolBar.add(editSplineButton);
        toolBar.addSeparator();
        toolBar.add(resetViewButton);

        return toolBar;
    }

    private void openSplineEditor() {
        SplineEditorDialog dialog = new SplineEditorDialog(
                this,
                scene,
                () -> scenePanel.repaint()
        );

        dialog.setVisible(true);
    }
}