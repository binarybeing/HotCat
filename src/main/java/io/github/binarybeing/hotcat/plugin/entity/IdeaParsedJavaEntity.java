package io.github.binarybeing.hotcat.plugin.entity;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.binarybeing.hotcat.plugin.utils.JavaParseUtils;

import java.util.*;
import java.util.stream.Collectors;

public class IdeaParsedJavaEntity {
    private PsiClass psiClass;

    private PsiMethod selectedMethod;

    private Collection<PsiParameter> parametersOfSelectedMethod = new ArrayList<>();

    private PsiTypeElement returnTypeOfSelectedMethod;


    private PsiModifierList modifiersOfSelectedMethod;

    private PsiIdentifier nameOfSelectedMethod;

    private List<PsiField> fieldsRefBySelectedMethod = new ArrayList<>();

//    private List<PsiClass> typesRefBySelectedMethod = new ArrayList<>();

    private Collection<PsiField> fields = new ArrayList<>();

    private Collection<PsiMethod> methods = new ArrayList<>();
    private Collection<PsiClassType> throwTypesOfSelectedMethod = new ArrayList<>();

    private Map<String, PsiExpression> selectedExpression = new HashMap<>();

    private PsiElement currentPositionPsiElement;

    public Collection<PsiClassType> getThrowTypesOfSelectedMethod() {
        return throwTypesOfSelectedMethod;
    }

    public void setThrowTypesOfSelectedMethod(Collection<PsiClassType> throwTypesOfSelectedMethod) {
        this.throwTypesOfSelectedMethod = throwTypesOfSelectedMethod;
    }

    public Map<String, PsiExpression> getSelectedExpression() {
        return selectedExpression;
    }

    public void setSelectedExpression(Map<String, PsiExpression> selectedExpression) {
        this.selectedExpression = selectedExpression;
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }

    public void setPsiClass(PsiClass psiClass) {
        this.psiClass = psiClass;
    }

    public PsiMethod getSelectedMethod() {
        return selectedMethod;
    }

    public void setSelectedMethod(PsiMethod selectedMethod) {
        this.selectedMethod = selectedMethod;
    }

    public Collection<PsiParameter> getParametersOfSelectedMethod() {
        return parametersOfSelectedMethod;
    }

    public void setParametersOfSelectedMethod(Collection<PsiParameter> parametersOfSelectedMethod) {
        this.parametersOfSelectedMethod = parametersOfSelectedMethod;
    }

    public PsiTypeElement getReturnTypeOfSelectedMethod() {
        return returnTypeOfSelectedMethod;
    }

    public void setReturnTypeOfSelectedMethod(PsiTypeElement returnTypeOfSelectedMethod) {
        this.returnTypeOfSelectedMethod = returnTypeOfSelectedMethod;
    }

    public PsiModifierList getModifiersOfSelectedMethod() {
        return modifiersOfSelectedMethod;
    }

    public void setModifiersOfSelectedMethod(PsiModifierList modifiersOfSelectedMethod) {
        this.modifiersOfSelectedMethod = modifiersOfSelectedMethod;
    }

    public PsiIdentifier getNameOfSelectedMethod() {
        return nameOfSelectedMethod;
    }

    public void setNameOfSelectedMethod(PsiIdentifier nameOfSelectedMethod) {
        this.nameOfSelectedMethod = nameOfSelectedMethod;
    }

    public List<PsiField> getFieldsRefBySelectedMethod() {
        return fieldsRefBySelectedMethod;
    }

    public void setFieldsRefBySelectedMethod(List<PsiField> fieldsRefBySelectedMethod) {
        this.fieldsRefBySelectedMethod = fieldsRefBySelectedMethod;
    }

    public Collection<PsiField> getFields() {
        return fields;
    }

    public void setFields(Collection<PsiField> fields) {
        this.fields = fields;
    }

    public Collection<PsiMethod> getMethods() {
        return methods;
    }

    public void setMethods(Collection<PsiMethod> methods) {
        this.methods = methods;
    }

    public PsiElement getCurrentPositionPsiElement() {
        return currentPositionPsiElement;
    }

