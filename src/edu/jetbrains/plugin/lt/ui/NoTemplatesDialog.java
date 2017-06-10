package edu.jetbrains.plugin.lt.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class NoTemplatesDialog extends DialogWrapper {
    public NoTemplatesDialog(Project project) {
        super(project);
        setTitle("Templates in " + project.getName());
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel jPanel = new JBPanel<>(new BorderLayout());
        jPanel.setMinimumSize(new Dimension(300, 100));
        JBLabel jLabel = new JBLabel("No Templates");
        jLabel.setHorizontalAlignment(SwingConstants.CENTER);
        jPanel.add(jLabel, BorderLayout.CENTER);
        return jPanel;
    }
}
