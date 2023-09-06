package io.github.binarybeing.hotcat.plugin.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.impl.ContentImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SidePanelUtils {

    private static Map<String, Content> contentMap = new ConcurrentHashMap<>();
    public static String showSidePanel(AnActionEvent event, String panelNameId, String subTitle,
                                JComponent jComponent, Runnable task) throws InterruptedException {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return "project not found";
        }
        ToolWindowManager instance = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = instance.getToolWindow("HotCat");
        if (toolWindow == null) {
            return "toolWindow not found";
        }
        ContentManager contentManager = toolWindow.getContentManager();
        Content managerContent = contentManager.findContent(panelNameId);
        if (managerContent != null) {
             contentManager.removeContent(managerContent, true);
        }
        Content content = new ContentImpl(jComponent, panelNameId, true);
        contentManager.addContent(content);
        contentManager.setSelectedContent(content);
        toolWindow.show(task);
        return "success";
    }
    private static class HotCatToolWindowFactory implements ToolWindowFactory {

        private Content selected;

        protected static ToolWindow toolWindow;

        public HotCatToolWindowFactory() {
        }
        public void setComponent(String title, JComponent jComponent){
            selected = new ContentImpl(jComponent, title, true);
        }
        @Override
        public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        }

        @Override
        public void init(@NotNull ToolWindow toolWindow) {
            HotCatToolWindowFactory.toolWindow = toolWindow;
            toolWindow.setToHideOnEmptyContent(true);
            LogUtils.addLog(toolWindow.getTitle() + " inited");
        }

    }
}
