package scala.edu.jetbrains.plugin.lt;

import com.intellij.codeInsight.template.impl.LiveTemplateSettingsEditor;
import com.intellij.codeInsight.template.impl.LiveTemplatesConfigurable;
import com.intellij.codeInsight.template.impl.TemplateListPanel;
import com.intellij.openapi.editor.Editor;
import com.intellij.ui.CheckboxTree;
import com.intellij.util.ui.tree.TreeModelAdapter;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

import javax.swing.JComponent;
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
                                TemplateListPanel.class.getDeclaredField(
                                        "myCurrentTemplateEditor");
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
