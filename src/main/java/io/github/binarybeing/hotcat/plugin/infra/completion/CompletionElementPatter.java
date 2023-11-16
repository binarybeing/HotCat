package io.github.binarybeing.hotcat.plugin.infra.completion;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.Nullable;

public class CompletionElementPatter extends PsiElementPattern.Capture<LeafPsiElement> {

    protected CompletionElementPatter(Class<LeafPsiElement> aClass) {
        super(aClass);
    }

    @Override
    public boolean accepts(@Nullable Object o, ProcessingContext context) {
        if (o instanceof PsiIdentifier) {
            return true;
        }
        return false;
    }
    //PsiLiteralExpression
}
