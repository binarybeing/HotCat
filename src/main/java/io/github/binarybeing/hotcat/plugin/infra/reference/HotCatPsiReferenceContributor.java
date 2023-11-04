package io.github.binarybeing.hotcat.plugin.infra.reference;

import com.intellij.patterns.ObjectPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @this is a test
 *
 */
public class HotCatPsiReferenceContributor extends PsiReferenceContributor {
    private static PsiReferenceRegistrar registrar;

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        HotCatPsiReferenceContributor.registrar = registrar;
        PsiElementPattern.Capture<PsiComment> pattern = new HotCatElementPatter(PsiComment.class);

        HotCatPsiReferenceProvider provider = new HotCatPsiReferenceProvider();


        //  new feature
        registrar.registerReferenceProvider(pattern, provider);
    }
}
