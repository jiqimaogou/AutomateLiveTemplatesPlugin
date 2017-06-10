package edu.jetbrains.plugin.lt.newui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import scala.edu.jetbrains.plugin.lt.AutomaticModeSettings;
import scala.edu.jetbrains.plugin.lt.finder.sstree.TemplateSearchConfiguration;
import scala.edu.jetbrains.plugin.lt.finder.sstree.TemplateSearchConfigurationImpl;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

public class ChooseSettings extends JDialog {
    private JPanel contentPane;
    private JRadioButton manualSettingsRadioButton;
    private JRadioButton automaticParametersRadioButton;
    private JFormattedTextField minSupportFormattedTextField;
    private JFormattedTextField lengthMaximumFormattedTextField;
    private JFormattedTextField lengthMinimumFormattedTextField;
    private JFormattedTextField placeholderRatioFormattedTextField;
    private JFormattedTextField nodesMaximumFormattedTextField;
    private JFormattedTextField nodesMinimumFormattedTextField;
    private JFormattedTextField placeholderMaximumFormattedTextField;
    private JButton okButton;
    private JFormattedTextField templateCountFormattedTextField;

    private RunModeSettings result;

    public ChooseSettings() {
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(okButton);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        ButtonGroup group = new ButtonGroup();
        group.add(manualSettingsRadioButton);
        group.add(automaticParametersRadioButton);

        manualSettingsRadioButton.addChangeListener(e -> setManualTrue(manualSettingsRadioButton.isSelected()));
        manualSettingsRadioButton.setSelected(true);

        okButton.addActionListener(e -> onOk());
    }

    private void onOk() {
        result = getResult();
        validateParams();
        dispose();
    }

