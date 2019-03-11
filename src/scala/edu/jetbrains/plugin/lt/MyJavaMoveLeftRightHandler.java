package scala.edu.jetbrains.plugin.lt;

import com.intellij.codeInsight.editorActions.moveLeftRight.JavaMoveLeftRightHandler;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class MyJavaMoveLeftRightHandler extends JavaMoveLeftRightHandler {
    @NotNull
    @Override
    public PsiElement[] getMovableSubElements(@NotNull PsiElement element) {
        if (element instanceof PsiExpressionStatement) {
            PsiElement[] result = getChildrenOfType(element, PsiExpressionList.class);
            if (result != null) return result;
        }
        return super.getMovableSubElements(element);
    }

    @Nullable
    private static <T extends PsiElement> T[] getChildrenOfType(@Nullable PsiElement element, @NotNull Class<T> aClass) {
        if (element == null) return null;
        Collection<T> result = PsiTreeUtil.findChildrenOfType(element, aClass);
        return result.isEmpty() ? null : ArrayUtil.toObjectArray(result, aClass);
    }
}
