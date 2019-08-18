package scala.edu.jetbrains.plugin.lt;

import com.intellij.codeInsight.template.impl.LiveTemplateSettingsEditor;
import com.intellij.codeInsight.template.impl.LiveTemplatesConfigurable;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.codeInsight.template.impl.TemplateListPanel;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CheckboxTree;
import com.intellij.util.ui.tree.TreeModelAdapter;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;

public class MyLiveTemplatesConfigurable extends LiveTemplatesConfigurable {
    private TemplateListPanel myPanel;

    @Override
    public JComponent createComponent() {
        myPanel = (TemplateListPanel) super.createComponent();
        try {
            Field myTreeField = TemplateListPanel.class.getDeclaredField("myTree");
            myTreeField.setAccessible(true);
            CheckboxTree myTreeValue = (CheckboxTree) myTreeField.get(myPanel);
            myTreeValue.getModel().addTreeModelListener(new TreeModelAdapter() {
                @Override
                protected void process(@NotNull TreeModelEvent event, @NotNull EventType type) {
                    try {
                        Field myCurrentTemplateEditorField =
                                TemplateListPanel.class.getDeclaredField("myCurrentTemplateEditor");
                        myCurrentTemplateEditorField.setAccessible(true);
                        LiveTemplateSettingsEditor myCurrentTemplateEditorValue =
                                (LiveTemplateSettingsEditor) myCurrentTemplateEditorField.get(
                                        myPanel);
                        try {
                            Field myTemplateEditorField =
                                    LiveTemplateSettingsEditor.class.getDeclaredField(
                                            "myTemplateEditor");
                            myTemplateEditorField.setAccessible(true);
                            Editor myTemplateEditorValue = (Editor) myTemplateEditorField.get(
                                    myCurrentTemplateEditorValue);

                            myTemplateEditorValue.getDocument().addDocumentListener(
                                    new DocumentListener() {
                                        @Override
                                        public void documentChanged(@NotNull DocumentEvent e) {
                                            try {
                                                Field myTemplateField =
                                                        LiveTemplateSettingsEditor.class.getDeclaredField(
                                                                "myTemplate");
                                                myTemplateField.setAccessible(true);
                                                TemplateImpl myTemplateValue =
                                                        (TemplateImpl) myTemplateField.get(
                                                                myCurrentTemplateEditorValue);
                                                String templateText =
                                                        myTemplateEditorValue.getDocument().getText();
                                                ArrayList<Variable> variables =
                                                        myTemplateValue.getVariables();
                                                Collections.sort(variables,
                                                        Collections.reverseOrder(
                                                                Comparator.comparing(
                                                                        Variable::getName)));
                                                variables.sort(Comparator.comparingInt(
                                                        variable -> variable.getName().length()));
                                                for (Variable variable : variables) {

                                                    variable.setExpressionString("completeSmart()");
                                                    variable.setExpressionString("complete()");

                                                    templateText = templateText.replaceAll(
                                                            "(?<!\\$)(" + StringUtil.pluralize(
                                                                    variable.getName()) + "|"
                                                                    + StringUtil.capitalize(
                                                                    StringUtil.pluralize(
                                                                            variable.getName()))
                                                                    + "|" + StringUtil.decapitalize(
                                                                    StringUtil.pluralize(
                                                                            variable.getName()))
                                                                    + "|" + variable.getName() + "|"
                                                                    + StringUtil.capitalize(
                                                                    variable.getName()) + "|"
                                                                    + StringUtil.decapitalize(
                                                                    variable.getName())
                                                                    + ")(?!\\$)",
                                                            "\\$" + "$1" + "\\$");
                                                }
                                                myTemplateValue.setString(templateText);
                                            } catch (Exception exc) {
                                                exc.printStackTrace();
                                                throw new IllegalStateException(exc.getMessage());
                                            }
                                        }
                                    });
                        } catch (Exception exc) {
                            exc.printStackTrace();
                            throw new IllegalStateException(exc.getMessage());
                        }
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        throw new IllegalStateException(exc.getMessage());
                    }
                }
            });

        } catch (Exception exc) {
            exc.printStackTrace();
            throw new IllegalStateException(exc.getMessage());
        }
        return myPanel;
    }
}
