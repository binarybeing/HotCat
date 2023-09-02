package io.github.binarybeing.hotcat.plugin.project;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import org.junit.Assert;

import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ProjectTest extends BaseTest {
    @Override
    public Object doExecute() throws Exception {
        Project project = event.getProject();
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        Module module = ProjectFileIndex.SERVICE.getInstance(project).getModuleForFile(virtualFile);
        ModuleRootManager manager = ModuleRootManager.getInstance(module);
        Arrays.toString(manager.getContentRoots());
        return project.getBasePath();
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        Assert.assertEquals(code, 200);
    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-08-30";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }
}
