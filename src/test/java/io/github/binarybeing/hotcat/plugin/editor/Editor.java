package io.github.binarybeing.hotcat.plugin.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Locale;

/**
 * @author gn.binarybei
 * @date 2023/3/24
 * @note
 */
public class Editor {

    public String getSelectText(AnActionEvent event) throws InterruptedException {

        DataContext dataContext = event.getDataContext();

        com.intellij.openapi.editor.Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            return "editor not found";
        }
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return "project not found";
        }
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        if (virtualFile == null) {
            return "project not found";
        }
        return editor.getSelectionModel().getSelectedText().toUpperCase(Locale.ROOT);
    }
}
