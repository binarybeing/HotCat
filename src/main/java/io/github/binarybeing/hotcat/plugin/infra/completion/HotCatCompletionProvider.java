package io.github.binarybeing.hotcat.plugin.infra.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.util.Consumer;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HotCatCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition();
        if (!(element instanceof PsiIdentifier)){
            return;
        }
        if (!(element.getPrevSibling() instanceof PsiReferenceParameterList)) {
            return;
        }
        if (!(element.getPrevSibling().getPrevSibling() instanceof PsiJavaToken)
                || !(Objects.equals(element.getPrevSibling().getPrevSibling().getText(),"."))) {
            return;
        }
        //todo dingzhi
        PsiJavaToken dotTokenElement = (PsiJavaToken) element.getPrevSibling().getPrevSibling();
        if (!((dotTokenElement.getPrevSibling()) instanceof PsiReferenceExpression)) {
            return;
        }
        PsiReferenceExpression reference = (PsiReferenceExpression) dotTokenElement.getPrevSibling();
        PsiElement psiElement = reference.resolve();
        if (!(psiElement instanceof PsiLocalVariable)) {
            PooledByteBufAllocator allocator = new PooledByteBufAllocator(true);
            ByteBuf buffer = allocator.buffer();
            String name = "field1";
            Map<Class<?>, Integer> map = new HashMap<>();

            buffer.writeByte(1);
            return;
        }
        PsiLocalVariable variable = (PsiLocalVariable) psiElement;
        String name = variable.getName();
        result.addElement(PrioritizedLookupElement.withPriority(new LookupElement() {
            @Override
            public @NotNull String getLookupString() {
                return "";
            }

            @Override
            public void renderElement(LookupElementPresentation presentation) {
                presentation.setIcon(PlatformIcons.VARIABLE_ICON);
                presentation.setTypeText("type text");
                presentation.setItemText("item text");
            }

            @Override
            public void handleInsert(@NotNull InsertionContext context) {
                super.handleInsert(context);
                Editor editor = context.getEditor();
                editor.getDocument().deleteString(reference.getTextOffset(),
                        dotTokenElement.getTextOffset() + 1);
                String insert = "Long a = (Long) map.get(\"test\");";
                editor.getDocument().insertString(reference.getTextOffset(), insert);
                editor.getSelectionModel().setSelection(reference.getTextOffset(),
                                                        reference.getTextOffset() + insert.length());

            }
        }, 99999999999.0));

        result.runRemainingContributors(parameters, new Consumer<CompletionResult>() {
            @Override
            public void consume(CompletionResult completionResult) {
                result.addElement(completionResult.getLookupElement());
            }
        });
    }
}
