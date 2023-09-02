package io.github.binarybeing.hotcat.plugin.terminal;

import io.github.binarybeing.hotcat.plugin.BaseTest;

import java.text.SimpleDateFormat;

public class TerminalTest extends BaseTest {
//    @Override
//    public Object doExecute() throws Exception {
//        Project project = event.getProject();
////        TerminalToolWindowManaer windowManager = TerminalToolWindowManager.getInstance(project);
//
//        ShellTerminalWidget widget = windowManager.createLocalShellWidget("~", "test", true);
//
//        CompletableFuture<List<String>> future = new CompletableFuture<>();
//        List<String> list = new ArrayList<>();
//        String cmd = "echo 123";
//        cmd = cmd + "\n" + "echo EOF";
//        widget.addMessageFilter((s, i)->{
//            if(Objects.equals(s, "EOF")){
//                future.complete(list);
//                return null;
//            }
//            list.add(s);
//            return null;
//        });
//        try {
//            widget.executeCommand(cmd);
//        } catch (Exception e) {
//            LogUtils.addLog("executeCommand error " + cmd + " " + e.getMessage());
//            future.complete(Lists.newArrayList());
//        }
//        return "ok";
//    }

    @Override
    public Object doExecute() throws Exception {
        return null;
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
