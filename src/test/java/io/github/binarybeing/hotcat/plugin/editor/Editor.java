package io.github.binarybeing.hotcat.plugin.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import io.github.binarybeing.hotcat.plugin.BaseTest;

import java.text.SimpleDateFormat;

/**
 * @author gn.binarybei
 * @date 2023/3/24
 * @note
 */
public class Editor extends BaseTest{

    @Override
    public Object doExecute() throws Exception {
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
        TextAttributes attributes = new TextAttributes(null, JBColor.CYAN, JBColor.BLUE, EffectType.BOLD_DOTTED_LINE, 0);
        editor.getMarkupModel().addLineHighlighter(2, 0, attributes);
        return "你好20210";
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert data.contains("你好20210");
    }

    @Override
    public long until() throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd").parse("2023-08-30").getTime();
    }
}
