package ui;

import model.SplineParameters;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ParametersPanel extends JPanel {
    private final JTextField kField;
    private final JSpinner nSpinner;
    private final JSpinner mSpinner;
    private final JSpinner m1Spinner;

    private final JCheckBox autoUpdateCheckBox;

    private final JButton applyButton;
    private final JButton cancelButton;
    private final JButton defaultButton;
    private final JButton closeButton;

    private boolean updatingFromCode = false;

    public ParametersPanel(SplineParameters params, int realK) {
        super(new BorderLayout());

        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        kField = new JTextField(String.valueOf(realK), 3);
        kField.setEditable(false);
        kField.setFocusable(false);
        kField.setToolTipText("K изменяется добавлением или удалением точек мышью");
        nSpinner = new JSpinner(new SpinnerNumberModel(params.getN(), -1, 100, 1));
        mSpinner = new JSpinner(new SpinnerNumberModel(params.getM(), -2, 100, 1));
        m1Spinner = new JSpinner(new SpinnerNumberModel(params.getM1(), -1, 30, 1));
        nSpinner.setToolTipText("N: от 1 до 100");
        mSpinner.setToolTipText("M: от 2 до 100");
        m1Spinner.setToolTipText("M1: от 1 до 30");

        autoUpdateCheckBox = new JCheckBox("Автообновление", true);

        applyButton = new JButton("Применить");
        cancelButton = new JButton("Отмена");
        defaultButton = new JButton("Стандарт");
        closeButton = new JButton("Закрыть");

        applyButton.setToolTipText("Перестроить сцену без закрытия редактора");
        cancelButton.setToolTipText("Вернуть состояние на момент открытия редактора");
        defaultButton.setToolTipText("Вернуть стандартную образующую и параметры");
        closeButton.setToolTipText("Закрыть редактор, оставив текущие изменения");

        buildLayout();
    }

    private void buildLayout() {
        JPanel topRow = new JPanel(new BorderLayout());

        JPanel paramsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        paramsPanel.add(new JLabel("K:"));
        paramsPanel.add(kField);
        paramsPanel.add(new JLabel("N:"));
        paramsPanel.add(nSpinner);
        paramsPanel.add(new JLabel("M:"));
        paramsPanel.add(mSpinner);
        paramsPanel.add(new JLabel("M1:"));
        paramsPanel.add(m1Spinner);

        JPanel autoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        autoPanel.add(autoUpdateCheckBox);

        topRow.add(paramsPanel, BorderLayout.WEST);
        topRow.add(autoPanel, BorderLayout.EAST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));

        Dimension buttonSize = new Dimension(120, 30);
        applyButton.setPreferredSize(buttonSize);
        cancelButton.setPreferredSize(buttonSize);
        defaultButton.setPreferredSize(buttonSize);
        closeButton.setPreferredSize(buttonSize);

        buttonsPanel.add(applyButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(defaultButton);
        buttonsPanel.add(closeButton);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(topRow);
        content.add(Box.createVerticalStrut(6));
        content.add(buttonsPanel);

        add(content, BorderLayout.CENTER);
    }

    public int getNValue() {
        return (Integer) nSpinner.getValue();
    }

    public int getMValue() {
        return (Integer) mSpinner.getValue();
    }

    public int getM1Value() {
        return (Integer) m1Spinner.getValue();
    }

    public boolean isAutoUpdateEnabled() {
        return autoUpdateCheckBox.isSelected();
    }

    public void setValues(int k, int n, int m, int m1) {
        updatingFromCode = true;

        kField.setText(String.valueOf(k));
        nSpinner.setValue(n);
        mSpinner.setValue(m);
        m1Spinner.setValue(m1);

        updatingFromCode = false;
    }

    public void setKValue(int k) {
        kField.setText(String.valueOf(k));
    }

    public boolean isUpdatingFromCode() {
        return updatingFromCode;
    }

    public void addCommonParametersChangeListener(ChangeListener listener) {
        nSpinner.addChangeListener(listener);
        mSpinner.addChangeListener(listener);
        m1Spinner.addChangeListener(listener);
    }

    public void addApplyAction(Runnable action) {
        applyButton.addActionListener(e -> action.run());
    }

    public void addCloseAction(Runnable action) {
        closeButton.addActionListener(e -> action.run());
    }

    public void addCancelAction(Runnable action) {
        cancelButton.addActionListener(e -> action.run());
    }

    public void addDefaultAction(Runnable action) {
        defaultButton.addActionListener(e -> action.run());
    }

    public boolean commitValuesOrShowError(Component parent) {
        return commitSpinnerOrShowError(nSpinner, "N", 1, 100, parent)
                && commitSpinnerOrShowError(mSpinner, "M", 2, 100, parent)
                && commitSpinnerOrShowError(m1Spinner, "M1", 1, 30, parent);
    }

    private boolean commitSpinnerOrShowError(
            JSpinner spinner,
            String name,
            int min,
            int max,
            Component parent
    ) {
        int previousValue = (Integer) spinner.getValue();
        String text = getSpinnerText(spinner);

        int value;

        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            showRangeError(parent, name, min, max);
            setSpinnerSilently(spinner, previousValue);
            return false;
        }

        if (value < min || value > max) {
            showRangeError(parent, name, min, max);
            setSpinnerSilently(spinner, previousValue);
            return false;
        }

        setSpinnerSilently(spinner, value);
        return true;
    }

    private void setSpinnerSilently(JSpinner spinner, int value) {
        updatingFromCode = true;

        spinner.setValue(value);

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setText(String.valueOf(value));
        }

        updatingFromCode = false;
    }

    private String getSpinnerText(JSpinner spinner) {
        JComponent editor = spinner.getEditor();

        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            return defaultEditor.getTextField().getText().trim();
        }

        return String.valueOf(spinner.getValue()).trim();
    }

    private void showRangeError(Component parent, String name, int min, int max) {
        JOptionPane.showMessageDialog(
                parent,
                "Некорректное значение параметра " + name + ".\n" +
                        "Допустимый диапазон: от " + min + " до " + max + ".",
                "Ошибка ввода",
                JOptionPane.ERROR_MESSAGE
        );
    }
}