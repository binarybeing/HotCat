package io.github.binarybeing.hotcat.plugin.server.service;

import com.github.binarybeing.hotcat.proto.project.IdeaProjectGrpcService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.binarybeing.hotcat.plugin.dto.project.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProjectService {
    private AnActionEvent event;
    private Project project;

    public ProjectService(AnActionEvent event) {
        this.event = event;
        this.project = event.getProject();
    }

    public String getProjectName(){
        return project.getName();
    }

    public String getProjectPath(){
        return project.getBasePath();
    }

    public ProjectDto currentInfo(){
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        Editor editor = CommonDataKeys.EDITOR.getData(event.getDataContext());
        if (editor instanceof EditorImpl) {
            EditorImpl editor1 = (EditorImpl) editor;
            virtualFile = editor1.getVirtualFile();
        }
        ProjectDto ans = new ProjectDto();
        if (null == virtualFile) {
            return ans;
        }

        List<ProjectModule> modules = new ArrayList<>();
        for (Module pModule : ModuleManager.getInstance(project).getModules()) {
            ProjectModule projectModule = new ProjectModule();
            projectModule.setName(pModule.getName());
            VirtualFile pModuleDir = ProjectUtil.guessModuleDir(pModule);
            projectModule.setPath(pModuleDir == null ? "" : pModuleDir.getPath());
            modules.add(projectModule);
        }
        ProjectModule currentModule = null;
        Module module = ProjectFileIndex.SERVICE.getInstance(project).getModuleForFile(virtualFile);
        if (module != null) {
            VirtualFile moduleDir = ProjectUtil.guessModuleDir(module);
            currentModule = new ProjectModule();
            currentModule.setName(module.getName());
            currentModule.setPath(moduleDir == null ? "" : moduleDir.getPath());
        }
        ProjectDirectory projectDirectory = null;
        ProjectFile projectFile = null;

        if (virtualFile.isDirectory()) {
            projectDirectory = new ProjectDirectory();
            projectDirectory.setName(virtualFile.getName());
            projectDirectory.setPath(virtualFile.getPath());
            List<ProjectFile> projectFileList = FileUtils.listFiles(new File(virtualFile.getPath()), null, false)
                    .stream().filter(File::isFile).map(f -> {
                        ProjectFile file = new ProjectFile();
                        file.setPath(f.getPath());
                        file.setName(f.getName());
                        return file;
                    }).collect(Collectors.toList());
            List<ProjectDirectory> projectDirList = FileUtils.listFiles(new File(virtualFile.getPath()), null, false)
                    .stream().filter(File::isDirectory).map(f -> {
                        ProjectDirectory file = new ProjectDirectory();
                        file.setPath(f.getPath());
                        file.setName(f.getName());
                        return file;
                    }).collect(Collectors.toList());
            projectDirectory.setProjectFiles(projectFileList);
            projectDirectory.setDirectories(projectDirList);

        } else {
            projectFile = new ProjectFile();
            projectFile.setName(virtualFile.getName());
            projectFile.setPath(virtualFile.getPath());
        }
        EditorContent editorContent = null;
        if (!virtualFile.isDirectory() && editor != null) {
            editorContent = new EditorContent();
            editorContent.setText(editor.getDocument().getText());
            TextSelectModel model = new TextSelectModel();
            model.setText(editor.getSelectionModel().getSelectedText());
            model.setStart(editor.getSelectionModel().getSelectionStart());
            model.setEnd(editor.getSelectionModel().getSelectionEnd());

            model.setLine(Objects.requireNonNull(editor.getSelectionModel().getSelectionStartPosition()).line);
            editorContent.setSelectModel(model);
        }

        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        ans.setPath(projectDir == null ? "" : projectDir.getPath());
        ans.setName(project.getName());
        ans.setModules(modules);
        ans.setCurrent(currentModule);
        ans.setCurrentDirectory(projectDirectory);
        ans.setCurrentFile(projectFile);
        ans.setEditorContent(editorContent);
        return ans;
    }


    public IdeaProjectGrpcService.CurrentModule getCurrentModule() throws IOException {
        IdeaProjectGrpcService.CurrentModule.Builder builder = IdeaProjectGrpcService.CurrentModule.newBuilder();
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        Editor editor = CommonDataKeys.EDITOR.getData(event.getDataContext());
        Assert.assertNotNull("no file or dir selected", virtualFile);
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Module module = ProjectFileIndex.SERVICE.getInstance(project).getModuleForFile(virtualFile);
        if(module == null){
            module = ArrayUtils.isEmpty(modules) ? null : modules[0];
        }
        Assert.assertNotNull("no module found", module);
        ModuleRootManager manager = ModuleRootManager.getInstance(module);

        builder.setModule(IdeaProjectGrpcService.Module.newBuilder()
                .setName(module.getName())
                .setPath(ProjectUtil.guessModuleDir(module).getPath())
                .build());
        String content = "";
        if (!virtualFile.isDirectory()) {
            content = FileUtils.readFileToString(new File(virtualFile.getPath()), "UTF-8");
        }
        IdeaProjectGrpcService.TextSelectModel selectModel = null;
        if (editor != null) {
            SelectionModel model = editor.getSelectionModel();
            selectModel = IdeaProjectGrpcService.TextSelectModel.newBuilder()
                    .setStart(model.getSelectionStart())
                    .setEnd(model.getSelectionEnd())
                    .setText(model.getSelectedText() == null ? "" : model.getSelectedText())
                    .setLine(model.getSelectionStartPosition() == null ? 0 : model.getSelectionStartPosition().line)
                    .build();
        }
        builder.setCurrentFile(IdeaProjectGrpcService.CurrentFile.newBuilder()
                .setIsFile(!virtualFile.isDirectory())
                .setContent(IdeaProjectGrpcService.Content.newBuilder().setText(content)
                        .setSelectModel(selectModel)
                        .build())
                .build());

        return builder.build();
    }

    public Iterable<IdeaProjectGrpcService.Module> getModules() {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        List<IdeaProjectGrpcService.Module> list = new ArrayList<>();

        for (Module module : modules) {
            VirtualFile[] files = ModuleRootManager.getInstance(module).getContentRoots();
            if (ArrayUtils.isEmpty(files)) {
                continue;
            }
            IdeaProjectGrpcService.Module.Builder builder = IdeaProjectGrpcService.Module.newBuilder();
            builder.setName(module.getName());
            builder.setPath(files[0].getPath());
            list.add(builder.build());
        }
        return list;
    }
}
