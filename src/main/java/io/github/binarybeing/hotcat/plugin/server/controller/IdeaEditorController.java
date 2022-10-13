package io.github.binarybeing.hotcat.plugin.server.controller;

import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
import io.github.binarybeing.hotcat.plugin.utils.JsonUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class IdeaEditorController extends AbstractController {
    @Override
    String path() {
        return "/api/idea/editor";
    }
    @Override
    public Response handle(Request request) {
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
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            return Response.error("editor not found");
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
