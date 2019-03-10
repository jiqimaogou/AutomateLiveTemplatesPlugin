package scala.edu.jetbrains.plugin.lt;

import com.google.common.collect.ObjectArrays;
import com.intellij.codeInsight.unwrap.JavaMethodParameterUnwrapper;
import com.intellij.codeInsight.unwrap.JavaUnwrapDescriptor;
import com.intellij.codeInsight.unwrap.Unwrapper;

public class MyJavaUnwrapDescriptor extends JavaUnwrapDescriptor {
    @Override
    protected Unwrapper[] createUnwrappers() {
        return ObjectArrays.concat(new MyJavaMethodParameterUnwrapper(), super.createUnwrappers());
    }
}
