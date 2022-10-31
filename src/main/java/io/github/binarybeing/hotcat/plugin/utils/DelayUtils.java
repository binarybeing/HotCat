package io.github.binarybeing.hotcat.plugin.utils;

/**
 * @author gn.binarybei
 * @date 2022/6/8
 * @note
 */
public class DelayUtils {

    public static void run(Runnable runnable, long delayMillis) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                runnable.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        thread.start();

    }
}
