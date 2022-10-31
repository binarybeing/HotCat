package io.github.binarybeing.hotcat.plugin.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.binarybeing.hotcat.plugin.server.controller.AbstractController;
import io.github.binarybeing.hotcat.plugin.server.controller.ControllerContext;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.JsonUtils;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;

/**
 * @author gn.binarybei
 * @date 2022/9/23
 * @note
 */
public class Server {
    public static final Server INSTANCE = new Server();
    private HttpServer httpServer;
    private int port = 17122;

    public void start() {
        ControllerContext.start();
        Pair<Integer, HttpServer> serverPair = doCreateServer(port);
        if (serverPair == null) {
            LogUtils.addLog("server start failed");
            return;
        }
        httpServer = serverPair.getRight();
        port = serverPair.getLeft();
        httpServer.createContext("/api", exchange -> {
            AbstractController controller = ControllerContext.get(exchange.getRequestURI().getPath());
            if (controller == null) {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
                return;
            }
            Response response;
            try {
                Request request = Request.formExchange(exchange);
                Long eventId = JsonUtils.readJsonLongValue(request.getJsonObject(), "eventId");
                String script = JsonUtils.readJsonStringValue(request.getJsonObject(), "script");
                LogUtils.addLog("eventId: " + eventId + ", script: " + script);
                response = controller.handle(request);
            } catch (Exception e) {
                LogUtils.addLog(exchange.getRequestURI().getPath()+" error: " + e.getMessage());
                response = Response.error(e.getMessage());
            }
            resp(new Gson().toJson(response), exchange);
        });
        httpServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            httpServer.stop(0);
        }));
    }

    private Pair<Integer, HttpServer> doCreateServer(int port) {
        try {
            return Pair.of(port, HttpServer.create(new InetSocketAddress("127.0.0.1", port), 10));
        } catch (BindException e) {
            LogUtils.addLog("port " + port + " is in use, try to use port " + (port + 1));
            return doCreateServer(port + 1);
        } catch (Exception e) {
            return null;
        }
    }

    private void resp(String res, HttpExchange exchange) throws IOException {
        byte[] respContents = res.getBytes("UTF-8");
        exchange.getResponseHeaders().add("Content-Type", "text/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, respContents.length);
        exchange.getResponseBody().write(respContents);
        exchange.close();
    }

    public int getPort() {
        return port;
    }
}
