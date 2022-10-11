package io.github.binarybeing.hotcat.plugin.handlers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.IdeaEventHandler;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.server.Server;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;

import java.io.*;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class InvokePythonPluginHandler implements IdeaEventHandler {
    @Override
    public void handle(PluginEntity plugin, AnActionEvent event) {
        Long eventId = EventContext.registerEvent(event);
        String absolutePath = plugin.getFile().getAbsolutePath();

        String cmd = "python3 " + absolutePath + " " + Server.INSTANCE.getPort() + " " + eventId + " " + absolutePath;
        cmd = "\"" + cmd + "\"";
        cmd = cmd.replaceAll(" ", "\" \"");

        LogUtils.addLog("Runtime execute cmd: " + cmd);
        try {
            Runtime.getRuntime().exec(new String[]{"python3", absolutePath, String.valueOf(Server.INSTANCE.getPort()), String.valueOf(eventId), absolutePath});
        } catch (IOException e) {
            LogUtils.addLog("Runtime execute cmd error: " + e.getMessage());
        }
    }

    @Override
    public void handle(String name, AnActionEvent event) {
        throw new RuntimeException("unsupported method");
    }
}
