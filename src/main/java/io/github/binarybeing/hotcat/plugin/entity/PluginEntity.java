package io.github.binarybeing.hotcat.plugin.entity;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class PluginEntity {
    private String name;
    private File file;

    private List<PluginEntity> subMenus;

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

    public List<PluginEntity> getSubMenus() {
        return subMenus;
    }

    public void setSubMenus(List<PluginEntity> subMenus) {
        this.subMenus = subMenus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginEntity that = (PluginEntity) o;
        return Objects.equals(file.getAbsolutePath(), that.file.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(file.getAbsolutePath());
    }
}
