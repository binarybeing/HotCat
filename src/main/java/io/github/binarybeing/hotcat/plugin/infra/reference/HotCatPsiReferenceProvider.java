package io.github.binarybeing.hotcat.plugin.infra.reference;

import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class HotCatPsiReferenceProvider extends PsiReferenceProvider {

    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
        String text = null;
        PsiReference[] references = null;
        if(element instanceof PsiComment && element.getText().startsWith("//ht:")){
            references = new PsiReference[]{new HotCatReference(element),
                    element.getParent().getReference()};
        }
        return references;
    }


}
