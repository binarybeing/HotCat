package io.github.binarybeing.hotcat.plugin.handlers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.IdeaEventHandler;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.server.Server;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedDeque;

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

    @Override
    public void handle(PluginEntity plugin, AnActionEvent event) {
        Long eventId = EventContext.registerEvent(event);
        String absolutePath = plugin.getFile().getAbsolutePath();
        String cmd = "python3 '" + absolutePath + "' " + Server.INSTANCE.getPort() + " " + eventId + " '" + absolutePath+"'";
        cmds.addFirst(cmd);
        if (cmds.size() > 10) {
            cmds.removeLast();
        }
        LogUtils.addLog("Runtime execute cmd: " + cmd);
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"python3", absolutePath, String.valueOf(Server.INSTANCE.getPort()), String.valueOf(eventId), absolutePath});
            process.onExit().thenAccept(p -> {
                try {
                    InputStream inputStream = p.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LogUtils.addLog(plugin.getName()+" output: "+ line);
                    }
                    InputStream errorStream = p.getErrorStream();
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                    while ((line = errorReader.readLine()) != null) {
                        LogUtils.addLog(plugin.getName()+" error info: "+ line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            LogUtils.addLog("Runtime execute cmd error: " + e.getMessage());
        }
    }

    @Override
    public void handle(String name, AnActionEvent event) {
        throw new RuntimeException("unsupported method");
    }
}
