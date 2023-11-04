package io.github.binarybeing.hotcat.plugin.runtime;

import com.google.gson.Gson;
import io.github.binarybeing.hotcat.plugin.BaseTest;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ProcessTest extends BaseTest {
    @Override
    public Object doExecute() throws Exception {
        try {
            if (true) {
                Map<String, Object> map = new HashMap<>();
                map.put("action", "1_action");
                map.put("data", Base64.getEncoder().encode("hello\nadsf".getBytes(StandardCharsets.UTF_8)));
                String json = new Gson().toJson(map);
                return json;
            }

            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("python3 /Users/momo/plugin_projects/HotCat/src/pytest/test.py");
            CompletableFuture<Process> future = process.onExit();
            Process processRes = future.get(3, TimeUnit.SECONDS);
            InputStream errorStream = processRes.getErrorStream();
            InputStream inputStream = processRes.getInputStream();
            byte[] bytes1 = inputStream.readAllBytes();
            String res = new String(bytes1);
            return res.trim();
        } catch (Exception e) {
            return e.getMessage();
        }

    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert code == 200;
    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-11-25";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }
}
