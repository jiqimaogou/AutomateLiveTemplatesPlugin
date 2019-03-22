package scala.edu.jetbrains.plugin.lt;

import com.intellij.codeInsight.template.impl.LiveTemplatesConfigurable;
import com.intellij.codeInsight.template.impl.TemplateListPanel;

import javax.swing.JComponent;

public class MyLiveTemplatesConfigurable extends LiveTemplatesConfigurable {
    private TemplateListPanel myPanel;

    @Override
    public JComponent createComponent() {
        myPanel = (TemplateListPanel) super.createComponent();
        return myPanel;
    }
}
