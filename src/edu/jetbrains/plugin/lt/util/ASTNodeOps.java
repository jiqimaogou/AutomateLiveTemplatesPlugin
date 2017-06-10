package edu.jetbrains.plugin.lt.util;

import com.intellij.lang.ASTNode;

import java.util.Arrays;

public final class ASTNodeOps {
    private ASTNodeOps() {
    }

    public static int nodes(ASTNode astNode) {
        return 1 + Arrays.stream(astNode.getChildren(null)).mapToInt(ASTNodeOps::nodes).sum();
    }

    public static int depth(ASTNode astNode) {
        return 1 + Arrays.stream(astNode.getChildren(null)).mapToInt(ASTNodeOps::depth).max().orElse(0);
    }
}
