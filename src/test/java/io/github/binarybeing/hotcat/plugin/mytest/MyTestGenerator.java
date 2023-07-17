package io.github.binarybeing.hotcat.plugin.mytest;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import io.github.binarybeing.hotcat.plugin.panel.SidePanel;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MyTestGenerator {
    public String generate(@NotNull AnActionEvent event) throws Exception{

        DataContext dataContext = event.getDataContext();
        try {
            CompilationUnit compilationUnit = getCompilationUnit(dataContext);
            MethodDeclaration method = getSelectingMethod(dataContext, compilationUnit);

            Set<String> fields = new LinkedHashSet<>();
            method.accept(new VoidVisitorAdapter<>() {
                @Override
                public void visit(NameExpr n, Set<String> arg) {
                    try {
                        ResolvedValueDeclaration resolve = n.resolve();
                        if (resolve.isField()) {
                            fields.add(resolve.getName());
                        }
                    } catch (Exception e) {}
                }
            }, fields);
            PsiJavaFile javaFile = (PsiJavaFile)CommonDataKeys.PSI_FILE.getData(dataContext);
            ArrayList<PsiField> list = new ArrayList<>();
            javaFile.acceptChildren(new PsiElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof PsiClass) {
                        ((PsiClass) element).acceptChildren(this);
                    }
                    if (element instanceof PsiField) {
                        if (fields.contains(((PsiField) element).getName())) {
                            list.add((PsiField) element);
                        }
                    }
                }
            });

            if (!list.isEmpty()) {
                ArrayList<Object> objects = new ArrayList<>();
                for (PsiField psiField : list) {

                    objects.add(psiField.getType().getCanonicalText());
                }

//                throw new RuntimeException(objects.toString());
            }
//            LogUtils.addLog(list.toString());


//            LogUtils.addLog("varId = " + new Gson().toJson(varId));
            List<TypeDeclaration> typeList = new ArrayList<>();
            for (TypeDeclaration type : compilationUnit.getTypes()) {
                type.accept(new VoidVisitorAdapter<List<TypeDeclaration>>() {
                    @Override
                    public void visit(MethodDeclaration n, List<TypeDeclaration> list) {
                        if(n.equals(method)){
                            list.add(type);
                        }
                    }
                }, typeList);
            }
            if(typeList.size() == 0){
                throw new Exception("no class of method founded");
            }

            File testFile = getTargetTestFile(compilationUnit, typeList.get(0), method, dataContext);
            if(!testFile.exists()){
                testFile = doGenerate(compilationUnit, method, dataContext, event, testFile);
            }
            VirtualFile javaSourceFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
            VirtualFile testVFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(testFile);
            FileEditor editor = TextEditorProvider.getInstance().createEditor(event.getProject(), testVFile);

            new SidePanel().showSidePanel(event, editor.getComponent(), "MockMock", method.getName().asString());
            return testVFile.toString();
        } catch (Exception e) {
            LogUtils.addError(e, "MyTestGenerator generate error: " +e.getMessage());
            throw e;
        }

    }

    private static File doGenerate(CompilationUnit compilationUnit, MethodDeclaration method,
                                          DataContext dataContext, AnActionEvent event, File file) throws Exception {

        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(compilationUnit.getPackageDeclaration().get().getName().asString()).append(";\n");
        builder.append("import org.junit.Test;\n");
        builder.append("import org.junit.runner.RunWith;\n");

        builder.append("public class ").append(file.getName().substring(0, file.getName().length() - 5)).append(" {\n");
        builder.append("\t@Test\n");
        builder.append("\tpublic void ").append(method.getName()).append("() {\n");
        builder.append("\t\t// TODO: 2020/12/23 \n");
        builder.append("\t}\n");
        builder.append("}");
        FileUtils.writeStringToFile(file, builder.toString());
        return new File(file.getAbsolutePath());
    }


    @NotNull
    private static File getTargetTestFile(CompilationUnit compilationUnit, TypeDeclaration type,
                                          MethodDeclaration method, DataContext dataContext) throws Exception {

        PackageDeclaration aPackage = compilationUnit.getPackageDeclaration().get();

        // get test module
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        Module module = ProjectFileIndex.SERVICE.getInstance(project).getModuleForFile(virtualFile);
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

        List<VirtualFile> roots = moduleRootManager.getSourceRoots(JavaSourceRootType.TEST_SOURCE);
        if (roots.size() == 0) {
            throw new Exception("no test module founded");
        }
        String testPath = roots.get(0).getPath();
        String typeName = type.getName().asString();
        String methodName = method.getName().asString();
        methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        String testFilePath = testPath + "/" + aPackage.getName().toString().replace(".", "/") + "/" + typeName + methodName + "Test.java";
        return new File(testFilePath);
    }

    private CompilationUnit getCompilationUnit(DataContext dataContext) throws Exception{
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            throw new Exception("project not found");
        }
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            throw new Exception("editor not found");
        }

        PsiFile javaFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        Module module = ModuleUtil.findModuleForFile(javaFile.getVirtualFile(), project);
        getSelectedType(dataContext, project,editor, (PsiJavaFile) javaFile);


        if(!(javaFile instanceof PsiJavaFile)){
            throw new Exception("no selected java file founded");
        }

        String path = javaFile.getVirtualFile().getPath();
        File file = new File(path);
        ParserConfiguration configuration = new ParserConfiguration();
        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots(false);
        if (sourceRoots.length == 0) {
            throw new Exception("no source root founded");
        }

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        configuration.setSymbolResolver(javaSymbolSolver);
        StaticJavaParser.setConfiguration(configuration);
        return StaticJavaParser.parse(file);
    }

    private MethodDeclaration getSelectingMethod(DataContext dataContext, CompilationUnit compilationUnit) throws Exception{
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            throw new Exception("editor not found");
        }
        PsiFile javaFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        if(!(javaFile instanceof PsiJavaFile)){
            throw new Exception("no selected java file founded");
        }
        String path = javaFile.getVirtualFile().getPath();
        File file = new File(path);
        int line = positionToLineColumn(file, editor.getSelectionModel().getSelectionStart());
        List<MethodDeclaration> list = new ArrayList<>();
        VoidVisitorAdapter<List<MethodDeclaration>> visitorAdapter = new VoidVisitorAdapter<>() {
            @Override
            public void visit(MethodDeclaration n, List<MethodDeclaration> list) {
                list.add(n);
            }
        };
        compilationUnit.accept(visitorAdapter, list);
        Optional<MethodDeclaration> methodOpt = list.stream().filter(n -> n.getBegin().orElseThrow().line <= line)
                .filter(n -> n.getEnd().orElseThrow().line >= line)
                .findAny();
        if(methodOpt.isEmpty()){
            throw new Exception("no method founded");
        }
        return methodOpt.get();
    }

    private int positionToLineColumn(File file, int position) throws IOException {
        String content = FileUtils.readFileToString(file, "UTF-8");
        int line = 1;
        for (int i = 0; i < position; i++) {
            char c = content.charAt(i);
            if (c == '\n') {
                line++;
            }
        }
        return line;
    }

