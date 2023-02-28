package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
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
    String path() {
        return "/api/idea/web_browser";
    }

    @Override
    protected @NotNull Response handle(Request request, AnActionEvent event, String script) {
        Project project = event.getProject();
        if (project == null || project.getProjectFile() == null) {
            return Response.error("project file is null");
        }

        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("webBrowser", new WebBrowser(project));
        return ApplicationRunnerUtils.run(() -> {
            Object result = expression.evaluate(context);
            return Response.success(result);
        });
    }
    public static class WebBrowser{
        private final Project project;
        private String tabName;
        private Url url;

        public WebBrowser(Project project) {
            this.project = project;
        }
        public WebBrowser setTabName(String tabName) {
            this.tabName = tabName;
            return this;
        }
        public WebBrowser setUrl(String url) {
            this.url = Urls.newFromEncoded(url);
            return this;
        }
        public String start(){
            BrowserLauncher.getInstance().open(url.toString());
            return "success";
        }
    }

}
