package io.github.binarybeing.hotcat.plugin.utils;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.binarybeing.hotcat.plugin.entity.IdeaParsedJavaEntity;
import org.junit.Assert;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class JavaParseUtils {

    public static IdeaParsedJavaEntity parseJavaFile(DataContext dataContext, String file, int start, int end){
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
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
        List<PsiField> fieldsRefByMethod = PsiTreeUtil.collectElementsOfType(ideaParsedJavaEntity.getSelectedMethod(), PsiReferenceExpression.class)
                .stream().map(PsiReference::resolve).filter(Objects::nonNull).filter(p -> p instanceof PsiField)
                .map(p -> (PsiField) p).collect(Collectors.toList());
        ideaParsedJavaEntity.setFieldsRefBySelectedMethod(fieldsRefByMethod);

        ideaParsedJavaEntity.setModifiersOfSelectedMethod(ideaParsedJavaEntity.getSelectedMethod().getModifierList());
        ideaParsedJavaEntity.setReturnTypeOfSelectedMethod(ideaParsedJavaEntity.getSelectedMethod().getReturnTypeElement());
        ideaParsedJavaEntity.setNameOfSelectedMethod(ideaParsedJavaEntity.getSelectedMethod().getNameIdentifier());
        ideaParsedJavaEntity.setParametersOfSelectedMethod(Arrays.stream(ideaParsedJavaEntity.getSelectedMethod().getParameterList().getParameters()).collect(Collectors.toList()));
        ideaParsedJavaEntity.setThrowTypesOfSelectedMethod(Arrays.stream(ideaParsedJavaEntity.getSelectedMethod().getThrowsList().getReferencedTypes()).collect(Collectors.toList()));

        Map<String, PsiExpression> selectedExpression = new HashMap<>();
        TextRange range = new TextRange(editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd());
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
                .stream().filter(p -> p.getTextRange().contains(range))
                .max(Comparator.comparingDouble(p -> p.getTextRange().getStartOffset() + p.getTextRange().getEndOffset() == 0 ? 0 : (1.0 / p.getTextRange().getEndOffset())))
                .ifPresent(p -> selectedExpression.put("selectedMethodCall", p));
        PsiTreeUtil.findChildrenOfType(ideaParsedJavaEntity.getSelectedMethod(), PsiNewExpression.class)
                .stream().filter(p -> p.getTextRange().contains(range))
                .max(Comparator.comparingDouble(p -> p.getTextRange().getStartOffset() + p.getTextRange().getEndOffset() == 0 ? 0 : (1.0 / p.getTextRange().getEndOffset())))
                .ifPresent(p -> selectedExpression.put("selectedNewExpression", p));
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

}
