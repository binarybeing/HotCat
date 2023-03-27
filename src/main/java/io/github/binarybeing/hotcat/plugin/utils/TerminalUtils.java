package io.github.binarybeing.hotcat.plugin.utils;


import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.jediterm.terminal.TtyConnector;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * @author gn.binarybei
 * @date 2022/5/31
 * @note
 */
public class TerminalUtils {

    private static final Logger LOG = Logger.getInstance(TerminalUtils.class);

    private static final Queue<String> queue = new LinkedBlockingQueue<>();

    public static List<String> getTerminalOutPut(){
        List<String> list = new ArrayList<>(queue);
        queue.clear();
        return list;
    }
    private static void doExecuteCommand(@NotNull String shellCommand, @NotNull TtyConnector connector) throws IOException {
        StringBuilder result = new StringBuilder();
        result.append(shellCommand).append("\r\n");
        connector.write(result.toString());
    }
    public static List<String> doCommandWithOutput(Project project, String terminalName, String cmd, Map<String, String> conditions) throws IOException, InterruptedException {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("Terminal");
        if(toolWindow == null){
            DialogUtils.showError("Terminal not found", "the terminal tool window not found");
            return Collections.emptyList();
        }
        try {
            String script = String.format("tell application \"Terminal\" to do script \"%s\" ", cmd);
            Runtime.getRuntime().exec(new String[]{"osascript", "-e", script});
            return Collections.emptyList();
        }catch (Exception e){
            DialogUtils.showError("Terminal execute error", e.getMessage());
            return Collections.emptyList();
        }
    }

    private static void acquire(Semaphore semaphore) {
        try {
            semaphore.acquire(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
