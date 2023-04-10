package io.github.binarybeing.hotcat.plugin.javaassist;

import com.intellij.lang.FileASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import io.github.binarybeing.hotcat.plugin.panel.SidePanel;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Javaassistor {

    public String getSelectText(AnActionEvent event) throws InterruptedException {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return "project not found";
        }
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        if (virtualFile == null) {
            return "virtualFile not found";
        }
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            return "editor not found";
        }
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        if(!(psiFile instanceof PsiJavaFile)){
            return "no selected java file founded";
        }
        int position = editor.getSelectionModel().getSelectionStart();
        try {
            new SidePanel().showTestSplitEditor(event);
        } catch (Exception e) {
            e.printStackTrace();

        }



        return editor.getSelectionModel().getSelectedText();
    }
    public void startMakingTest(Project project, int position, PsiJavaFile javaFile) throws Exception{
        FileASTNode node = javaFile.getNode();
        PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, node);
        String path = javaFile.getVirtualFile().getPath();
        File file = new File(path);
        int line = positionToLineColumn(file, position);
        CompilationUnit compilationUnit = JavaParser.parse(file);
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
        LogUtils.addLog("line= "+line + " methodOpt="+methodOpt.isPresent());
        if(methodOpt.isEmpty()){
            throw new Exception("no method founded");
        }

        MethodDeclaration method = methodOpt.get();
        String methodName = method.getName();

        List<Node> dList = new ArrayList<>();
        method.accept(new VoidVisitorAdapter<List<Node>>() {
            @Override
            public void visit(Parameter n, List<Node> list) {
                list.add(n);
            }
        }, dList);

        List<Parameter> parameters = method.getParameters();

        for (Node declarator : dList) {
            LogUtils.addLog("declarator = "+declarator);
        }

        throw new Exception("method = "+dList);
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
