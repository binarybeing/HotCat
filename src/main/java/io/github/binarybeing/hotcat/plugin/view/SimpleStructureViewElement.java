//package io.github.binarybeing.hotcat.plugin.view;
//
//import com.intellij.ide.projectView.PresentationData;
//import com.intellij.ide.structureView.StructureViewTreeElement;
//import com.intellij.ide.util.treeView.smartTree.TreeElement;
//import com.intellij.navigation.ItemPresentation;
//import com.intellij.psi.NavigatablePsiElement;
//import com.intellij.psi.util.PsiTreeUtil;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author gn.binarybei
// * @date 2022/10/19
// * @note
// */
//public class SimpleStructureViewElement implements StructureViewTreeElement {
//    private final NavigatablePsiElement myElement;
//
//    public SimpleStructureViewElement(NavigatablePsiElement element) {
//        this.myElement = element;
//    }
//
//    @Override
//    public Object getValue() {
//        return myElement;
//    }
//
//    @Override
//    public void navigate(boolean requestFocus) {
//        myElement.navigate(requestFocus);
//    }
//
//    @Override
//    public boolean canNavigate() {
//        return myElement.canNavigate();
//    }
//
//    @Override
//    public boolean canNavigateToSource() {
//        return myElement.canNavigateToSource();
//    }
//
//    @NotNull
//    public String getAlphaSortKey() {
//        String name = myElement.getName();
//        return name != null ? name : "";
//    }
//
//    @NotNull
//    @Override
//    public ItemPresentation getPresentation() {
//        ItemPresentation presentation = myElement.getPresentation();
//        return presentation != null ? presentation : new PresentationData();
//    }
//
//    @Override
//    public TreeElement @NotNull [] getChildren() {
//            if (myElement instanceof SimpleFile) {
//                List<SimpleProperty> properties = PsiTreeUtil.getChildrenOfTypeAsList(myElement, SimpleProperty.class);
//                List<TreeElement> treeElements = new ArrayList<>(properties.size());
//                for (SimpleProperty property : properties) {
//                    treeElements.add(new SimpleStructureViewElement((SimplePropertyImpl) property));
//                }
//                return treeElements.toArray(new TreeElement[0]);
//            }
//            return EMPTY_ARRAY;
//    }
//
//}
