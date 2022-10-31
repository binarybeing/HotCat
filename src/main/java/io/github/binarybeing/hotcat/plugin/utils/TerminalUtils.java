package io.github.binarybeing.hotcat.plugin.utils;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalView;

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

    private static final Map<String, ShellTerminalWidget> widgetMap = new HashMap<>();

    private static final Queue<String> queue = new LinkedBlockingQueue<>();

    public static List<String> getTerminalOutPut(){
        List<String> list = new ArrayList<>(queue);
        queue.clear();
        return list;
    }

    public static String doCommand(Project project, String terminalName, String cmd, Map<String, String> conditions) throws IOException, InterruptedException {
        TerminalView terminalView = TerminalView.getInstance(project);
        Semaphore semaphore = new Semaphore(0);
        String[] res = new String[]{""};
        queue.clear();
        ApplicationManager.getApplication().invokeLater(()->{
            try {
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

                    res[0] = sb.toString();
                } catch (Exception e) {
                    LogUtils.addLog("executeCommand error " + cmd + " " + e.getMessage());
                }
            }finally {
                semaphore.release();
            }
        });
        acquire(semaphore);
        return res[0];
    }

    private static void acquire(Semaphore semaphore) {
        try {
            semaphore.acquire(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
