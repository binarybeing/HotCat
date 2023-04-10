package io.github.binarybeing.hotcat.plugin.panel;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import io.github.binarybeing.hotcat.plugin.javaassist.Javaassistor;

import javax.swing.*;

public class MethodTestGeneratorPanel implements Runnable{
    private ToolWindow toolWindow;
    private AnActionEvent event;
    int time = 0;
    public MethodTestGeneratorPanel(AnActionEvent event, ToolWindow toolWindow){
        this.toolWindow = toolWindow;
        this.event = event;
    }

    @Override
    public void run() {
        Javaassistor javaassistor = new Javaassistor();
        DataContext dataContext = event.getDataContext();
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        if(!(psiFile instanceof PsiJavaFile)){
            throw new RuntimeException("no selected java file founded");
        }
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            throw new RuntimeException("editor not found");
        }
        int position = editor.getSelectionModel().getSelectionStart();
        try {
            javaassistor.startMakingTest(event.getProject(), position, (PsiJavaFile) psiFile);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
