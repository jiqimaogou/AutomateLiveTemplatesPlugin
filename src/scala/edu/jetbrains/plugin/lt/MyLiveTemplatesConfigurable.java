package scala.edu.jetbrains.plugin.lt;

import com.intellij.codeInsight.template.impl.LiveTemplatesConfigurable;
import com.intellij.codeInsight.template.impl.TemplateListPanel;
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
                }
            });

        } catch (Exception exc) {
            exc.printStackTrace();
            throw new IllegalStateException(exc.getMessage());
        }
        return myPanel;
    }
}
