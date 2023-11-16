package io.github.binarybeing.hotcat.plugin.utils;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.server.Server;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.internal.Engine;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class ScriptUtils {
    private static final JexlEngine jexlEngine = new Engine();

    public static Object runJava(AnActionEvent event, String cmd){
        try {
            JexlExpression expression = jexlEngine.createExpression(cmd);
            JexlContext context = new MapContext();
            context.set("event", event);
            return expression.evaluate(context);
        } catch (Exception e) {
            LogUtils.addLog("script error: " + e.getMessage() + " script=" + cmd + " "+ e.getStackTrace()[0]);
        }
        return null;
    }

    public static void runInit(PluginEntity entity){
        EventContext.empyEvent()
                .thenAccept(e -> {
                    Long eventId = EventContext.registerEvent(e, entity);
                    String path = entity.getFile().getAbsolutePath();
                    if (entity.getFile().isFile()) {
                        path = entity.getFile().getParentFile().getAbsolutePath();
                    }
                    if (!path.endsWith("/")) {
                        path += "/";
                    }
                    path += "init.py";
                    File file = new File(path);
                    if (file.exists()) {
                        run("python3", path, Server.INSTANCE.getPort(), eventId, path);
                    }
        });
    }

    public static void runPreLoad(PluginEntity entity, String action, String data, String outputFile){

        EventContext.empyEvent().thenAccept(e->{
            Long eventId = EventContext.registerEvent(e, entity);
            String path = entity.getFile().getAbsolutePath();
            if (entity.getFile().isFile()) {
                path = entity.getFile().getParentFile().getAbsolutePath();
            }
            if (!path.endsWith("/")) {
                path += "/";
            }
            path += "pre_load.py";

            Map<String, Object> map = new HashMap<>();
            map.put("action", action);
            map.put("data", new String(Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8))));

            File file = new File(path);
            if (file.exists()) {
                run("python3", path, Server.INSTANCE.getPort(), eventId, path, new Gson().toJson(map), outputFile);
            }
        });

    }

    public static CompletableFuture<String> commonPython3Run(Long eventId, String path, String action, String data, boolean needResponse) {
        Map<String, Object> map = new HashMap<>();
        map.put("action", action);
        map.put("data", new String(Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8))));
        File file = new File(path);
        if (file.exists()) {
            String output = file.getParent();
            if (!output.endsWith("/")) {
                output += "/";
            }
            output += "output.log";
            String outputFile = output;
            return run("python3", path, Server.INSTANCE.getPort(), eventId, path, new Gson().toJson(map), outputFile)
                    .thenCompose(s -> {
                        if (!needResponse) {
                            return CompletableFuture.completedFuture("");
                        }
                        File outputRes = new File(outputFile);
                        if (outputRes.exists()) {
                            try {
                                CompletableFuture<String> future = CompletableFuture.completedFuture(FileUtils.readFileToString(outputRes, StandardCharsets.UTF_8));
                                return future;
                            } catch (Exception e) {
                                return CompletableFuture.failedFuture(e);
                            }
                        } else {
                            return CompletableFuture.failedFuture(new RuntimeException("no out put file exist"));
                        }
                    });
        }
        return CompletableFuture.failedFuture(new RuntimeException("path not exist " + path));
    }

    /**
     *
     * @param cmd
     * @return
     */
    public static CompletableFuture<String> run(String cmd, Object...args){
        StringBuilder sb = new StringBuilder();
        sb.append(cmd).append(" ");
        for (Object arg : args) {
            sb.append("'").append(arg).append("' ");
        }
        cmd = sb.toString();
        cmd = new String(Base64.getEncoder().encode(cmd.getBytes(StandardCharsets.UTF_8)));
        return HttpClientUtils.get("http://localhost:17022/" + cmd);
    }


    public static CompletableFuture<String> runPython3(String cmd, String[] args){
        try {
//            Runtime runtime = Runtime.getRuntime();
            String cmdPath = PluginFileUtils.getPluginDirName()+"/shell_runner.sh";
            List<String> list = new ArrayList<>();
            list.add("sh");
            list.add(cmdPath);
            list.add(cmd);
            for (String arg : args) {
                list.add(arg);
            }
            ProcessBuilder processBuilder = new ProcessBuilder(list.toArray(new String[0]));
            Process process = processBuilder.start();
            CompletableFuture<Process> future = process.onExit();
            return future.thenApply(p->{
                try {
                    InputStream errorStream = p.getErrorStream();
                    byte[] errorBytes = errorStream.readAllBytes();
                    String errorInfo = new String(errorBytes);
                    if (StringUtils.isNoneBlank(errorInfo)) {
                        LogUtils.addLog(list.toString() + "; exec error info: " + errorInfo);
                        throw new RuntimeException(list.toString() + "; exec error");
                    }
                    InputStream inputStream = p.getInputStream();
                    String res = new String(inputStream.readAllBytes());
                    return res.trim();
                } catch (Exception e) {
                    return null;
                }
            });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

}
