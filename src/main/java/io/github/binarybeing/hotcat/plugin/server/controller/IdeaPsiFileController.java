package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiFile;
import com.intellij.remoteServer.ServerType;
import com.intellij.remoteServer.configuration.RemoteServer;
import com.intellij.remoteServer.configuration.ServerConfiguration;
import com.intellij.remoteServer.runtime.deployment.debug.DebuggerLauncher;
import com.intellij.remoteServer.runtime.deployment.debug.JavaDebugConnectionData;
import com.intellij.remoteServer.runtime.deployment.debug.JavaDebuggerLauncher;
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
 * @date 2022/9/28
 * @note
 */
public class IdeaPsiFileController extends AbstractController {
    @Override
    String path() {
        return "/api/idea/psi_file";
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

        DataContext dataContext = event.getDataContext();
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        if (psiFile == null) {
            return Response.error("psiFile not found");
        }
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("psiFile", psiFile);
        return ApplicationRunnerUtils.run(() -> {
            Object result = expression.evaluate(context);
            return Response.success(result);
        });
    }

    private void playLand(PsiFile psiFile){
        psiFile.getFileType().getName();
        psiFile.getVirtualFile().getPath();
        Project project = psiFile.getProject();

    }

}
