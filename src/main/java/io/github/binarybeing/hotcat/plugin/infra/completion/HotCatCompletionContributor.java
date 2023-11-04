package io.github.binarybeing.hotcat.plugin.infra.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.impl.source.tree.LeafPsiElement;

public class HotCatCompletionContributor extends CompletionContributor {

    public HotCatCompletionContributor() {
        PlatformPatterns.psiElement(JavaTokenType.STRING_LITERAL);
        super.extend(null, new CompletionElementPatter(LeafPsiElement.class), new HotCatCompletionProvider());
    }
}