//    private static PsiMethodCallExpression getClosestCallMethod(PsiMethodCallExpression psiMethodCallExpression, TextRange textRange){
//
//    }
    public static void getSelectedType(DataContext dataContext, Project project, Editor editor, PsiJavaFile psiJavaFile){


        final List<Object> info = new ArrayList<>();
        TextRange range = new TextRange(editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd());

//        IdeaParsedJavaEntity entity = JavaParseUtils.parseJavaFile(dataContext, psiJavaFile.getVirtualFile().getPath(),
//                editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd());
//        PsiMethodCallExpression psiExpression = (PsiMethodCallExpression)entity.getSelectedExpression().get("selectedMethodCall");
//        for (PsiExpression expression : psiExpression.getArgumentList().getExpressions()) {
//            info.add(expression.getText()+ " "+expression.getClass().getName());
//        }

//        PsiElement element = psiJavaFile.findElementAt(range.getStartOffset());
//        PsiIfStatement psiIfStatement = PsiTreeUtil.getParentOfType(element, PsiIfStatement.class);
//        PsiExpression condition = psiIfStatement.getCondition();
//        for (PsiElement child : condition.getChildren()) {
//            info.add(child.getText()+ " "+child.getClass().getName());
//        }

//        PsiTreeUtil.findChildrenOfType(entity.getSelectedMethod(), PsiMethodCallExpression.class)
//                .stream().filter(p->p.getTextRange().contains(range))
//                .max(Comparator.comparingDouble(p -> p.getTextRange().getStartOffset() + p.getTextRange().getEndOffset() == 0 ? 0 : (1.0 / p.getTextRange().getEndOffset())))
//                .ifPresent(p->{
//                    String qualifiedName = p.resolveMethod().getContainingClass().getQualifiedName();
//                    info.add(p.getText());
//                });
//        throw new RuntimeException(info.toString());

//        int start = editor.getSelectionModel().getSelectionStart();
//
//        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(psiJavaFile.findElementAt(start), PsiMethod.class);

//        PsiExpression conditionExpression = PsiTreeUtil.findChildrenOfAnyType(psiMethod, PsiIfStatement.class).stream().filter(p -> p.getTextRange().contains(range))
//                .map(PsiIfStatement::getCondition).map(p -> {
//                    return PsiTreeUtil.findChildrenOfType(p, PsiExpression.class).stream().filter(p1 -> p1.getTextRange().contains(range))
//                            .max(Comparator.comparingInt(p1->p1.getTextRange().getStartOffset())).orElse(null);
//                }).filter(Objects::nonNull).findFirst().orElse(null);
//        info.add(conditionExpression.getText());
//        PsiMethodCallExpression callExpression = PsiTreeUtil.findChildrenOfAnyType(psiMethod, PsiMethodCallExpression.class)
//                .stream().filter(p -> p.getTextRange().contains(range))
//                .max(Comparator.comparingInt(p -> p.getTextRange().getStartOffset())).orElse(null);
//        info.add(callExpression.getText());



//        PsiTreeUtil.collectElementsOfType(entity.getSelectedMethod(), PsiIfStatement.class)
//                .stream().map(PsiIfStatement::getCondition).forEach(c->{
//                    info.add(c.getText());
//                });




//        PsiImportStatement frequencyGroup = importList.findSingleClassImportStatement("org.im.core.frequency.terra.TerraFrequencyGroup");
//        info.add(frequencyGroup.getText());
        //get reference method start
//        PsiTreeUtil.collectElementsOfType(entity.getSelectedPsiMethod(), PsiReferenceExpression.class)
//                .stream().map(PsiReference::resolve).filter(Objects::nonNull).filter(p->p instanceof PsiMethod)
//                .map(p->(PsiMethod)p).forEach(p->{
//                    info.add(p.getName());
//                 });
        //get reference method end
//
//
//        final Collection<PsiReferenceExpression> psiReferenceExpressions = PsiTreeUtil.collectElementsOfType(entity.getSelectedPsiMethod(), PsiReferenceExpression.class);
//        for (PsiReferenceExpression psiReferenceExpression : psiReferenceExpressions) {
//            final PsiElement psiElement = psiReferenceExpression.resolve();
//            if ("TimeoutException".equals(psiElement.getText())) {
//                info.add(psiElement.getClass());
//            }
//            if (!(psiElement instanceof PsiMethod)) {
//                if (psiElement instanceof PsiField) {
//                    PsiField element = (PsiField) psiElement;
//                    info.add(element.getType().getCanonicalText());
//                }
//                if (psiElement instanceof PsiParameter) {
//                    PsiParameter element = (PsiParameter) psiElement;
//                    info.add(element.getType().getCanonicalText());
//                }
//                if (psiElement instanceof PsiLocalVariable) {
//                    PsiLocalVariable element = (PsiLocalVariable) psiElement;
//                    info.add(element.getType().getCanonicalText());
//                }
//                if (psiElement instanceof PsiClass) {
//                    PsiClass element = (PsiClass) psiElement;
//                    info.add(element.getQualifiedName());
//                }
//                if (psiElement instanceof PsiReturnStatement) {
//                    PsiReturnStatement element = (PsiReturnStatement) psiElement;
//                    info.add(element.getReturnValue().getType().getCanonicalText());
//                }
//            }
//        }

    }

