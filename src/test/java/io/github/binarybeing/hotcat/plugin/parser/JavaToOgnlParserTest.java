package io.github.binarybeing.hotcat.plugin.parser;

import com.google.common.base.Predicate;
import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.xml.stream.events.ElementTypeNames;
import org.junit.Ignore;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaToOgnlParserTest extends BaseTest {


    @Override

    public Object doExecute() throws Exception {

        String param1 = "param1";
        ArrayList<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        Predicate<String> predicate = new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return false;
            }
        };
//        Predicate<String> tPredicate = s -> StringUtils.isNotEmpty(s);
        List<String> strings = list.stream().filter(predicate).collect(Collectors.toList());

        System.out.println("hello");
        Map<String,String> map = new Gson().fromJson("{}", Map.class);

        final PsiFile file = super.psiFile;
        if(file instanceof PsiJavaFile){
            PsiJavaFile javaFile = (PsiJavaFile) file;
        }

//        file = CommonDataKeys.PSI_FILE.getData(super.dataContext);

        return PsiTreeUtil.findChildrenOfType(file, PsiStatement.class)
                .stream()
                .filter(s -> s instanceof PsiDeclarationStatement || s instanceof PsiReturnStatement || s instanceof PsiExpressionStatement )
                .map(s -> s.getClass().getSimpleName())
                .collect(Collectors.joining("\n"));
    }


    public Object trans() throws Exception {

        List<PsiStatement> collect = PsiTreeUtil.findChildrenOfType(psiFile, PsiStatement.class)
                .stream()
                .filter(s -> s instanceof PsiDeclarationStatement || s instanceof PsiReturnStatement || s instanceof PsiExpressionStatement)
                .collect(Collectors.toList());

        List<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (PsiStatement statement : collect) {
            ans.add(this.toOgnl(statement, sb));
        }
        return String.join(",", ans) + "\n" + sb.toString();
    }
    private String toOgnl(PsiStatement statements, StringBuilder stringBuilder){

        String exp =  toOgnlElement(statements, stringBuilder);
        return exp;
    }

    private String toOgnlElement(PsiElement element, StringBuilder classes){
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
                || element instanceof PsiKeyword
                || element instanceof PsiJavaToken && ";".equals(element.getText())) {
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

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert code == 200;
    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-09-05";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }
}
