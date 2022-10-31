package io.github.binarybeing.hotcat.plugin.server.controller;

import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class LogController extends AbstractController {
    @Override
    String path() {
        return "/api/log";
    }
    @Override
    public Response handle(Request request) {
        if ("post".equalsIgnoreCase(request.getMethod())) {
            String log = request.getRequestParam("log");
            LogUtils.addLog(log);
            return Response.success("ok");
        }
        return Response.success(LogUtils.getLogs(20));
    }
}
