package io.github.binarybeing.hotcat.plugin.utils;


import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.impl.ConsoleViewUtil;
import com.intellij.execution.process.*;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.terminal.TerminalExecutionConsole;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.containers.IntObjectMap;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalTabs;
import com.jediterm.terminal.ui.TerminalTabsImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;
import org.jetbrains.plugins.terminal.JBTabbedTerminalWidget;
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner;
import org.jetbrains.plugins.terminal.TerminalView;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

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
            return ApplicationRunnerUtils.run(()->{
                String scirpt = String.format("tell application \"Terminal\" to do script \"%s\"  in window 1", cmd);
                Runtime.getRuntime().exec(new String[]{"osascript", "-e", "tell application \"Terminal\" to activate",
                        "-e", scirpt});
                return Collections.emptyList();
            });
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
