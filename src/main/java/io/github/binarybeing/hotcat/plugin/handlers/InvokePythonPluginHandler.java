package io.github.binarybeing.hotcat.plugin.handlers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.IdeaEventHandler;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.server.Server;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import io.github.binarybeing.hotcat.plugin.utils.PluginFileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
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
