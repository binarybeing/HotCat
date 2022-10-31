package io.github.binarybeing.hotcat.plugin.entity;

import java.io.File;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class PluginEntity {
    private String name;
    private File file;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
