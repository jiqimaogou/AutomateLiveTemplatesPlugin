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

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.internal.psiView.PsiViewerDialog;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ShortcutSet;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

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
                ActionPlaces.UNKNOWN, group);
        menu.getComponent().show(component, x, y);
    }


    private abstract class TreeSelectionAction extends DumbAwareAction {
        private TreeSelectionAction(@Nullable String text) {
            super(text);
        }

        private TreeSelectionAction(@Nullable String text, @Nullable String description,
                @Nullable Icon icon) {
            super(text, description, icon);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(true);
            TreePath[] selectionPaths = myPsiTreeValue.getSelectionPaths();
            if (selectionPaths == null) {
                e.getPresentation().setEnabled(false);
                return;
            }
            for (TreePath path : selectionPaths) {
                if (path.getPath().length <= 2) {
                    e.getPresentation().setEnabled(false);
                    return;
                }
            }
        }

        protected final boolean isSingleSelection() {
            final TreePath[] selectionPaths = myPsiTreeValue.getSelectionPaths();
            return selectionPaths != null && selectionPaths.length == 1;
        }
    }


    private class RemoveAction extends TreeSelectionAction {
        private RemoveAction() {
            super(IdeBundle.message("button.remove"), null, AllIcons.General.Remove);
            ShortcutSet shortcutSet = KeymapUtil.filterKeyStrokes(CommonShortcuts.getDelete(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                    KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
            if (shortcutSet != null) {
                registerCustomShortcutSet(shortcutSet, myPanel);
            }
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            final List<TreePath> expandedPaths = TreeUtil.collectExpandedPaths(myActionsTree);
            final TreePath[] selectionPath = myActionsTree.getSelectionPaths();
            if (selectionPath != null) {
                for (TreePath treePath : selectionPath) {
                }
                ((DefaultTreeModel)myActionsTree.getModel()).reload();
            }
            TreeUtil.restoreExpandedPaths(myActionsTree, expandedPaths);
        }
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