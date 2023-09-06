package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author gn.binarybei
 * @date 2022/10/13
 * @note
 */
public class IdeaWebBrowserController extends BaseEventScriptController{
    @Override
    public String path() {
        return "/api/idea/web_browser";
    }

    @Override
    protected @NotNull Response handle(Request request, AnActionEvent event, String script) {
        Project project = event.getProject();
        if (project == null || project.getProjectFile() == null) {
            return Response.error("project file is null");
        }
        WebBrowserManager manager = WebBrowserManager.getInstance();
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("webBrowser", new WebBrowser(event, project));
        Object result = expression.evaluate(context);
        return Response.success(result);
    }
    public static class WebBrowser{
        private final Project project;
        private final AnActionEvent event;

        private String tabName;
        private String url;

        public WebBrowser(AnActionEvent event, Project project) {
            this.event = event;
            this.project = project;
        }
        public WebBrowser setTabName(String tabName) {
            this.tabName = tabName;
            return this;
        }
        public WebBrowser setUrl(String url) {
            this.url = url;
            return this;
        }
        public String start() throws Exception {
            new IdeaPanelController.IdeaPanel(event).showSidePanelWebBrowser(tabName, url);
            return "success";
        }
    }

}
