package io.github.binarybeing.hotcat.plugin.server.controller;

import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.server.dto.StreamResponse;
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
    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    @Override
    protected Response handle(Request request) {
        StreamResponse response = new StreamResponse();
        response.start();
        return response;

    }
}
