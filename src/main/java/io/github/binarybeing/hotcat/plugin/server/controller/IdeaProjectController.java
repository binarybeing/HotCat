package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author gn.binarybei
 * @date 2022/9/28
 * @note
 */
public class IdeaProjectController extends BaseEventScriptController{
    @Override
    String path() {
        return "/api/idea/project";
    }

    @Override
    protected @NotNull Response handle(Request request, AnActionEvent event, String script) {

        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return Response.error("project not found");
        }
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("project", project);
        return ApplicationRunnerUtils.run(() -> {
            Object result = expression.evaluate(context);
            return Response.success(result);
        });
    }
}
