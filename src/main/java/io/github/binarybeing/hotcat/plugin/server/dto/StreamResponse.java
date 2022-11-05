package io.github.binarybeing.hotcat.plugin.server.dto;

import io.github.binarybeing.hotcat.plugin.utils.LogUtils;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gn.binarybei
 * @date 2022/11/5
 * @note
 */
public class StreamResponse extends Response {
    private LinkedBlockingDeque<String> queue = new LinkedBlockingDeque<>();
    private AtomicBoolean isEnd = new AtomicBoolean(false);

    public void resp(String msg) {
        queue.add(msg);
    }
    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public void start(){
        new Thread(()->{
            while (!getIsEnd()) {
                for (String log : LogUtils.getLogs(20)) {
                    resp(log);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void end() {
        isEnd.set(true);
    }

    public LinkedBlockingDeque<String> getQueue() {
        return queue;
    }

    public boolean getIsEnd() {
        return isEnd.get();
    }
}
