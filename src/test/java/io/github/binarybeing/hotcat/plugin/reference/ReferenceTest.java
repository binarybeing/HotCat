package io.github.binarybeing.hotcat.plugin.reference;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.ProcessingContext;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import io.github.binarybeing.hotcat.plugin.infra.reference.HotCatPsiReferenceContributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;

//ht:hotcat

public class ReferenceTest extends BaseTest {
    @Override
    public Object doExecute() throws Exception {
        Class<HotCatPsiReferenceContributor> contributorClass = HotCatPsiReferenceContributor.class;
        Field field = contributorClass.getDeclaredField("registrar");
        field.setAccessible(true);
        PsiReferenceRegistrar registrar = (PsiReferenceRegistrar)field.get(contributorClass);
        PsiElement element = psiFile.findElementAt(editor.getSelectionModel().getSelectionEnd());
//        PsiLiteral type = PsiTreeUtil.findChildOfType(super.psiElement, PsiLiteral.class);
        String a = "ht:java.lang.String";
        Class<?> targetClazz = LeafPsiElement.class;
//        register(registrar, element);


        return element.getClass().getName()+" "+ element.getText() +" "+ element.getReferences().length;
    }

    private void register(PsiReferenceRegistrar registrar, PsiElement element) {
        PsiElementPattern.Capture<PsiComment> pattern = new PsiElementPattern.Capture<>(PsiComment.class){
            @Override
            public boolean accepts(@Nullable Object o, ProcessingContext context) {
                return o instanceof PsiComment && ((PsiComment)o).getText().startsWith("//ht:");
            }
        };
        PsiReference psiReference = new PsiReferenceBase<>(element){
            @Override
            public @Nullable PsiElement resolve() {
                return psiFile;
            }
        };
        PsiReferenceProvider referenceProvider = new PsiReferenceProvider() {
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                return new PsiReference[]{psiReference};
            }
        };
        registrar.registerReferenceProvider(pattern, referenceProvider);
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert code == 200;
    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-09-10";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }
}
