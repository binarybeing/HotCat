package io.github.binarybeing.hotcat.plugin.server.controller;

import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.jetbrains.annotations.NotNull;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public abstract class AbstractController {
    protected JexlEngine jexlEngine = new JexlBuilder().create();
    public abstract String path();

    public Response handleRequest(Request request){
        return handle(request);
    }

    protected abstract @NotNull Response handle(Request request);

    protected boolean checkEventId(){
        return true;
    }
    protected boolean checkScript(){
        return true;
    }
}
