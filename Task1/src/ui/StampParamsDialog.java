package ui;

import model.StampParams;

import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import javax.swing.JSlider;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class StampParamsDialog {
    public static boolean show(Component parent, StampParams params) {
        int originalSides = params.getSides();

        JTextField sidesField = new JTextField(String.valueOf(params.getSides()), 6);

        JTextField radiusField = new JTextField(String.valueOf(params.getRadius()), 6);
        JSlider radiusSlider = new JSlider(JSlider.HORIZONTAL, 1, 301, params.getRadius());
        radiusSlider.setPaintTicks(true);
        radiusSlider.setMajorTickSpacing(50);
        radiusSlider.setMinorTickSpacing(10);
        radiusSlider.setPaintLabels(true);

        JTextField rotField = new JTextField(String.valueOf(params.getRotationDeg()), 6);
        JSlider rotSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, params.getRotationDeg());
        rotSlider.setPaintTicks(true);
        rotSlider.setMajorTickSpacing(90);
        rotSlider.setMinorTickSpacing(15);
        rotSlider.setPaintLabels(true);

        JCheckBox starCheck = new JCheckBox("Звезда", params.isStar());

        //синхронизация полей и слайдеров
        //когда двигаем слайдер, число в поле обновляется автоматически
        radiusSlider.addChangeListener(e ->
                radiusField.setText(String.valueOf(radiusSlider.getValue()))
        );

        radiusField.addActionListener(e ->
                validateAndUpdateField(radiusField, radiusSlider, 1, 301, parent, "Радиус")
        );

        rotSlider.addChangeListener(e ->
                rotField.setText(String.valueOf(rotSlider.getValue()))
        );

        rotField.addActionListener(e ->
                validateAndUpdateField(rotField, rotSlider, 0, 360, parent, "Поворот")
        );

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("Стороны (3-16):"), c);
        c.gridx = 1;
        panel.add(sidesField, c);

        c.gridx = 0; c.gridy = 1;
        panel.add(new JLabel("Радиус (1-301):"), c);
        c.gridx = 1;
        panel.add(radiusField, c);
        c.gridx = 2;
        panel.add(radiusSlider, c);

        c.gridx = 0; c.gridy = 2;
        panel.add(new JLabel("Поворот (0-360):"), c);
        c.gridx = 1;
        panel.add(rotField, c);
        c.gridx = 2;
        panel.add(rotSlider, c);

        c.gridx = 0; c.gridy = 3;
        c.gridwidth = 2;
        panel.add(starCheck, c);
        c.gridwidth = 1;

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    parent, panel, "Параметры штампа",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (result != JOptionPane.OK_OPTION) return false;

            try {
                int sides = Integer.parseInt(sidesField.getText().trim());
                int radius = Integer.parseInt(radiusField.getText().trim());
                int rot = Integer.parseInt(rotField.getText().trim());

                if (sides < 3 || sides > 16) {
                    showErr(parent, "Число сторон должно быть от 3 и до 16");
                    sidesField.setText(String.valueOf(originalSides));
                    continue;
                }
                if (radius < 1 || radius > 301) {
                    showErr(parent, "Радиус должен быть от 1 до 301");
                    radiusField.setText(String.valueOf(radiusSlider.getValue()));
                    continue;
                }
                if (rot < 0 || rot > 360) {
                    showErr(parent, "Поворот должен быть от 0 до 360°");
                    rotField.setText(String.valueOf(rotSlider.getValue()));
                    continue;
                }

                params.setSides(sides);
                params.setRadius(radius);
                params.setRotationDeg(rot);
                params.setStar(starCheck.isSelected());
                return true;

            } catch (Exception e) {
                showErr(parent, "Введите корректные числа");
                sidesField.setText(String.valueOf(originalSides));
                radiusField.setText(String.valueOf(radiusSlider.getValue()));
                rotField.setText(String.valueOf(rotSlider.getValue()));
            }
        }
    }

    private static void validateAndUpdateField(JTextField field, JSlider slider, int min, int max, Component parent, String fieldName) {
        try {
            String text = field.getText().trim();
            if (text.isEmpty()) {
                showErr(parent, "Поле \"" + fieldName + "\" не может быть пустым");
                field.setText(String.valueOf(slider.getValue()));
                return;
            }

            int value = Integer.parseInt(text);
            if (value >= min && value <= max) {
                slider.setValue(value);  //обновляем слайдер
            } else {
                showErr(parent, fieldName + " должен быть от " + min + " до " + max);
                field.setText(String.valueOf(slider.getValue()));
            }
        } catch (Exception e) {
            showErr(parent, "Введите целое число для поля \"" + fieldName + "\"");
            field.setText(String.valueOf(slider.getValue()));
        }
    }

    private static void showErr(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
    }
}