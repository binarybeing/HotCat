package io.github.binarybeing.hotcat.plugin.server.dto;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class Request {

    private String uri;

    private String urlQuery;

    private String method;

    private JsonObject jsonObject;

    public Request(String uri, String urlQuery, String method, JsonObject jsonObject) {
        this.uri = uri;
        this.method = method;
        this.jsonObject = jsonObject;
        this.urlQuery = urlQuery;
    }

    public static Request formExchange(HttpExchange exchange) {
        try {
            InputStream stream = exchange.getRequestBody();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = stream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.close();
            bytes = outputStream.toByteArray();

            String method = exchange.getRequestMethod();
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();
            String s = new String(bytes, StandardCharsets.UTF_8);
            if (StringUtils.isBlank(s)) {
                return new Request(requestURI.getPath(), query, method, new JsonObject());
            }
            JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();
            return new Request(requestURI.getPath(), query, method, jsonObject);
        } catch (Exception e) {
            LogUtils.addLog("request error: " + e.getMessage());
        }
        return new Request("", "", "get", new JsonObject());
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

    public String getUrlQuery() {
        return urlQuery;
    }

    @Override
    public String toString() {
        return "Request{" +
                "uri='" + uri + '\'' +
                ", urlQuery='" + urlQuery + '\'' +
                ", method='" + method + '\'' +
                ", jsonObject=" + jsonObject +
                '}';
    }
}
