package io.github.binarybeing.hotcat.plugin.dto.project;

import java.util.List;

public class ProjectDirectory {
    private String name;
    private String path;
    private List<ProjectDirectory> directories;
    private List<ProjectFile> projectFiles;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ProjectDirectory> getDirectories() {
        return directories;
    }

    public void setDirectories(List<ProjectDirectory> directories) {
        this.directories = directories;
    }

    public List<ProjectFile> getProjectFiles() {
        return projectFiles;
    }

    public void setProjectFiles(List<ProjectFile> projectFiles) {
        this.projectFiles = projectFiles;
    }
}
