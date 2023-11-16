package io.github.binarybeing.hotcat.plugin.server;

import io.github.binarybeing.hotcat.plugin.utils.PluginFileUtils;
import io.github.binarybeing.hotcat.plugin.utils.ScriptUtils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Python3Server {
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    private boolean started = false;
    public void start() {
        service.submit(this::stop);
        service.scheduleAtFixedRate(this::check, 10, 10, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public boolean isStarted() {
        return started;
    }

    public void stop() {
        try {
            Process process = Runtime.getRuntime().exec("curl localhost:17022/c2h1dGRvd24=");
            process.waitFor();
        } catch (Exception e) {
            //ignore
        }

    }

    private void check() {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(17022);
        Socket socket = new Socket();
        try {
            socket.connect(inetSocketAddress, 3000);
            started = true;
        } catch (Exception e) {
            String s = PluginFileUtils.getPluginDirName() + "/python_script_executor.py";
            CompletableFuture<String> future = ScriptUtils.runPython3(s, new String[0]);
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (Exception ex) {}
            started = true;
        }finally {
            try {
                socket.close();
            } catch (Exception e) {}
        }
    }

}
