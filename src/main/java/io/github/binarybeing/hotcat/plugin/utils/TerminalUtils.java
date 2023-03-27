package io.github.binarybeing.hotcat.plugin.utils;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.terminal.JBTerminalWidgetListener;
import com.jediterm.terminal.*;
import com.jediterm.terminal.model.CharBuffer;
import com.jediterm.terminal.model.LinesBuffer;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.ui.TerminalPanelListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalProcessOptions;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

    private static final Map<String, ShellTerminalWidget> widgetMap = new HashMap<>();

    private static final Queue<String> queue = new LinkedBlockingQueue<>();

    public static List<String> getTerminalOutPut(){
        List<String> list = new ArrayList<>(queue);
        queue.clear();
        return list;
    }
    public static List<String> doCommandWithOutput(Project project, String terminalName, String cmd, Map<String, String> conditions) throws IOException, InterruptedException {
        TerminalView terminalView = TerminalView.getInstance(project);
        final List<String> output = new ArrayList<>();
        ShellTerminalWidget runningWidget = getShellTerminalWidget(terminalView, terminalName, cmd, conditions);
        if (runningWidget == null) {
            return Collections.emptyList();
        }
        TtyConnector connector = runningWidget.getTtyConnector();
        while (connector == null) {
            connector = runningWidget.getTtyConnector();
            Thread.sleep(500L);
        }
        while (runningWidget.hasRunningCommands()) {
            Thread.sleep(100L);
        }
        TerminalTextBuffer textBuffer = runningWidget.getTerminalTextBuffer();
        LinesBuffer historyBuffer = textBuffer.getHistoryBuffer();
        for (int i = 0; i < historyBuffer.getLineCount(); i++) {
            String s = historyBuffer.getLineText(i);
            if (StringUtils.isNotBlank(s)) {
                output.add(s.replaceAll("\\ue000", ""));
            }
        }
        for(int i = 0; i < textBuffer.getScreenLinesCount(); i++) {
            String s = textBuffer.getLine(i).getText();
            if (StringUtils.isNotBlank(s)) {
                output.add(s.replaceAll("\\ue000", ""));
            }
        }
        return output;

    }

    private static ShellTerminalWidget getShellTerminalWidget(TerminalView terminalView, String terminalName, String cmd, Map<String, String> conditions){
        AbstractTerminalRunner<?> runner = terminalView.getTerminalRunner();
        ShellTerminalWidget widget = terminalView.createLocalShellWidget(terminalName, terminalName, true);
        //Process process = runner.createProcess(new TerminalProcessOptions(null, null, null), widget);
        //widget.

        widget.addMessageFilter((s, i)->{
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
            return widget;
        } catch (Exception e) {
            LogUtils.addLog("executeCommand error " + cmd + " " + e.getMessage());
            return null;
        }
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
