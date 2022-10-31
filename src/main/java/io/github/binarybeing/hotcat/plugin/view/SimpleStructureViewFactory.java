//package io.github.binarybeing.hotcat.plugin.view;
//
//import com.intellij.ide.structureView.StructureViewBuilder;
//import com.intellij.ide.structureView.StructureViewModel;
//import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
//import com.intellij.lang.PsiStructureViewFactory;
//import com.intellij.openapi.editor.Editor;
//import com.intellij.psi.PsiFile;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
///**
// * @author gn.binarybei
// * @date 2022/10/19
// * @note
// */
//public class SimpleStructureViewFactory implements PsiStructureViewFactory {
//    @Override
//    public @Nullable StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
//        return new TreeBasedStructureViewBuilder() {
//            @NotNull
//            @Override
//            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
//
//                return new SimpleStructureViewModel(editor, psiFile);
//            }
//        };
//    }
//}
