package io.github.binarybeing.hotcat.plugin.dto.project;

import java.util.List;

public class ProjectDto {

    private String name;
    private String path;
    private List<ProjectModule> modules;

    private ProjectModule current;

    private ProjectFile currentFile;

    private ProjectDirectory currentDirectory;

    private EditorContent editorContent;

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

    public List<ProjectModule> getModules() {
        return modules;
    }

    public void setModules(List<ProjectModule> modules) {
        this.modules = modules;
    }

    public ProjectModule getCurrent() {
        return current;
    }

    public void setCurrent(ProjectModule current) {
        this.current = current;
    }

    public ProjectFile getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(ProjectFile currentFile) {
        this.currentFile = currentFile;
    }

    public ProjectDirectory getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(ProjectDirectory currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public EditorContent getEditorContent() {
        return editorContent;
    }

    public void setEditorContent(EditorContent editorContent) {
        this.editorContent = editorContent;
    }
}
