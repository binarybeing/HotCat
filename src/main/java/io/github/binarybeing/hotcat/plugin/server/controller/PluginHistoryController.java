package io.github.binarybeing.hotcat.plugin.server.controller;

import io.github.binarybeing.hotcat.plugin.handlers.InvokePythonPluginHandler;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;

/**
 * @author gn.binarybei
 * @date 2022/11/5
 * @note
 */
public class PluginHistoryController extends AbstractController {
    @Override
    String path() {
        return "/api/plugin/history";
    }

    @Override
    protected Response handle(Request request) {
        return Response.success(InvokePythonPluginHandler.getHistoryCmds());
    }
}
