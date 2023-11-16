package io.github.binarybeing.hotcat.plugin.hints;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import io.github.binarybeing.hotcat.plugin.infra.hint.HotCatFactoryInlayHintsCollector;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

public class FocusTest extends BaseTest {
    @Override
    public Object doExecute() throws Exception {
//        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
//            Desktop.getDesktop().browse(new URI("https://www.baidu.com"));
////        }
//        String s = "sxx";
//        // PsiJavaToken
//        List<String> list = new ArrayList<>();
//        PsiElement element = super.psiFile.findElementAt(super.editor.getSelectionModel().getSelectionStart());
//        list.add(element.getText());
//        list.add(String.valueOf(element.getTextOffset()));
//        for (PsiElement child : element.getChildren()) {
//            list.add(child.getClass().getSimpleName());
//        }
        FileEditorManager manager = FileEditorManager.getInstance(project);
        FileEditor fileEditor = manager.getSelectedEditor();

        StringBuilder sb = new StringBuilder();
        sb.append(fileEditor.getPreferredFocusedComponent().isFocusOwner());



//        VirtualFile file = fileEditor.getFile();
//
//        IdeFocusManager focusManager = IdeFocusManager.getInstance(project);
//        Component focusOwner = focusManager.getFocusOwner();
//        JComponent owner = (JComponent) focusOwner;
//        Object property = owner.getClientProperty("editor");
//        boolean b = property != null;


        return fileEditor.getName() +" " + sb.toString();
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert code == 200;
    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-11-25";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }
}
