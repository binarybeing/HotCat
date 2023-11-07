package io.github.binarybeing.hotcat.plugin.infra.hint;

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.codeInsight.hints.InlayPresentationFactory;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HotCatFactoryInlayHintsCollector extends FactoryInlayHintsCollector {

    private static Map<String, Pair<String, String>> listenerMap = new ConcurrentHashMap<>();
    static {
        listenerMap.put("__open_baidu", Pair.of("百度", "https://www.baidu.com"));
        listenerMap.put("__open_baibu", Pair.of("百度", null));
    }

    public static void register(String text, String hint, String jumpUrl){
        listenerMap.put(text, Pair.of(hint, jumpUrl));
    }

    public static void unregister(String text){
        listenerMap.remove(text);
    }

    public HotCatFactoryInlayHintsCollector(@NotNull Editor editor) {
        super(editor);
    }


    @Override
    public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
        Collection<PsiJavaToken> tokens = PsiTreeUtil.collectElementsOfType(psiElement, PsiJavaToken.class);

        boolean ans = false;
        for (PsiJavaToken token : tokens) {
            String text = token.getText();
            if (!text.startsWith("\"") || !text.endsWith("\"")) {
                continue;
            }
            text = text.substring(1, text.length() - 1);
            Pair<String, String> hintAndJump = listenerMap.get(text);
            if (hintAndJump == null || StringUtils.isBlank(hintAndJump.getKey())) {
                continue;
            }
            String hint = hintAndJump.getKey();

            if (hint.length() > 200) {
                hint = hint.substring(0, 200);
            }

            String shortHint = hint;
            if (shortHint.length() > 5) {
                shortHint = shortHint.substring(0, 6) + " ...";
            }
            int offset = token.getTextOffset() + text.length() + 2;
            PresentationFactory factory = getFactory();
            InlayPresentation presentation = factory.smallTextWithoutBackground(shortHint);
            InlayPresentationFactory.Padding padding = new InlayPresentationFactory.Padding(5, 0, 6, 0);
            InlayPresentationFactory.RoundedCorners corners = new InlayPresentationFactory.RoundedCorners(4, 4);
            presentation = factory.container(presentation, padding, corners, Color.darkGray, 0);
            presentation = factory.withTooltip(hint, presentation);
            if (StringUtils.isNotBlank(hintAndJump.getRight())) {
                presentation = factory.referenceOnHover(presentation, (event, point)->{
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        try {
                            Desktop.getDesktop().browse(new URI(hintAndJump.getRight()));
                        } catch (Exception e) {
                            LogUtils.addError(e, "open url error:"+ hintAndJump.getRight());
                        }
                    }
                });
            }

            inlayHintsSink.addInlineElement(offset, false, presentation);
            ans = true;
        }
        return ans;
    }
}
