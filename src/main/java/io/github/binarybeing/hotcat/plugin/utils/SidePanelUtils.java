package io.github.binarybeing.hotcat.plugin.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.intellij.ui.content.impl.ContentImpl;
import com.intellij.util.ui.EmptyIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SidePanelUtils {
    public static String showSidePanel(AnActionEvent event, String panelNameId, String subTitle,
                                JComponent jComponent, Runnable task) throws InterruptedException {
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
        } else {
            for (Content content : toolWindow.getContentManager().getContents()) {
                toolWindow.getContentManager().removeContent(content, true);
            }
            toolWindow.getContentManager().addContent(new ContentImpl(jComponent, subTitle, true), 0);
        }
        final ToolWindow toShow = toolWindow;
        toShow.getContentManager().addContentManagerListener(new ContentManagerListener() {
            @Override
            public void contentRemoved(@NotNull ContentManagerEvent event) {
                toShow.remove();
            }

        });
        toShow.show(task);
        return "success";
    }
    private static class HotCatToolWindowFactory implements ToolWindowFactory {

        private JComponent jComponent;

        private String subTitle;

        public HotCatToolWindowFactory(JComponent jComponent, String subTitle) {
            this.jComponent = jComponent;
            this.subTitle = subTitle;
        }

        @Override
        public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
            ContentImpl content = new ContentImpl(jComponent, subTitle, true);
            toolWindow.getContentManager().addContent(content);
        }

        @Override
        public boolean isApplicable(@NotNull Project project) {
            return ToolWindowFactory.super.isApplicable(project);
        }

        @Override
        public boolean shouldBeAvailable(@NotNull Project project) {
            return ToolWindowFactory.super.shouldBeAvailable(project);
        }

        @Override
        public void init(@NotNull ToolWindow toolWindow) {
            LogUtils.addLog(subTitle + " inited");
        }

    }
}
