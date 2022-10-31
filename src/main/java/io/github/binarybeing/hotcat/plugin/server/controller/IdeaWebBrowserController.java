package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.ide.actions.OpenFileAction;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.ide.browsers.actions.WebPreviewVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
import io.github.binarybeing.hotcat.plugin.utils.JsonUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author gn.binarybei
 * @date 2022/10/13
 * @note
 */
public class IdeaWebBrowserController extends AbstractController{
    @Override
    String path() {
        return "/api/idea/web_browser";
    }

    @Override
    public @NotNull Response handle(Request request) {


        Long eventId = JsonUtils.readJsonLongValue(request.getJsonObject(), "eventId");
        String script = JsonUtils.readJsonStringValue(request.getJsonObject(), "script");

        AnActionEvent event = EventContext.getEvent(eventId);
        if (event == null) {
            return Response.error("event not found");
        }
        if (StringUtils.isEmpty(script)) {
            return Response.error("script is empty");
        }
        Project project = event.getProject();
        if (project == null || project.getProjectFile() == null) {
            return Response.error("project file is null");
        }
        WebBrowserManager manager = WebBrowserManager.getInstance();
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
            WebPreviewVirtualFile file = new NatCatPreviewVirtualFile(project.getProjectFile(), tabName, url);
            OpenFileAction.openFile(file, project);
            return "success";
        }
    }
    public static class NatCatPreviewVirtualFile extends  WebPreviewVirtualFile {
        private String tabName;
        public NatCatPreviewVirtualFile(@NotNull VirtualFile virtualFile, String tabName, @NotNull Url url) {
            super(virtualFile, url);
            this.tabName = tabName;
        }

        @Override
        public @NlsSafe @NotNull String getName() {
            return StringUtils.isEmpty(tabName) ? "HotCat" : tabName;
        }

        @Override
        public void copyUserDataTo(@NotNull UserDataHolderBase other) {
            super.copyUserDataTo(other);
        }
    }
}
