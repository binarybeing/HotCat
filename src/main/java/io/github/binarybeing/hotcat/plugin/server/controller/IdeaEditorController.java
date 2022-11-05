package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class IdeaEditorController extends BaseEventScriptController {
    @Override
    String path() {
        return "/api/idea/editor";
    }

    @Override
    protected Response handle(Request request, AnActionEvent event, String script) {
        DataContext dataContext = event.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            return Response.error("editor not found");
        }
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return Response.error("project not found");
        }
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        if (virtualFile == null) {
            return Response.error("virtualFile not found");
        }

        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("editor", editor);
        return ApplicationRunnerUtils.run(() -> {
            Object result = expression.evaluate(context);
            return Response.success(result);
        });
    }

}
