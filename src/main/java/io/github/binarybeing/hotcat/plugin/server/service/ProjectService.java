package io.github.binarybeing.hotcat.plugin.server.service;

import com.github.binarybeing.hotcat.proto.project.IdeaProjectGrpcService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        ModuleRootManager manager = ModuleRootManager.getInstance(modules[0]);
        builder.setModule(IdeaProjectGrpcService.Module.newBuilder()
                .setName(modules[0].getName())
                .setPath(manager.getContentRoots()[0].getPath())
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
            IdeaProjectGrpcService.Module.Builder builder = IdeaProjectGrpcService.Module.newBuilder();
            builder.setName(module.getName());
            VirtualFile[] files = ModuleRootManager.getInstance(module).getContentRoots();
            builder.setPath(files[0].getPath());
            list.add(builder.build());
        }
        return list;
    }
}
