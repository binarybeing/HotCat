package io.github.binarybeing.hotcat.plugin.infra.reference;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiComment;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.Nullable;

public class HotCatElementPatter extends PsiElementPattern.Capture<PsiComment> {

    protected HotCatElementPatter(Class<PsiComment> aClass) {
        super(aClass);
    }

    @Override
    public boolean accepts(@Nullable Object o, ProcessingContext context) {
        if (o instanceof PsiComment) {
            return o instanceof PsiComment && ((PsiComment)o).getText().startsWith("//ht:");
        }
        if (o instanceof PsiDocToken) {

        }
        if (o instanceof JavaTokenType) {
            JavaTokenType tokenType = (JavaTokenType) o;

        }
        if (o instanceof LeafPsiElement){

        }
        return false;
    }
    //PsiLiteralExpression
}
