package edu.jetbrains.plugin.lt.util;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.codeInsight.template.impl.TemplateContext;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import scala.edu.jetbrains.plugin.lt.finder.common.Template;

public final class TemplateOps {
    private TemplateOps() {
    }

    public static void saveTemplate(Template template, String abbreviation, String description) {
        TemplateImpl templateToSave = new TemplateImpl(abbreviation, TemplateSettings.USER_GROUP_NAME);
        String[] textParts = template.text().split("#_#");
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= textParts.length; ++i) {
            builder.append(textParts[i - 1]);
            builder.append(i == textParts.length ? "$END$" : "$VAR" + i + "$");
        }
        templateToSave.setString(builder.toString());
        templateToSave.setDescription(description);
        templateToSave.setToReformat(true);
        TemplateContext context = new TemplateContext();
        for (TemplateContextType contextType : TemplateContextType.EP_NAME.getExtensions()) {
            context.setEnabled(contextType, true);
        }
        templateToSave.applyContext(context);
        templateToSave.parseSegments();
        templateToSave.setToShortenLongNames(false);
        TemplateSettings.getInstance().addTemplate(templateToSave);
        TemplateSettings.getInstance().reset();
    }

    public static boolean isPossibleAbbreviation(String abbreviation) {
        return TemplateSettings.getInstance().getTemplates(abbreviation).isEmpty();
    }
}
