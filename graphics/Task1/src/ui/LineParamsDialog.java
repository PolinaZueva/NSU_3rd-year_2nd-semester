package ui;

import model.LineParams;

import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Component;
import java.awt.GridBagLayout;  //менеджер расположения
import java.awt.GridBagConstraints;  //определяет, как именно элемент располагается в GridBagLayout
import java.awt.Insets;  //отступы

public class LineParamsDialog {
    public static boolean show(Component parent, LineParams params) {
        JTextField thicknessField = new JTextField(String.valueOf(params.getThickness()), 5);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Толщина линии (1-50):"), c);

        c.gridx = 1;
        c.gridy = 0;
        panel.add(thicknessField, c);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    parent,
                    panel,
                    "Параметры линии",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return false;
            }

            try {
                int thickness = Integer.parseInt(thicknessField.getText().trim());

                if (thickness < 1 || thickness > 50) {
                    showErr(parent, "Толщина должна быть от 1 до 50");
                    thicknessField.setText(String.valueOf(params.getThickness()));
                    continue;
                }

                params.setThickness(thickness);
                return true;

            } catch (NumberFormatException e) {
                showErr(parent, "Некорректное число");
                thicknessField.setText(String.valueOf(params.getThickness()));
            }
        }
    }

    private static void showErr(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
    }
}
