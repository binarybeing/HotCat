package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;

public class HotCatVersionController extends BaseEventScriptController{
    @Override
    public String path() {
        return "/api/idea/hotcat/version";
    }

    @Override
    protected Response handle(Request request, AnActionEvent event, String script){
        return Response.success("1.3.6.193231.3");
    }
}
