package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author gn.binarybei
 * @date 2022/9/28
 * @note
 */
public class IdeaVirtualFileController extends BaseEventScriptController{
    @Override
    String path() {
        return "/api/idea/virtual_file";
    }

    @Override
    protected @NotNull Response handle(Request request, AnActionEvent event, String script) {

        DataContext dataContext = event.getDataContext();
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        if (virtualFile == null) {
            return Response.error("virtualFile not found");
        }
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("virtualFile", virtualFile);
        Object result = expression.evaluate(context);
        return Response.success(result);
    }

    private void test(VirtualFile virtualFile) {

    }
}
