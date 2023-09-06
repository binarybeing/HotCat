package io.github.binarybeing.hotcat.plugin.utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Url;

public class WebPreviewVirtualFileUtils {


    public static VirtualFile create(VirtualFile projectFile, Url newFromEncoded) throws Exception{
        try {
            return (VirtualFile) Class.forName("com.intellij.ide.browsers.actions.WebPreviewVirtualFile")
                    .getConstructor(VirtualFile.class, Url.class)
                    .newInstance(projectFile, newFromEncoded);
        } catch (Exception e) {
            throw new RuntimeException("current version not support WebPreviewVirtualFile");
        }

    }
}
