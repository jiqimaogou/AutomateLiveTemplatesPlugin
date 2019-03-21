/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scala.edu.jetbrains.plugin.lt;

import com.intellij.internal.psiView.PsiViewerDialog;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.Tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.lang.reflect.Field;

/**
 * @author Konstantin Bulenkov
 */
public class MyPsiViewerAction extends DumbAwareAction {

    Tree myPsiTreeValue;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = isForContext() ? e.getData(CommonDataKeys.EDITOR) : null;
        PsiViewerDialog psiViewerDialog = new PsiViewerDialog(e.getProject(), editor);
        try {
            Field myPsiTreeField = PsiViewerDialog.class.getDeclaredField("myPsiTree");
            myPsiTreeField.setAccessible(true);
            myPsiTreeValue = (Tree) myPsiTreeField.get(psiViewerDialog);

            myPsiTreeValue.addMouseListener(new PopupHandler() {
                @Override
                public void invokePopup(Component comp, int x, int y) {
                    popupInvoked(comp, x, y);
                }
            });

        } catch (Exception exc) {
            exc.printStackTrace();
            throw new IllegalStateException(exc.getMessage());
        }
        psiViewerDialog.show();
    }

    private void popupInvoked(Component component, int x, int y) {
        DefaultActionGroup group = new DefaultActionGroup();
        ActionPopupMenu menu = ActionManager.getInstance().createActionPopupMenu(
                ActionPlaces.ANT_MESSAGES_POPUP, group);
        menu.getComponent().show(component, x, y);
    }


    //@Override
    //public void update(@NotNull AnActionEvent e) {
    //    boolean enabled = isEnabled(e.getProject());
    //    e.getPresentation().setEnabledAndVisible(enabled);
    //    if (enabled && isForContext() && e.getData(CommonDataKeys.EDITOR) == null) {
    //        e.getPresentation().setEnabled(false);
    //    }
    //}

    protected boolean isForContext() {
        return false;
    }

    private static boolean isEnabled(@Nullable Project project) {
        if (project == null) return false;
        if (ApplicationManagerEx.getApplicationEx().isInternal()) return true;
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            if ("PLUGIN_MODULE".equals(ModuleType.get(module).getId())) {
                return true;
            }
        }
        return false;
    }

    public static class ForContext extends MyPsiViewerAction {

        @Override
        protected boolean isForContext() {
            return true;
        }
    }
}