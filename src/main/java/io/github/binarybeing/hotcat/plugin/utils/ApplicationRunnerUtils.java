package io.github.binarybeing.hotcat.plugin.utils;

import com.intellij.openapi.application.ApplicationManager;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

/**
 * @author gn.binarybei
 * @date 2022/10/13
 * @note
 */
public class ApplicationRunnerUtils {
    public static void runWithNoException(Runnable callable) {
        try {
            run(()->{
                callable.run();
                return null;
            });
        } catch (Exception e) {
            LogUtils.addError(e, "ApplicationRunnerUtils runWithNoException error");
        }
    }

    public static <T>  T runWithNoException(Callable<T> callable) {
        try {
            return run(callable);
        } catch (Exception e) {
            LogUtils.addError(e, "ApplicationRunnerUtils runWithNoException error");
            return null;
        }
    }

    public static <T>  T run(Callable<T> callable) throws Exception {
        Semaphore semaphore = new Semaphore(0);
        final Object[] res = new Object[2];
//        ApplicationManager.getApplication().invokeLater();

        ApplicationManager.getApplication().invokeLater(()->{
            try {
                res[0] = callable.call();
            }catch (Exception e) {
                res[1] = e;
                LogUtils.addLog("ApplicationRunnerUtils run error "+ e.getMessage());
            }finally {
                semaphore.release();
            }
        });
        try {
            semaphore.acquire();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.addLog("ApplicationRunnerUtils run error, semaphore.acquire error "+ e.getMessage());
        }
        if (res[1] != null) {
            throw (Exception) res[1];
        }
        //noinspection unchecked
        return (T) res[0];
    }
}
