package io.github.binarybeing.hotcat.plugin.utils;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.binarybeing.hotcat.plugin.entity.IdeaParsedJavaEntity;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class JavaParseUtils {

    public static IdeaParsedJavaEntity parseJavaFile(DataContext dataContext, String file, int start, int end){
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        Assert.assertTrue("editor or project not found, try open a java file", editor != null && project != null);
        File targetFile = new File(file);
        VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetFile);
        Assert.assertNotNull("file not found", vFile);
        PsiFile psiFile = PsiManager.getInstance(project).findFile(vFile);
        Assert.assertTrue("not a java file", psiFile instanceof PsiJavaFile);
        PsiJavaFile javaFile = (PsiJavaFile) psiFile;
        IdeaParsedJavaEntity ideaParsedJavaEntity = new IdeaParsedJavaEntity();

        ideaParsedJavaEntity.setPsiClass(PsiTreeUtil.getParentOfType(javaFile.findElementAt(start), PsiClass.class));
        ideaParsedJavaEntity.setFields(PsiTreeUtil.findChildrenOfType(ideaParsedJavaEntity.getPsiClass(), PsiField.class));
        ideaParsedJavaEntity.setMethods(PsiTreeUtil.findChildrenOfType(ideaParsedJavaEntity.getPsiClass(), PsiMethod.class));
        ideaParsedJavaEntity.setSelectedMethod(PsiTreeUtil.getParentOfType(javaFile.findElementAt(start), PsiMethod.class));
        Map<String, PsiExpression> selectedExpression = new HashMap<>();

        if (ideaParsedJavaEntity.getSelectedMethod() != null) {
            TextRange range = new TextRange(editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd());

            List<PsiField> fieldsRefByMethod = PsiTreeUtil.collectElementsOfType(ideaParsedJavaEntity.getSelectedMethod(), PsiReferenceExpression.class)
                    .stream().map(PsiReference::resolve).filter(Objects::nonNull).filter(p -> p instanceof PsiField)
                    .map(p -> (PsiField) p).collect(Collectors.toList());
            ideaParsedJavaEntity.setFieldsRefBySelectedMethod(fieldsRefByMethod);
            ideaParsedJavaEntity.setModifiersOfSelectedMethod(ideaParsedJavaEntity.getSelectedMethod().getModifierList());
            ideaParsedJavaEntity.setReturnTypeOfSelectedMethod(ideaParsedJavaEntity.getSelectedMethod().getReturnTypeElement());
            ideaParsedJavaEntity.setNameOfSelectedMethod(ideaParsedJavaEntity.getSelectedMethod().getNameIdentifier());
            ideaParsedJavaEntity.setParametersOfSelectedMethod(Arrays.stream(ideaParsedJavaEntity.getSelectedMethod().getParameterList().getParameters()).collect(Collectors.toList()));
            ideaParsedJavaEntity.setThrowTypesOfSelectedMethod(Arrays.stream(ideaParsedJavaEntity.getSelectedMethod().getThrowsList().getReferencedTypes()).collect(Collectors.toList()));
            PsiTreeUtil.findChildrenOfAnyType(ideaParsedJavaEntity.getSelectedMethod(), PsiIfStatement.class).stream().filter(p -> p.getTextRange().contains(range))
                    .map(PsiIfStatement::getCondition).filter(Objects::nonNull).map(p -> {
                        for (PsiElement child : p.getChildren()) {
                            if (child instanceof PsiBinaryExpression
                                    || child instanceof PsiMethodCallExpression
                                    || child instanceof PsiPrefixExpression) {
                                if(child.getTextRange().contains(range)){
                                    return (PsiExpression) child;
                                }
                            }
                        }
                        return null;
                    }).filter(Objects::nonNull).findFirst().ifPresent(p -> selectedExpression.put("selectedIfCondition", p));

            PsiTreeUtil.findChildrenOfAnyType(ideaParsedJavaEntity.getSelectedMethod(), PsiMethodCallExpression.class)
                    .forEach(p -> selectedExpression.put("methodCalls", p));

            PsiTreeUtil.findChildrenOfType(ideaParsedJavaEntity.getSelectedMethod(), PsiNewExpression.class)
                    .forEach(p -> selectedExpression.put("newExpressions", p));

            PsiTreeUtil.findChildrenOfAnyType(ideaParsedJavaEntity.getSelectedMethod(), PsiMethodCallExpression.class)
                    .stream().filter(p -> p.getTextRange().contains(range))
                    .max(Comparator.comparingDouble(p -> p.getTextRange().getStartOffset() + p.getTextRange().getEndOffset() == 0 ? 0 : (1.0 / p.getTextRange().getEndOffset())))
                    .ifPresent(p -> selectedExpression.put("selectedMethodCall", p));


            PsiTreeUtil.findChildrenOfType(ideaParsedJavaEntity.getSelectedMethod(), PsiNewExpression.class)
                    .stream().filter(p -> p.getTextRange().contains(range))
                    .max(Comparator.comparingDouble(p -> p.getTextRange().getStartOffset() + p.getTextRange().getEndOffset() == 0 ? 0 : (1.0 / p.getTextRange().getEndOffset())))
                    .ifPresent(p -> selectedExpression.put("selectedNewExpression", p));
        }
        ideaParsedJavaEntity.setCurrentPositionPsiElement(psiElement);
        ideaParsedJavaEntity.setSelectedExpression(selectedExpression);
        return ideaParsedJavaEntity;
    }

    public static List<String> getRelatedTypes(PsiElement element){
        Set<String> set = new LinkedHashSet<>();
        PsiTreeUtil.findChildrenOfType(element, PsiReferenceExpression.class).forEach(ref -> {
            PsiElement resolved = ref.resolve();
            if (resolved instanceof PsiField) {
                PsiField field = (PsiField) resolved;
                set.add(field.getType().getCanonicalText());
            }
            if (resolved instanceof PsiParameter) {
                PsiParameter ele = (PsiParameter) resolved;
                set.add(ele.getType().getCanonicalText());
            }
            if (resolved instanceof PsiLocalVariable) {
                PsiLocalVariable ele = (PsiLocalVariable) resolved;
                set.add(ele.getType().getCanonicalText());
            }
            if (resolved instanceof PsiClass) {
                PsiClass ele = (PsiClass) resolved;
                set.add(ele.getQualifiedName());
            }
        });
        List<String> list = new ArrayList<>();
        for (String typeName : set) {
            if (typeName == null
                    || typeName.startsWith("java.lang") || "int".equals(typeName) || "long".equals(typeName) || "double".equals(typeName) || "float".equals(typeName) || "boolean".equals(typeName) || "char".equals(typeName) || "byte".equals(typeName) || "short".equals(typeName) || "void".equals(typeName)){
                continue;
            }
            if(typeName.contains("<")){
                typeName = typeName.substring(0, typeName.indexOf("<"));
            }
            list.add(typeName);
        }
        return list;
    }

    public static String transToOgnl(PsiElement element) {
        List<PsiStatement> collect = PsiTreeUtil.findChildrenOfType(element, PsiStatement.class)
                .stream()
                .filter(s -> s instanceof PsiDeclarationStatement || s instanceof PsiReturnStatement || s instanceof PsiExpressionStatement)
                .collect(Collectors.toList());

        List<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (PsiStatement statement : collect) {
            ans.add(toOgnl(statement, sb));
        }
        LogUtils.addLog("transToOgnl: " + sb.toString());
        return String.join(",", ans);
    }
    private static  String toOgnl(PsiStatement statements, StringBuilder stringBuilder){
        return toOgnlElement(statements, stringBuilder);
    }

    private static String toOgnlElement(PsiElement element, StringBuilder classes){
        if(element == null){
            return "";
        }
        classes.append(element.getText())
                .append(" \n======> ").append(element.getClass().getSimpleName()).append("  | ")
                .append(" parent=").append(element.getParent().getClass().getSimpleName())
                .append(" prev=").append(element.getPrevSibling() == null ? "" : element.getPrevSibling().getClass().getSimpleName())
                .append(" nav=").append(element.getNavigationElement() == null ? "" : element.getNavigationElement().getClass().getSimpleName())
                .append(" reference=").append(element.getReference() == null || element.getReference().resolve() == null ? "" : element.getReference().resolve().getClass().getSimpleName())
                .append(" next=").append(element.getNextSibling() == null ? "" : element.getNextSibling().getClass().getSimpleName()).append("\n");

        if(element instanceof PsiKeyword && "new".equals(element.getText())){
            return "new";
        }

        if (element instanceof PsiKeyword && "instanceof".equals(element.getText())) {
            return "instanceof";
        }

        if (element instanceof PsiModifierList
                || element instanceof PsiReferenceParameterList
                || element instanceof PsiKeyword
                || element instanceof PsiJavaToken && ";".equals(element.getText())
                ) {
            return "";
        }

        if(element instanceof PsiTypeElement){
            if (element.getParent() instanceof PsiInstanceOfExpression) {
                return ((PsiTypeElement) element).getType().getCanonicalText();
            }
            if(PsiTreeUtil.findFirstParent(element, c -> c instanceof PsiMethodCallExpression) != null){
                return "@" + ((PsiTypeElement) element).getType().getCanonicalText();
            }

            return "";
        }

        if(element instanceof PsiJavaToken && element.getParent() instanceof  PsiTypeCastExpression){
            if("(".equals(element.getText()) || ")".equals(element.getText())){
                return "";
            }
        }


        if (element instanceof PsiJavaToken && ".".equals(element.getText())) {
            if(element.getPrevSibling() instanceof PsiSuperExpression
                    || element.getPrevSibling() instanceof PsiThisExpression ){
                return "";
            }
            if(element.getPrevSibling() instanceof PsiReferenceExpression
                    && (((PsiReferenceExpression)element.getPrevSibling()).resolve() instanceof PsiClass)
                    && element.getNextSibling() instanceof PsiReferenceParameterList
                    && element.getNextSibling().getNextSibling() instanceof PsiIdentifier){
                return "@";
            }
            if (element.getNextSibling() instanceof PsiReferenceParameterList) {
                return ".";
            }
            if(element.getNextSibling() instanceof PsiKeyword && "class".equals(element.getNextSibling().getText())){
                return "@class";
            }

            return ".";
        }

        if (element instanceof PsiIdentifier) {
            if (element.getParent() instanceof PsiJavaCodeReferenceElement
                    && element.getParent().getParent() instanceof PsiNewExpression
                    && element.getNavigationElement() instanceof PsiIdentifier){
                PsiElement parentElement = ((PsiJavaCodeReferenceElement) element.getParent()).resolve();
                if (parentElement instanceof PsiClass) {
                    return ((PsiClass)parentElement).getQualifiedName();
                }
            }
            if (element.getPrevSibling() instanceof PsiReferenceParameterList
                    && element.getPrevSibling().getPrevSibling() instanceof PsiJavaToken
                    && ".".equals(element.getPrevSibling().getPrevSibling().getText())) {
                return element.getText();
            }
            return "#"+element.getText();
        }
        if (element instanceof PsiReferenceExpression) {
            PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) element;
            PsiElement resolve = psiReferenceExpression.resolve();
            if (resolve instanceof PsiClass) {
                PsiClass aClass = (PsiClass) resolve;
                return "@"+aClass.getQualifiedName();
            }
        }

        PsiElement[] children = element.getChildren();
        if (ArrayUtils.isEmpty(children)) {
            return element.getText();
        }
        StringBuilder sb = new StringBuilder();
        for (PsiElement child : children) {
            sb.append(toOgnlElement(child, classes));
        }
        return sb.toString();
    }

}
