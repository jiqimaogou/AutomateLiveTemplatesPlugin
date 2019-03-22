package scala.edu.jetbrains.plugin.lt;

import com.intellij.codeInsight.template.impl.LiveTemplatesConfigurable;
import com.intellij.codeInsight.template.impl.TemplateListPanel;
import com.intellij.ui.CheckboxTree;

import java.lang.reflect.Field;

import javax.swing.JComponent;

public class MyLiveTemplatesConfigurable extends LiveTemplatesConfigurable {
    private TemplateListPanel myPanel;

    @Override
    public JComponent createComponent() {
        myPanel = (TemplateListPanel) super.createComponent();
        try {
            Field myTreeField = TemplateListPanel.class.getDeclaredField("myTree");
            myTreeField.setAccessible(true);
            CheckboxTree myTreeValue = (CheckboxTree) myTreeField.get(myPanel);
        } catch (Exception exc) {
            exc.printStackTrace();
            throw new IllegalStateException(exc.getMessage());
        }
        return myPanel;
    }
}
