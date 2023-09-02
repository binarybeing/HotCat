package io.github.binarybeing.hotcat.plugin.utils;


import com.google.api.client.util.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author gn.binarybei
 * @date 2022/5/31
 * @note
 */
public class TerminalUtils {

    private static final Logger LOG = Logger.getInstance(TerminalUtils.class);

    private static final Map<String, ShellTerminalWidget> widgetMap = new HashMap<>();

    private static final Queue<String> queue = new LinkedBlockingQueue<>();

    public static List<String> getTerminalOutPut(){
        List<String> list = new ArrayList<>(queue);
        queue.clear();
        return list;
    }
    public static Future<List<String>> doCommandWithOutput(Project project, String terminalName, String cmd, Map<String, String> conditions) throws IOException, InterruptedException {
        TerminalView terminalView = TerminalView.getInstance(project);
        terminalView.getToolWindow().activate(()->{});
        return getShellTerminalWidget(project, terminalView, terminalName, cmd, conditions);
    }

    private static Future<List<String>> getShellTerminalWidget(Project project, TerminalView terminalView, String terminalName, String cmd, Map<String, String> conditions){
        ShellTerminalWidget widget = terminalView.createLocalShellWidget(terminalName, terminalName, true);
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        List<String> list = new ArrayList<>();
        cmd = cmd + "\n" + "echo EOF";
        widget.addMessageFilter((s, i)->{
            if(Objects.equals(s, "EOF")){
                future.complete(list);
                return null;
            }
            list.add(s);
            for (Map.Entry<String, String> entry : conditions.entrySet()) {
                if (s.startsWith(entry.getKey())) {
                    try {
                        widget.executeCommand(entry.getValue());
                    } catch (Exception e) {
                        LOG.error(e);
                    }
                }
            }
            return null;
        });
        try {
            widget.executeCommand(cmd);
        } catch (Exception e) {
            LogUtils.addLog("executeCommand error " + cmd + " " + e.getMessage());
            future.complete(Lists.newArrayList());
        }
        return future;
    }

    public static String doCommand(Project project, String terminalName, String cmd, Map<String, String> conditions) throws IOException, InterruptedException {
        TerminalView terminalView = TerminalView.getInstance(project);
        queue.clear();
        ShellTerminalWidget widget = widgetMap.remove(terminalName);
        if (widget != null) {
            widget.dispose();
        }
        ShellTerminalWidget shWidget = terminalView.createLocalShellWidget(terminalName, terminalName);
        widgetMap.put(terminalName, shWidget);
        StringBuilder sb = new StringBuilder();
        shWidget.addMessageFilter((s, i) -> {
            LOG.info("widgetSecond new line =" + s);
            //以回车结尾
            queue.offer(s);
            for (Map.Entry<String, String> entry : conditions.entrySet()) {
                if (s.startsWith(entry.getKey())) {
                    LOG.info("widgetSecond new line =" + s);
                    try {
                        shWidget.executeCommand(entry.getValue());
                    } catch (Exception e) {
                        LogUtils.addLog("executeCommand error " + entry.getValue() + " " + e.getMessage());
                    }
                }

            }
            return null;
        });

        try {
            shWidget.executeCommand(cmd);

            return sb.toString();
        } catch (Exception e) {
            LogUtils.addLog("executeCommand error " + cmd + " " + e.getMessage());
        }
        return "";
    }

}
