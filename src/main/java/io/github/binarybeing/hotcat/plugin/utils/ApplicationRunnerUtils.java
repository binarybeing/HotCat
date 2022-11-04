package io.github.binarybeing.hotcat.plugin.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.impl.file.impl.FileManager;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

/**
 * @author gn.binarybei
 * @date 2022/10/13
 * @note
 */
public class ApplicationRunnerUtils {

    public static <T>  T run(Callable<T> callable){
        Semaphore semaphore = new Semaphore(0);
        final Object[] res = new Object[1];
        ApplicationManager.getApplication().invokeLater(()->{
            try {
                res[0] = callable.call();
            }catch (Exception e) {
                e.printStackTrace();
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

        //noinspection unchecked
        return (T) res[0];
    }
}
