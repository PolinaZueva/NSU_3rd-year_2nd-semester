package ui;

import model.Point2D;
import model.Scene;
import model.SplineParameters;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SplineEditorDialog extends JDialog {
    private final Scene scene;
    private final SplineEditorPanel editorPanel;
    private final Runnable onApply;

    private ParametersPanel parametersPanel;

    private List<Point2D> initialControlPoints;
    private SplineParameters initialParams;

    public SplineEditorDialog(Frame owner, Scene scene, Runnable onApply) {
        super(owner, "B-Spline configure", false);

        this.scene = scene;
        this.onApply = onApply;

        syncKFromRealPoints();

        this.editorPanel = new SplineEditorPanel(scene, this::onEditorPointsChanged);

        setLayout(new BorderLayout());
        add(editorPanel, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setMinimumSize(new Dimension(640, 480));
        setSize(800, 600);
        setLocationRelativeTo(owner);

        refreshKSpinnerFromPoints();
        saveInitialState();
    }

    private JPanel createBottomPanel() {
        parametersPanel = new ParametersPanel(
                scene.getSplineParameters(),
                scene.getControlPoints().size()
        );

        parametersPanel.addCommonParametersChangeListener(e -> {
            if (parametersPanel.isUpdatingFromCode()) {
                return;
            }

            scene.getSplineParameters().setN(parametersPanel.getNValue());
            scene.getSplineParameters().setM(parametersPanel.getMValue());
            scene.getSplineParameters().setM1(parametersPanel.getM1Value());

            editorPanel.rebuildModel();
            editorPanel.repaint();

            if (parametersPanel.isAutoUpdateEnabled() && onApply != null) {
                scene.rebuild();
                onApply.run();
            }
        });

        parametersPanel.addApplyAction(this::applyParameters);
        parametersPanel.addCancelAction(this::restoreInitialState);
        parametersPanel.addDefaultAction(this::setDefaultState);
        parametersPanel.addCloseAction(this::dispose);

        return parametersPanel;
    }

    private void applyParameters() {
        if (!syncParametersFromControls()) {
            return;
        }

        scene.rebuild();
        editorPanel.rebuildModel();
        editorPanel.repaint();

        if (onApply != null) {
            onApply.run();
        }
    }

    private void rebuildAndRepaintEverything() {
        scene.rebuild();

        editorPanel.rebuildModel();
        editorPanel.repaint();

        if (onApply != null) {
            onApply.run();
        }
    }

    private boolean syncParametersFromControls() {
        if (!parametersPanel.commitValuesOrShowError(this)) {
            return false;
        }

        syncKFromRealPoints();

        scene.getSplineParameters().setN(parametersPanel.getNValue());
        scene.getSplineParameters().setM(parametersPanel.getMValue());
        scene.getSplineParameters().setM1(parametersPanel.getM1Value());

        return true;
    }

    private void onEditorPointsChanged() {
        refreshKSpinnerFromPoints();
        updateMainSceneIfNeeded();
    }

    private void refreshKSpinnerFromPoints() {
        int realK = scene.getControlPoints().size();

        parametersPanel.setKValue(realK);
        scene.getSplineParameters().setK(realK);
    }

    private void syncKFromRealPoints() {
        scene.getSplineParameters().setK(scene.getControlPoints().size());
    }

    private void setDefaultState() {
        List<Point2D> defaultPoints = Scene.createDefaultControlPoints();

        scene.setControlPoints(defaultPoints);

        scene.getSplineParameters().setK(defaultPoints.size());
        scene.getSplineParameters().setN(10);
        scene.getSplineParameters().setM(12);
        scene.getSplineParameters().setM1(1);

        parametersPanel.setValues(
                defaultPoints.size(),
                10,
                12,
                1
        );

        rebuildAndRepaintEverything();
    }

    private void saveInitialState() {
        initialControlPoints = copyPoints(scene.getControlPoints());

        initialParams = new SplineParameters(
                scene.getSplineParameters().getK(),
                scene.getSplineParameters().getN(),
                scene.getSplineParameters().getM(),
                scene.getSplineParameters().getM1()
        );
    }

    private void restoreInitialState() {
        scene.setControlPoints(copyPoints(initialControlPoints));

        scene.getSplineParameters().setK(initialParams.getK());
        scene.getSplineParameters().setN(initialParams.getN());
        scene.getSplineParameters().setM(initialParams.getM());
        scene.getSplineParameters().setM1(initialParams.getM1());

        parametersPanel.setValues(
                initialParams.getK(),
                initialParams.getN(),
                initialParams.getM(),
                initialParams.getM1()
        );

        rebuildAndRepaintEverything();
    }

    private List<Point2D> copyPoints(List<Point2D> points) {
        List<Point2D> copy = new ArrayList<>();

        for (Point2D p : points) {
            copy.add(new Point2D(p.getU(), p.getV()));
        }

        return copy;
    }

    private void updateMainSceneIfNeeded() {
        if (parametersPanel != null && parametersPanel.isAutoUpdateEnabled()) {
            scene.rebuild();

            if (onApply != null) {
                onApply.run();
            }
        }
    }
}