package io.github.binarybeing.hotcat.plugin.terminal;

import com.google.api.client.util.Lists;
import com.intellij.execution.filters.Filter;
import com.intellij.ide.x.c.E;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import io.github.binarybeing.hotcat.plugin.utils.DialogUtils;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalProcessOptions;
import org.jetbrains.plugins.terminal.TerminalView;

import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class TerminalTest extends BaseTest {
    @Override
    public Object doExecute() throws Exception {
        Project project = event.getProject();
        TerminalView terminalView = TerminalView.getInstance(project);
        ShellTerminalWidget widget = terminalView.createLocalShellWidget("", "test", true);

        CompletableFuture<List<String>> future = new CompletableFuture<>();
        List<String> list = new ArrayList<>();
        String cmd = "echo 123";
        cmd = cmd + "\n" + "echo EOF";
        widget.addMessageFilter((s, i)->{
            if(Objects.equals(s, "EOF")){
                future.complete(list);
                return null;
            }
            list.add(s);
            return null;
        });
        try {
            widget.executeCommand(cmd);
        } catch (Exception e) {
            LogUtils.addLog("executeCommand error " + cmd + " " + e.getMessage());
            future.complete(Lists.newArrayList());
        }
        return "ok";
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert code == 200;
    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-09-04";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }
}