//    public static void getSelectedType(Project project, Editor editor, PsiJavaFile psiJavaFile){
//        VisualPosition visualPosition = editor.getSelectionModel().getSelectionStartPosition();
//        int selectionStart = editor.getSelectionModel().getSelectionStart();
//        int selectionEnd = editor.getSelectionModel().getSelectionEnd();
//
//        IdeaJavaParserController.SelectedElements selectedElements = new IdeaJavaParserController.SelectedElements();
//        Set<PsiField> fields = new HashSet<>();
//        psiJavaFile.accept(new PsiElementVisitor() {
//            @Override
//            public void visitElement(@NotNull PsiElement element) {
//                if (element instanceof PsiJavaFileImpl) {
//                    element.acceptChildren(this);
//                }
//                if (element instanceof PsiClass) {
//                    TextRange textRange = element.getTextRange();
//                    if (textRange.containsRange(selectionStart, selectionEnd)) {
//                        selectedElements.setPsiClass((PsiClass) element);
//                        element.acceptChildren(this);
//                    }
//                }
//                if (element instanceof PsiField) {
//                    fields.add((PsiField) element);
//                }
//                if(element instanceof PsiMethod){
//                    TextRange textRange = element.getTextRange();
//                    if (textRange.containsRange(selectionStart, selectionEnd)) {
//                        selectedElements.setPsiMethod((PsiMethod) element);
//                    }
//                }
//            }
//        });
//        if (selectedElements.getPsiClass() == null || selectedElements.getPsiMethod() == null) {
//            throw new RuntimeException("class or method analysis failed");
//        }
//
//        PsiCodeBlock[] block = new PsiCodeBlock[1];
//        selectedElements.getPsiMethod().acceptChildren(new PsiElementVisitor() {
//            @Override
//            public void visitElement(@NotNull PsiElement element) {
//                if (element instanceof PsiParameterList) {
//                    PsiParameterList list = (PsiParameterList) element;
//                    List<PsiParameter> arrayList = new ArrayList<>();
//                    for (PsiParameter parameter : list.getParameters()) {
//                        arrayList.add(parameter);
//                    }
//                    selectedElements.setParameters(arrayList);
//                }
//                if(element instanceof PsiModifierList){
//                    PsiModifierList list = (PsiModifierList) element;
//                    selectedElements.setModifierList(list);
//                }
//                if(element instanceof PsiTypeElement){
//                    PsiTypeElement typeElement = (PsiTypeElement) element;
//                    selectedElements.setReturnTypeElement(typeElement);
//                }
//                if (element instanceof PsiIdentifier) {
//                    PsiIdentifier identifier = (PsiIdentifier) element;
//                    selectedElements.setMethodName(identifier);
//                }
//
//                if (element instanceof PsiCodeBlock) {
//                    block[0] = (PsiCodeBlock) element;
//                }
//            }
//        });
//        if(block[0] == null || fields.size()==0){
//            throw new RuntimeException("block[0] is null or no fields founded");
//        }
//        HashMap<String, PsiField> fieldsNames = new HashMap<>();
//        for (PsiField field : fields) {
//            field.acceptChildren(new PsiElementVisitor() {
//                @Override
//                public void visitElement(@NotNull PsiElement element) {
//                    if (element instanceof PsiIdentifier) {
//                        PsiIdentifier namedElement = (PsiIdentifier) element;
//                        fieldsNames.put(namedElement.getText(), field);
//                    }
//                }
//            });
//        }
//        List<PsiNamedElement> fieldsRefByMethod = new ArrayList<>();
//        ArrayList<Object> arrayList = new ArrayList<>();
//        block[0].acceptChildren(new PsiElementVisitor() {
//            @Override
//            public void visitElement(@NotNull PsiElement element) {
//                LogUtils.addLog("block[0] element:" + element.getClass() + element.getText());
//                if (element instanceof PsiIdentifier) {
//                    PsiIdentifier namedElement = (PsiIdentifier) element;
//                    if (fieldsNames.containsKey(namedElement.getText())) {
//                        fieldsRefByMethod.add(fieldsNames.get(namedElement.getText()));
//                        arrayList.add(namedElement.getText());
//                    }
//                }
//                if (element.getChildren().length > 0) {
//                    element.acceptChildren(this);
//                }
//            }
//        });
//        selectedElements.setFieldsRefByMethod(fieldsRefByMethod);
//
//        throw new RuntimeException("end:"+ arrayList.toString());

//        PsiMethod[] psiMethod = new PsiMethod[1];
//        selectedElements.getPsiMethod().accept(new PsiElementVisitor() {
//            @Override
//            public void visitElement(@NotNull PsiElement element) {
//                if (element instanceof PsiMethod) {
//                    TextRange textRange = element.getTextRange();
//                    if (textRange.containsRange(selectionStart, selectionEnd)) {
//                        psiMethod[0] = (PsiMethod) element;
//                    }
//                }
//            }
//        });

//        if (psiMethod[0] == null) {
//            throw new RuntimeException("psiMethod[0] is null");
//        }
//
//        List<Object> list = new ArrayList<>();
//        psiMethod[0].accept(new PsiElementVisitor() {
//            @Override
//            public void visitElement(@NotNull PsiElement element) {
//                list.add(element);
//            }
//        });
//        if (list.size() > 0) {
//            throw new RuntimeException("list= " + list);
//        }

//    }


}
