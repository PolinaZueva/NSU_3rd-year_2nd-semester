package ui;

import model.Point2D;
import model.Scene;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SplineEditorDialog extends JDialog {
    private final Scene scene;
    private final SplineEditorPanel editorPanel;
    private final Runnable onApply;

    private JSpinner kSpinner;
    private JSpinner nSpinner;
    private JSpinner mSpinner;
    private JSpinner m1Spinner;

    private boolean updatingFromCode = false;

    public SplineEditorDialog(Frame owner, Scene scene, Runnable onApply) {
        super(owner, "B-Spline configure", false);

        this.scene = scene;
        this.onApply = onApply;
        syncKFromRealPoints();

        this.editorPanel = new SplineEditorPanel(scene, this::refreshKSpinnerFromPoints);

        setLayout(new BorderLayout());
        add(editorPanel, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(640, 480));
        setSize(700, 500);
        setLocationRelativeTo(owner);

        refreshKSpinnerFromPoints();
    }

    private JPanel createBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JPanel paramsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));

        kSpinner = new JSpinner(new SpinnerNumberModel(
                scene.getControlPoints().size(), 4, 100, 1
        ));

        nSpinner = new JSpinner(new SpinnerNumberModel(
                scene.getSplineParameters().getN(), 1, 200, 1
        ));

        mSpinner = new JSpinner(new SpinnerNumberModel(
                scene.getSplineParameters().getM(), 2, 200, 1
        ));

        m1Spinner = new JSpinner(new SpinnerNumberModel(
                scene.getSplineParameters().getM1(), 1, 50, 1
        ));

        kSpinner.addChangeListener(e -> {
            if (!updatingFromCode) {
                applyKFromSpinner();
            }
        });

        ChangeListener commonListener = e -> applyParameters();

        nSpinner.addChangeListener(commonListener);
        mSpinner.addChangeListener(commonListener);
        m1Spinner.addChangeListener(commonListener);

        paramsPanel.add(new JLabel("K:"));
        paramsPanel.add(kSpinner);

        paramsPanel.add(new JLabel("N:"));
        paramsPanel.add(nSpinner);

        paramsPanel.add(new JLabel("M:"));
        paramsPanel.add(mSpinner);

        paramsPanel.add(new JLabel("M1:"));
        paramsPanel.add(m1Spinner);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));

        JButton applyButton = new JButton("Применить");
        JButton defaultButton = new JButton("Стандарт");
        JButton closeButton = new JButton("Закрыть");

        applyButton.setToolTipText("Перестроить B-сплайн и тело вращения");
        defaultButton.setToolTipText("Сбросить стандартные точки и параметры");
        closeButton.setToolTipText("Закрыть редактор");

        applyButton.addActionListener(e -> applyParameters());
        defaultButton.addActionListener(e -> setDefaultState());
        closeButton.addActionListener(e -> dispose());

        buttonsPanel.add(applyButton);
        buttonsPanel.add(defaultButton);
        buttonsPanel.add(closeButton);

        bottom.add(paramsPanel, BorderLayout.CENTER);
        bottom.add(buttonsPanel, BorderLayout.EAST);

        return bottom;
    }

    private void applyKFromSpinner() {
        int newK = (Integer) kSpinner.getValue();

        setControlPointCount(newK);
        scene.getSplineParameters().setK(newK);

        editorPanel.repaint();
    }

    private void applyParameters() {
        syncKFromRealPoints();

        scene.getSplineParameters().setN((Integer) nSpinner.getValue());
        scene.getSplineParameters().setM((Integer) mSpinner.getValue());
        scene.getSplineParameters().setM1((Integer) m1Spinner.getValue());

        editorPanel.rebuildModel();
        editorPanel.repaint();

        if (onApply != null) {
            onApply.run();
        }
    }

    private void refreshKSpinnerFromPoints() {
        int realK = scene.getControlPoints().size();

        updatingFromCode = true;
        kSpinner.setValue(realK);
        updatingFromCode = false;

        scene.getSplineParameters().setK(realK);
    }

    private void syncKFromRealPoints() {
        scene.getSplineParameters().setK(scene.getControlPoints().size());
    }

    private void setControlPointCount(int newK) {
        List<Point2D> points = scene.getControlPoints();

        while (points.size() < newK) {
            points.add(createNewPoint(points));
        }

        while (points.size() > newK) {
            points.remove(points.size() - 1);
        }
    }

    private Point2D createNewPoint(List<Point2D> points) {
        if (points.isEmpty()) {
            return new Point2D(0.0, 0.0);
        }

        Point2D last = points.get(points.size() - 1);

        return new Point2D(
                last.getU() + 0.4,
                last.getV()
        );
    }

    private void setDefaultState() {
        List<Point2D> defaultPoints = Scene.createDefaultControlPoints();

        scene.setControlPoints(defaultPoints);

        updatingFromCode = true;
        kSpinner.setValue(defaultPoints.size());
        nSpinner.setValue(10);
        mSpinner.setValue(12);
        m1Spinner.setValue(1);
        updatingFromCode = false;

        applyParameters();
        refreshKSpinnerFromPoints();
        editorPanel.repaint();
    }
}