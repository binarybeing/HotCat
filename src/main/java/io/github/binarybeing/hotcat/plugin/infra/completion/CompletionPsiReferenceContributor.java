package io.github.binarybeing.hotcat.plugin.infra.completion;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @this is a test
 *
 */
public class CompletionPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        PsiElementPattern.Capture<LeafPsiElement> pattern = new CompletionElementPatter(LeafPsiElement.class);

        CompletionPsiReferenceProvider provider = new CompletionPsiReferenceProvider();
        //  new feature
        registrar.registerReferenceProvider(pattern, provider);
    }
}
