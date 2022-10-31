//package io.github.binarybeing.hotcat.plugin.view;
//
//import com.intellij.ide.structureView.StructureViewModel;
//import com.intellij.ide.structureView.StructureViewModelBase;
//import com.intellij.ide.structureView.StructureViewTreeElement;
//import com.intellij.ide.util.treeView.smartTree.Sorter;
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
//public class SimpleStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider{
//    public SimpleStructureViewModel(@Nullable Editor editor, PsiFile psiFile) {
//        super(psiFile, editor, new SimpleStructureViewElement(psiFile));
//    }
//
//    @NotNull
//    public Sorter @NotNull [] getSorters() {
//        return new Sorter[]{Sorter.ALPHA_SORTER};
//    }
//
//
//    @Override
//    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
//        return false;
//    }
//
//    @Override
//    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
//        return element.getValue() instanceof SimpleProperty;
//    }
//
//    @Override
//    protected Class<?> @NotNull [] getSuitableClasses() {
//        return new Class[]{SimpleProperty.class};
//    }
//}
