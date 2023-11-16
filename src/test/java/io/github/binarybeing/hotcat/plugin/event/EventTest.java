package io.github.binarybeing.hotcat.plugin.event;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.EmptyAction;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.impl.FakeVirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.NullVirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import io.github.binarybeing.hotcat.plugin.utils.WebPreviewVirtualFileUtils;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.SimpleDateFormat;

public class EventTest extends BaseTest {
    @Override
    public Object doExecute() throws Exception {
        EmptyAction emptyAction = new EmptyAction();
        Project project = ProjectManager.getInstance().getDefaultProject();

        FileEditor editor = TextEditorProvider.getInstance().createEditor(project, NullVirtualFile.INSTANCE);

        KeyEvent event = new KeyEvent(editor.getComponent(), KeyEvent.VK_UP, System.currentTimeMillis(), 0, 0, '0');
        ActionCallback callback = ActionManager.getInstance().tryToExecute(emptyAction, event, null, null, true);
        return project.getBasePath();
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert code == 20000;
    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-09-10";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }
}
