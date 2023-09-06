package io.github.binarybeing.hotcat.plugin.utils;


import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.junit.Assert;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author gn.binarybei
 * @date 2022/5/31
 * @note
 */
public class TerminalUtils {

    private static final Logger LOG = Logger.getInstance(TerminalUtils.class);

    private static final Map<String, ShellTerminalWidget> widgetMap = new ConcurrentHashMap<>();

    private static final Queue<String> queue = new LinkedBlockingQueue<>();

    public static List<String> getTerminalOutPut(){
        List<String> list = new ArrayList<>(queue);
        queue.clear();
        return list;
    }
    public static Future<List<String>> doCommandWithOutput(Project project, String terminalName, String cmd, Map<String, String> conditions) throws Exception {
        return getShellTerminalWidget(project, terminalName, cmd, conditions);
    }

    private static ShellTerminalWidget reflectGetShellTerminalWidget(Project project, String tabName) throws Exception{
        try {
            String clazzName = "org.jetbrains.plugins.terminal.TerminalToolWindowManager";
            Class<?> clazz = Class.forName(clazzName);
            //TerminalToolWindowManager windowManager = TerminalToolWindowManager.getInstance(project);
            Method method = clazz.getMethod("getInstance", Project.class);
            //TerminalToolWindowManager object
            Object invoke = method.invoke(null, project);
            //invoke.createLocalShellWidget("~", terminalName, true);
            Method createLocalShellWidget = clazz.getMethod("createLocalShellWidget", String.class, String.class, boolean.class);
            return (ShellTerminalWidget)createLocalShellWidget.invoke(invoke, "~", tabName, true);
        } catch (ClassNotFoundException e) {
            //TerminalView terminalView = TerminalView.getInstance(project);
            //ShellTerminalWidget widget = terminalView.createLocalShellWidget(terminalName, terminalName, true);
            String clazzName = "org.jetbrains.plugins.terminal.TerminalView";
            Class<?> clazz = Class.forName(clazzName);
            Method method = clazz.getMethod("getInstance", Project.class);
            Object invoke = method.invoke(null, project);
            Method createLocalShellWidget = clazz.getMethod("createLocalShellWidget", String.class, String.class, boolean.class);
            return (ShellTerminalWidget)createLocalShellWidget.invoke(invoke, tabName, tabName, true);
        }
    }

    private static Future<List<String>> getShellTerminalWidget(Project project, String terminalName, String cmd, Map<String, String> conditions) throws Exception{
        ShellTerminalWidget widget = reflectGetShellTerminalWidget(project, terminalName);
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
            future.complete(new ArrayList<>());
        }
        return future;
    }

    public static String doCommand(Project project, String terminalName, String cmd,
                                   Map<String, String> conditions, boolean useExist,
                                   boolean visible, boolean execute) throws Exception {
        queue.clear();
        ShellTerminalWidget widget = widgetMap.remove(terminalName);
        ShellTerminalWidget shWidget = null;
        if (useExist && widget !=null) {
            shWidget = widget;
        }
        if (useExist && widget == null){
            shWidget = reflectGetShellTerminalWidget(project, terminalName);
        }
        if(!useExist && widget != null){
            widget.dispose();
            shWidget = reflectGetShellTerminalWidget(project, terminalName);
        }
        if(!useExist && widget == null){
            shWidget = reflectGetShellTerminalWidget(project, terminalName);
        }
        if (visible) {
            shWidget.grabFocus();
        }
        shWidget.setVisible(visible);
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
                        widgetMap.get(terminalName).executeCommand(entry.getValue());
                    } catch (Exception e) {
                        LogUtils.addLog("executeCommand error " + entry.getValue() + " " + e.getMessage());
                    }
                }

            }
            return null;
        });

        try {
            if (execute) {
                shWidget.executeCommand(cmd);
            } else {

                Assert.assertNotNull("shWidget.getTtyConnector() is null", shWidget.getTtyConnector());
                shWidget.getTtyConnector().write(cmd.getBytes(StandardCharsets.UTF_8));
            }
            return sb.toString();
        } catch (Exception e) {
            LogUtils.addLog("executeCommand error " + cmd + " " + e.getMessage());
        }
        return "";
    }

}
