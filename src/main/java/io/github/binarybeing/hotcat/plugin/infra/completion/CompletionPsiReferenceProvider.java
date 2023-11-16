package io.github.binarybeing.hotcat.plugin.infra.completion;

import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class CompletionPsiReferenceProvider extends PsiReferenceProvider {

    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
        return new PsiReference[]{new CompletionReference(element)};
    }


}
