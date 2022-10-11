package io.github.binarybeing.hotcat.plugin.server.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class Request {

    private String uri;

    private String method;

    private JsonObject jsonObject;

    public Request(String uri, String method, JsonObject jsonObject) {
        this.uri = uri;
        this.method = method;
        this.jsonObject = jsonObject;
    }

    public static Request formExchange(HttpExchange exchange) {
        try {
            byte[] bytes = exchange.getRequestBody().readAllBytes();
            String method = exchange.getRequestMethod();
            String s = new String(bytes, StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(s).getAsJsonObject();
            return new Request(exchange.getRequestURI().getPath(), method, jsonObject);
        } catch (IOException e) {
            LogUtils.addLog("request error: " + e.getMessage());
        }
        return new Request("", "get", new JsonObject());
    }

    public String getUri() {
        return uri;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public String getRequestParam(String paramName) {
        if (jsonObject.has(paramName)) {
            return jsonObject.get(paramName).getAsString();
        }
        return null;
    }

    public String getMethod() {
        return method;
    }
}
