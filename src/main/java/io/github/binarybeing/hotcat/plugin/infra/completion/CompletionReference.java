package io.github.binarybeing.hotcat.plugin.infra.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompletionReference extends PsiReferenceBase<PsiElement> {

    private PsiElement element;
    public CompletionReference(@NotNull PsiElement element) {
        super(element);
        this.element = element;
    }

    @Override
    public @Nullable PsiElement resolve() {
        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return new Object[]{
                new LookupElement() {
                    @Override
                    public @NotNull String getLookupString() {
                        return "this is my test";
                    }
                }
        };
    }
}
