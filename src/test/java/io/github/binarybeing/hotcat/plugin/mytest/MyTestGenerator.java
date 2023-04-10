package io.github.binarybeing.hotcat.plugin.mytest;

import com.intellij.ide.actions.OpenFileAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorWithProviderComposite;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import io.github.binarybeing.hotcat.plugin.panel.SidePanel;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MyTestGenerator {
    public String generate(@NotNull AnActionEvent event) throws Exception{
        DataContext dataContext = event.getDataContext();
        try {
            CompilationUnit compilationUnit = getCompilationUnit(dataContext);
            MethodDeclaration method = getSelectingMethod(dataContext, compilationUnit);
            File testFile = getTargetTestFile(compilationUnit, method, dataContext);
            if(!testFile.exists()){
                testFile = doGenerate(compilationUnit, method, dataContext, event, testFile);
            }
            VirtualFile javaSourceFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
            VirtualFile testVFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(testFile);
            FileEditor editor = TextEditorProvider.getInstance().createEditor(event.getProject(), testVFile);

            new SidePanel().showSidePanel(event, editor.getComponent(), "PowerMockGenerator", method.getName());
            return testVFile.toString();
        } catch (Exception e) {
            LogUtils.addError(e, "MyTestGenerator generate error: " +e.getMessage());
            throw e;
        }

    }

    private static File doGenerate(CompilationUnit compilationUnit, MethodDeclaration method,
                                          DataContext dataContext, AnActionEvent event, File file) throws Exception {

        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(compilationUnit.getPackage().getName()).append(";\n");
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
    private static File getTargetTestFile(CompilationUnit compilationUnit, MethodDeclaration method, DataContext dataContext) throws Exception {
        PackageDeclaration aPackage = compilationUnit.getPackage();
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
        // get test module
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        Module module = ProjectFileIndex.SERVICE.getInstance(project).getModuleForFile(virtualFile);
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

        List<VirtualFile> roots = moduleRootManager.getSourceRoots(JavaSourceRootType.TEST_SOURCE);
        if(roots.size() == 0){
            throw new Exception("no test module founded");
        }
        String testPath = roots.get(0).getPath();
        String typeName = typeList.get(0).getName();
        String methodName = method.getName();
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
        if(!(javaFile instanceof PsiJavaFile)){
            throw new Exception("no selected java file founded");
        }
        String path = javaFile.getVirtualFile().getPath();
        File file = new File(path);
        return JavaParser.parse(file);
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
        Optional<MethodDeclaration> methodOpt = list.stream().filter(n -> n.getBeginLine() <= line)
                .filter(n -> n.getEndLine() >= line)
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

}