    public void setCurrentPositionPsiElement(PsiElement currentPositionPsiElement) {
        this.currentPositionPsiElement = currentPositionPsiElement;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        if(psiClass!= null){
            map.put("psiClass", Map.of("type", orElse(psiClass.getQualifiedName(), "")));
            if (psiClass.getSuperClass() != null) {
                map.put("superClass", Map.of("type", orElse(psiClass.getSuperClass().getQualifiedName(), "")));
            }
            if (psiClass.getInterfaces().length > 0) {
                List<Map<String, String>> interfaces = new ArrayList<>();
                for (PsiClass psiInterface : psiClass.getInterfaces()) {
                    interfaces.add(Map.of("type", orElse(psiInterface.getQualifiedName(), "")));
                }
                map.put("interfaces", interfaces);
            }
        }
        if(selectedMethod != null){
            map.put("selectedMethod", extractMethodInfo(selectedMethod));
        }
        if (fieldsRefBySelectedMethod.size() > 0) {
            List<Map<String, String>> fieldsRefByMethod = new ArrayList<>();
            for (PsiField field : this.fieldsRefBySelectedMethod) {
                fieldsRefByMethod.add(Map.of("name", orElse(field.getName(), ""),
                        "type", orElse(field.getType().getCanonicalText(), ""),
                        "from_class", Optional.ofNullable(PsiTreeUtil.getParentOfType(field, PsiClass.class)).map(PsiClass::getQualifiedName).orElse("")));
            }
            map.put("fieldsRefBySelectedMethod", fieldsRefByMethod);
        }
        if (fields.size() > 0) {
            List<Map<String, String>> fields = new ArrayList<>();
            for (PsiField field : this.fields) {
                fields.add(Map.of("name", orElse(field.getName(), ""),
                        "type", orElse(field.getType().toString(), ""),
                        "from_class", Optional.ofNullable(PsiTreeUtil.getParentOfType(field, PsiClass.class)).map(PsiClass::getQualifiedName).orElse("")));
            }
            map.put("fields", fields);
        }
        if (methods.size() > 0){
            List<Map<String, Object>> methods = new ArrayList<>();
            for (PsiMethod method : this.methods) {
                methods.add(extractMethodInfo(method));
            }
            map.put("methods", methods);
        }
        selectedExpression.computeIfPresent("selectedIfCondition", (k, v)->{
            if (v instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression callExpression = (PsiMethodCallExpression) v;
                PsiMethod psiMethod = callExpression.resolveMethod();
                if (psiMethod != null) {
                    Map<String, Object> extractMethodInfo = extractMethodInfo(psiMethod);
                    extractMethodInfo.put("callParams", extractParamsInfo(callExpression));
                    map.put(k, extractMethodInfo);
                }
            } else {
                map.put(k, Map.of("text", orElse(v.getText(), ""), "relatedTypes", JavaParseUtils.getRelatedTypes(v)));
            }
            return v;
        });
        selectedExpression.computeIfPresent("selectedNewExpression", (k, v)->{
            PsiNewExpression expression = (PsiNewExpression) v;
            PsiMethod psiMethod = expression.resolveConstructor();
            if (psiMethod != null) {
                map.put(k, extractMethodInfo(psiMethod));
            }
            return v;
        });
        selectedExpression.computeIfPresent("selectedMethodCall", (k, v)->{
            PsiMethodCallExpression callExpression = (PsiMethodCallExpression) v;
            PsiMethod psiMethod = callExpression.resolveMethod();
            if (psiMethod != null) {
                Map<String, Object> extractMethodInfo = extractMethodInfo(psiMethod);
                extractMethodInfo.put("callParams", extractParamsInfo(callExpression));
                map.put(k, extractMethodInfo);
            }
            return v;
        });

        if (nameOfSelectedMethod != null){
            map.put("nameOfSelectedMethod", Map.of("text", orElse(nameOfSelectedMethod.getText(), "")));
        }

        if (currentPositionPsiElement != null) {
            map.put("currentPositionElement", extractElement(currentPositionPsiElement));
        }
        return map;
    }

    private Map<String, Object> extractElement(PsiElement currentPositionPsiElement) {
        Map<String, Object> map = new HashMap<>(2);
        if(currentPositionPsiElement instanceof PsiMethod){
            map.put("type", "method");
            map.put("data", extractMethodInfo((PsiMethod) currentPositionPsiElement));
        }
        return map;
    }

    private<T> T orElse(T value, T other) {
        return value != null ? value : other;
    }


    private Map<String, Object> extractMethodInfo(PsiMethod psiMethod){
        Map<String, Object> methodCall = new HashMap<>();
        methodCall.put("from_class", Optional.ofNullable(psiMethod.getContainingClass()).map(PsiClass::getQualifiedName).orElse(""));
        methodCall.put("name", orElse(psiMethod.getName(), ""));
        methodCall.put("modifiers", psiMethod.getModifierList().getText());
        methodCall.put("params", Arrays.stream(psiMethod.getParameterList().getParameters())
                .map(p->Map.of("name", orElse(p.getName(), ""),
                        "type", orElse(p.getType().getCanonicalText(), "")))
                .collect(Collectors.toList()));
        methodCall.put("returnType", Map.of("type", Optional.ofNullable(psiMethod.getReturnType()).map(PsiType::getCanonicalText).orElse("")));
        methodCall.put("throws", Arrays.stream(psiMethod.getThrowsList().getReferencedTypes())
                .map(p->Map.of("type", orElse(p.getCanonicalText(), "")))
                .collect(Collectors.toList()));
        return methodCall;
    }

    private List<Map<String, Object>> extractParamsInfo(PsiMethodCallExpression callExpression){
        List<Map<String, Object>> list = new ArrayList<>();

        PsiExpressionList argumentList = callExpression.getArgumentList();
        PsiExpression[] expressions = argumentList.getExpressions();
        for (PsiExpression psiExpression : expressions) {
            if (psiExpression instanceof PsiReferenceExpression) {
                PsiReferenceExpression referenceExpression = (PsiReferenceExpression) psiExpression;
                PsiElement resolve = referenceExpression.resolve();
                if (resolve instanceof PsiClass) {
                    list.add(Map.of("name", psiExpression.getText(),
                            "type", orElse(((PsiClass) resolve).getQualifiedName(), "")));
                }
                if (resolve instanceof PsiLocalVariable) {
                    PsiLocalVariable localVariable = (PsiLocalVariable) resolve;
                    list.add(Map.of("name", psiExpression.getText(),
                            "type", orElse(localVariable.getType().getCanonicalText(), "")));
                }
                if (resolve instanceof PsiField) {
                    PsiField field = (PsiField) resolve;
                    list.add(Map.of("name", psiExpression.getText(),
                            "type", orElse(field.getType().getCanonicalText(), "")));
                }
                if (resolve instanceof PsiParameter) {
                    PsiParameter parameter = (PsiParameter) resolve;
                    list.add(Map.of("name", psiExpression.getText(),
                            "type", orElse(parameter.getType().getCanonicalText(), "")));
                }

            }
        }
        return list;
    }



}

