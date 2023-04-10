package io.github.binarybeing.hotcat.plugin.panel;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.impl.ContentImpl;
import com.intellij.util.ui.EmptyIcon;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SidePanel {

    public String showTestSplitEditor(AnActionEvent event) throws InterruptedException {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return "project not found";
        }
        EditorWindow editorWindow = event.getRequiredData(EditorWindow.DATA_KEY);


        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            return "editor not found";
        }
        AnAction right = ActionManager.getInstance().getAction("MoveTabRight");
        right.actionPerformed(event);
        return "success";
    }

    public String showSidePanel(AnActionEvent event, JComponent jComponent, String panelNameId, String subTitle) throws InterruptedException {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return "project not found";
        }
        ToolWindowManager instance = ToolWindowManager.getInstance(project);
        String windowId = panelNameId;
        ToolWindow toolWindow = instance.getToolWindow(windowId);
        if (toolWindow == null) {
            RegisterToolWindowTask windowTask = RegisterToolWindowTask.lazyAndClosable(windowId, new HotCatToolWindowFactory(jComponent, subTitle), EmptyIcon.create(1), ToolWindowAnchor.RIGHT);
            toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(windowTask);
        }
        toolWindow.show(new MethodTestGeneratorPanel(event, toolWindow));
        return "success";
    }
    private static class HotCatToolWindowFactory implements ToolWindowFactory{

        private JComponent jComponent;

        private String subTitle;

        public HotCatToolWindowFactory(JComponent jComponent, String subTitle) {
            this.jComponent = jComponent;
            this.subTitle = subTitle;
        }

        @Override
        public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
            ContentImpl hotCat = new ContentImpl(jComponent, subTitle, true);
            toolWindow.getContentManager().addContent(hotCat);
        }

        @Override
        public void init(@NotNull ToolWindow toolWindow) {
            LogUtils.addLog("init");
        }

    }
}
