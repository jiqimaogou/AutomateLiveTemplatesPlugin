package scala.edu.jetbrains.plugin.lt;

import com.intellij.codeInsight.unwrap.JavaMethodParameterUnwrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.IncorrectOperationException;

public class MyJavaMethodParameterUnwrapper extends JavaMethodParameterUnwrapper {
    @Override
    protected void doUnwrap(PsiElement element, Context context) throws IncorrectOperationException {
        PsiElement parent = element.getParent();
        if (parent == null) return;
        // trim leading empty spaces
        PsiElement first = element;
        first = first.getNextSibling();
        while (first instanceof PsiWhiteSpace) {
            context.delete(first);
            first = first.getNextSibling();
        }

        // trim trailing empty spaces
        PsiElement last = element;
        last = last.getPrevSibling();
        while (last instanceof PsiWhiteSpace) {
            context.delete(last);
            last = last.getPrevSibling();
        }
        context.delete(element);
    }
}
