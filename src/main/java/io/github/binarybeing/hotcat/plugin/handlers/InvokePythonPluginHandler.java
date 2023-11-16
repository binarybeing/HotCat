package io.github.binarybeing.hotcat.plugin.handlers;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.IdeaEventHandler;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.server.Server;
import io.github.binarybeing.hotcat.plugin.utils.HttpClientUtils;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import io.github.binarybeing.hotcat.plugin.utils.PluginFileUtils;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class InvokePythonPluginHandler implements IdeaEventHandler {


    private static ConcurrentLinkedDeque<String> cmds = new ConcurrentLinkedDeque<>();

    public static Collection<String> getHistoryCmds() {
        return new ArrayList<>(cmds);
    }

    ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "InvokePythonPluginHandler");
        }
    });

    @Override
    public void handle(PluginEntity plugin, AnActionEvent event) throws Exception {
        Long eventId = EventContext.registerEvent(event, plugin);
        String absolutePath = plugin.getFile().getAbsolutePath();
        String cmd = "python3 '" + absolutePath + "' " + Server.INSTANCE.getPort() + " " + eventId + " '" + absolutePath+"'";
        cmds.addFirst(cmd);
        if (cmds.size() > 200) {
            cmds.removeLast();
        }
        LogUtils.addLog("Runtime execute cmd: " + cmd);
        String cmdPath = PluginFileUtils.getPluginDirName()+"/shell_runner.sh";
        ProcessBuilder builder = new ProcessBuilder("sh", cmdPath, absolutePath, String.valueOf(Server.INSTANCE.getPort()),
                String.valueOf(eventId));
        builder.redirectErrorStream(true);

        Process process = builder.start();

        handOutPut(plugin.getName(), process);
    }

    @Override
    public CompletableFuture<String> actionCallback(Long eventId, String action, String data, String callbackPath) {
        try {
            File file = new File(callbackPath);
            if (!file.exists()) {
                LogUtils.addLog("no callback file found: " + callbackPath);
                return CompletableFuture.failedFuture(new RuntimeException("no callback file found"));
            }
            Map<String, Object> map = new HashMap<>();
            map.put("action", action);
            map.put("data", new String(Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8))));
            String json = new Gson().toJson(map);
            File outPutFile = new File(PluginFileUtils.getPluginDirName() + "/python3_output.log");
            if (outPutFile.exists()) {
                outPutFile.delete();
            }
            String cmd = String.format("python3 '%s' '%s'  '%s'  '%s' '%s' '%s'", callbackPath, Server.INSTANCE.getPort(), eventId, callbackPath, json, outPutFile.getAbsolutePath());
            cmd = new String(Base64.getEncoder().encode(cmd.getBytes(StandardCharsets.UTF_8)));
            return HttpClientUtils.get("http://localhost:17022/" + cmd)
                    .thenApply(s -> {
                        try {
                            return FileUtils.readFileToString(outPutFile, StandardCharsets.UTF_8);
                        } catch (IOException e) {}
                        return "";
                    });

        } catch (Exception e) {
            LogUtils.addError(e, "callback file error: " + callbackPath);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public void callback(Long eventId, String resp, String callbackPath) {
        String cmd = "python3 '" + callbackPath + "' " + Server.INSTANCE.getPort() + " " + eventId + " '" + callbackPath +"' '" + resp+ "'";
        LogUtils.addLog("Runtime execute cmd: " + cmd);
        String cmdPath = PluginFileUtils.getPluginDirName()+"/shell_runner.sh";
        ProcessBuilder builder = new ProcessBuilder("sh", cmdPath,
                callbackPath , String.valueOf(Server.INSTANCE.getPort()), String.valueOf(eventId), resp);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            handOutPut(callbackPath, process);
        } catch (Exception e) {
            LogUtils.addError(e, "response error: " + e.getMessage());
        }

    }

    @Override
    public void handle(String name, AnActionEvent event) {
        throw new RuntimeException("unsupported method");
    }

    private void handOutPut(String name, Process process){

        executorService.submit(()->{
            try (InputStream inputStream = process.getInputStream()) {
                byte[] bytes = new byte[10240];
                int len;
                while ((len=inputStream.read(bytes)) != -1) {
                    LogUtils.addLog(name + " output: " + new String(bytes, 0, len));
                }
            } catch (Exception e) {
                LogUtils.addError(e, "handOutPut error: " + e.getMessage());
            }
        });
    }
}
