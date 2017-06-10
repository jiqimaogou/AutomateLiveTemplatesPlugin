package edu.jetbrains.plugin.lt.newui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JCheckBoxList extends JList<JCheckBox> {
    private static Border noFocusBorder = new EmptyBorder(1, 0, 1, 1);

    public JCheckBoxList() {
        setCellRenderer(new JCheckBoxCellRenderer());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    JCheckBox checkBox = getModel().getElementAt(index);
                    checkBox.setSelected(!checkBox.isSelected());
                    repaint();
                }
                super.mouseClicked(e);
            }
        });
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public JCheckBoxList(ListModel<JCheckBox> model) {
        this();
        setModel(model);
    }

    private class JCheckBoxCellRenderer implements ListCellRenderer<JCheckBox> {



        @Override
        public Component getListCellRendererComponent(JList<? extends JCheckBox> list,
                                                      JCheckBox checkBox,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {

            checkBox.setBackground(getBackground());
            checkBox.setForeground(getForeground());
            checkBox.setEnabled(isEnabled());
            checkBox.setFont(getFont());
            checkBox.setFocusPainted(false);
            checkBox.setBorderPainted(true);
            checkBox.setBorder(noFocusBorder);
            return checkBox;
        }
    }

}
