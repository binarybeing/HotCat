package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.JsonUtils;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author gn.binarybei
 * @date 2022/11/5
 * @note
 */
public abstract class BaseEventScriptController extends AbstractController{
    @Override
    protected @NotNull Response handle(Request request) {
        Long eventId = JsonUtils.readJsonLongValue(request.getJsonObject(), "eventId");
        AnActionEvent event = EventContext.getEvent(eventId);
        if (checkEventId()) {
            if (event == null) {
                return Response.error("event not found");
            }
        }
        String script = JsonUtils.readJsonStringValue(request.getJsonObject(), "script");
        if (checkScript()) {
            if (StringUtils.isEmpty(script)) {
                return Response.error("script is empty");
            }
        }
        try {
            LogUtils.addEventLogs(eventId,"handleRequest: " + request);
            LogUtils.addLog("handleRequest: " + request);
            return handle(request, event, script);
        } catch (Exception e) {
            LogUtils.addEventLogs(eventId,"handleRequest error: " + e);
            return Response.error(e.getMessage());
        }
    }
    protected abstract Response handle(Request request, AnActionEvent event, String script);
}