    private void validateParams() {
        if (result.automaticModeSettings != null) {
            if (result.automaticModeSettings.desiredTemplateCount() <= 0)
                showErrorDialog("Desired template count must be positive!");
        }
        if (result.templateSearchConfiguration != null) {
            if (result.templateSearchConfiguration.lengthMaximum() < result.templateSearchConfiguration.lengthMinimum())
                showErrorDialog("Maximum text length must be more or equals minimum length");
            if (result.templateSearchConfiguration.nodesMaximum() < result.templateSearchConfiguration.nodesMinimum())
                showErrorDialog("Maximum node count must be more or equals minimum node count");
            if (result.templateSearchConfiguration.placeholderMaximum() < 0)
                showErrorDialog("Placeholder must be great or equals zero");
            if (result.templateSearchConfiguration.maxPlaceholderToNodeRatio() < 0 || result.templateSearchConfiguration.maxPlaceholderToNodeRatio() > 1)
                showErrorDialog("Max placeholder to node ratio must be between 0 and 1");
            if (result.templateSearchConfiguration.nodesMinimum() < 1)
                showErrorDialog("Nodes minimum must be greater than zero");
            if (result.templateSearchConfiguration.nodesMaximum() < 1)
                showErrorDialog("Nodes maximum must be greater than zero");
            if (result.templateSearchConfiguration.lengthMinimum() < 1)
                showErrorDialog("Length minimum must be greater than zero");
            if (result.templateSearchConfiguration.lengthMaximum() < 1)
                showErrorDialog("Length maximum must be greater than zero");
            if (result.minSupport <= 0)
                showErrorDialog("Minimum support must be positive");
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private RunModeSettings getResult() {
        if (manualSettingsRadioButton.isSelected()) {
            return new RunModeSettings(Double.parseDouble(minSupportFormattedTextField.getText().replace(",", ".")),
                    new TemplateSearchConfigurationImpl(
                            Integer.parseInt(lengthMinimumFormattedTextField.getText()),
                            Integer.parseInt(lengthMaximumFormattedTextField.getText()),
                            Integer.parseInt(placeholderMaximumFormattedTextField.getText()),
                            Integer.parseInt(nodesMinimumFormattedTextField.getText()),
                            Integer.parseInt(nodesMaximumFormattedTextField.getText()),
                            Double.parseDouble(placeholderRatioFormattedTextField.getText().replace(",", "."))
                    ),
                    null);
        } else {
            return new RunModeSettings(0, null,
                    new AutomaticModeSettings(Integer.parseInt(templateCountFormattedTextField.getText())));
        }
    }

    private void setManualTrue(boolean value) {
        minSupportFormattedTextField.setEnabled(value);
        lengthMinimumFormattedTextField.setEnabled(value);
        lengthMaximumFormattedTextField.setEnabled(value);
        placeholderMaximumFormattedTextField.setEnabled(value);
        placeholderRatioFormattedTextField.setEnabled(value);
        nodesMaximumFormattedTextField.setEnabled(value);
        nodesMinimumFormattedTextField.setEnabled(value);
        templateCountFormattedTextField.setEnabled(!value);
    }

    private void createUIComponents() {
        NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
        integerFieldFormatter.setMaximumFractionDigits(0);

        minSupportFormattedTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
        lengthMaximumFormattedTextField = new JFormattedTextField(integerFieldFormatter);
        lengthMinimumFormattedTextField = new JFormattedTextField(integerFieldFormatter);
        placeholderRatioFormattedTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
        nodesMaximumFormattedTextField = new JFormattedTextField(integerFieldFormatter);
        nodesMinimumFormattedTextField = new JFormattedTextField(integerFieldFormatter);
        placeholderMaximumFormattedTextField = new JFormattedTextField(integerFieldFormatter);
        templateCountFormattedTextField = new JFormattedTextField(integerFieldFormatter);

        minSupportFormattedTextField.setValue(3.0);
        lengthMaximumFormattedTextField.setValue(100);
        lengthMinimumFormattedTextField.setValue(15);
        placeholderMaximumFormattedTextField.setValue(5);
        nodesMaximumFormattedTextField.setValue(50);
        nodesMinimumFormattedTextField.setValue(3);
        placeholderRatioFormattedTextField.setValue(0.33);
        templateCountFormattedTextField.setValue(50);
    }

    public RunModeSettings showDialog() {
        pack();
        setVisible(true);
        return result;
    }


    public static void main(String[] args) {
        new ChooseSettings().showDialog();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.setMaximumSize(new Dimension(545, 308));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        manualSettingsRadioButton = new JRadioButton();
        manualSettingsRadioButton.setText("Manual settings");
        panel1.add(manualSettingsRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        automaticParametersRadioButton = new JRadioButton();
        automaticParametersRadioButton.setText("Automatic parameters");
        panel1.add(automaticParametersRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        contentPane.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(10, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.add(minSupportFormattedTextField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel2.add(spacer4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel2.add(spacer5, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panel2.add(lengthMaximumFormattedTextField, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("MinSupport");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Length maximum");
        panel2.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel2.add(lengthMinimumFormattedTextField, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        panel2.add(placeholderRatioFormattedTextField, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        panel2.add(nodesMaximumFormattedTextField, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        panel2.add(nodesMinimumFormattedTextField, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        panel2.add(placeholderMaximumFormattedTextField, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Length minimum");
        panel2.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Placeholder ratio");
        panel2.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Nodes maximum");
        panel2.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Nodes minimum");
        panel2.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Placeholder maximum");
        panel2.add(label7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        okButton = new JButton();
        okButton.setText("Ok");
        panel2.add(okButton, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Template count");
        panel2.add(label8, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel2.add(templateCountFormattedTextField, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    public static final class RunModeSettings {
        public final double minSupport;
        public final TemplateSearchConfiguration templateSearchConfiguration;
        public final AutomaticModeSettings automaticModeSettings;

        public RunModeSettings(double minSupport, TemplateSearchConfiguration templateSearchConfiguration,
                               AutomaticModeSettings automaticModeSettings) {
            this.minSupport = minSupport;
            this.templateSearchConfiguration = templateSearchConfiguration;
            this.automaticModeSettings = automaticModeSettings;
        }
    }

}
