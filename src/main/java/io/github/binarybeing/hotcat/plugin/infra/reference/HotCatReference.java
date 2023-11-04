package io.github.binarybeing.hotcat.plugin.infra.reference;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotCatReference extends PsiReferenceBase<PsiElement> {

    private PsiElement element;
    public HotCatReference(@NotNull PsiElement element) {
        super(element);
        this.element = element;
    }

    @Override
    public @Nullable PsiElement resolve() {
        try {
            String text = element.getText();
            return element.getNextSibling();
//
//            if (text == null) return null;
//            if(text.startsWith("ht")){
//                Module module = ModuleUtil.findModuleForFile(element.getContainingFile().getVirtualFile(), element.getProject());
//                String clazz = text.substring(7);
//                return JavaFileManager.getInstance(element.getProject()).findClass(clazz, GlobalSearchScope.moduleWithDependenciesScope(module));
//            }
//            if(text.startsWith("ht:symbol:")){
//
//            }
//            return null;
        } catch (Exception e) {
            LogUtils.addError(e, "HotCatReference.resolve error");
            return null;
        }
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
