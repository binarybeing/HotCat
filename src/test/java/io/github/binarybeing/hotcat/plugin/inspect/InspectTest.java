package io.github.binarybeing.hotcat.plugin.inspect;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.*;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import io.github.binarybeing.hotcat.plugin.infra.reference.HotCatElementPatter;
import io.github.binarybeing.hotcat.plugin.infra.reference.HotCatPsiReferenceContributor;
import io.github.binarybeing.hotcat.plugin.infra.reference.HotCatPsiReferenceProvider;
import io.github.binarybeing.hotcat.plugin.infra.reference.HotCatReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InspectTest extends BaseTest {
    @Override
    public Object doExecute() throws Exception {
        LookupManager instance = LookupManager.getInstance(project);


        Map<String, Object> map = new HashMap<>();
        for (PsiReference reference : psiElement.getReferences()) {
            if (reference instanceof HotCatReference) {
                return "success reference";
            }
        }
        Collection<PsiIdentifier> ofType = PsiTreeUtil.findChildrenOfType(psiElement, PsiIdentifier.class);
        for (PsiIdentifier identifier : ofType) {
            if (identifier.getText().equals("map")) {
                for (PsiReference reference : identifier.getReferences()) {
                    if (reference instanceof HotCatReference) {
                        return "success referenced hotcat";
                    }
                }

            }
        }
        if (map.isEmpty()) {
            return psiElement.getText();
        }



        Class<HotCatPsiReferenceContributor> contributorClass = HotCatPsiReferenceContributor.class;
        Field field = contributorClass.getDeclaredField("registrar");
        field.setAccessible(true);
        PsiReferenceRegistrar registrar = (PsiReferenceRegistrar)field.get(null);

        PsiReferenceProvider psiReferenceProvider = new HotCatPsiReferenceProvider(){
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                if(element.getText().equals("map")){
                    HotCatReference catReference = new HotCatReference(element){
                        @Override
                        public Object[] getVariants(){
                            LookupElement lookupElement = new LookupElement() {
                                @Override
                                public @NotNull String getLookupString() {
                                    return "//ht: this is my test";
                                }
                            };
                            return new Object[]{lookupElement};
                        }
                    };
                    return new PsiReference[]{catReference};
                }
                return super.getReferencesByElement(element, context);
            }
        };
        PsiElementPattern.Capture<PsiElement> capture = new PsiElementPattern.Capture<PsiElement>(PsiElement.class) {
            @Override
            public boolean accepts(@Nullable Object o, ProcessingContext context) {
                return true;
            }
        };
        registrar.registerReferenceProvider(capture, psiReferenceProvider, 1);

        LookupEx lookup = instance.getActiveLookup();
//        lookup.addLookupListener(new LookupListener() {
//            @Override
//            public void lookupShown(@NotNull LookupEvent event) {
//                lookup.getItems().add(new LookupElement() {
//                    @Override
//                    public @NotNull String getLookupString() {
//                        return "this is a test";
//                    }
//                });
//            }
//        });

        return "ok";
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert code == 200;
    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-09-25";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }
}
