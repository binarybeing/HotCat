package io.github.binarybeing.hotcat.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.utils.JsonUtils;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import io.github.binarybeing.hotcat.plugin.utils.PluginFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class InstallPluginAction extends AnAction {
    public InstallPluginAction() {
        super("Install Plugin...");
    }

    private static VirtualFile toSelect;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        EventContext.registerEvent(e);
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false,
                true, true,
                false, false);
        descriptor.withFileFilter(file -> {
            String name = file.getName();
            return name.endsWith(".zip");
        });
        DataContext dataContext = e.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        if (toSelect == null && project.getProjectFile() != null) {
            toSelect = project.getProjectFile().getParent();
        }
        FileChooser.chooseFile(descriptor, project, toSelect, new Consumer<VirtualFile>() {
            @Override
            public void consume(VirtualFile virtualFile) {
                toSelect = virtualFile.getParent();
                String path = virtualFile.getPath();
                File file = new File(path);
                if (!file.getName().endsWith(".zip")) {
                    JOptionPane.showMessageDialog(null, "Install Plugin Failed, file must be zip file", "Error", JOptionPane.ERROR_MESSAGE);
                    LogUtils.addLog("Install Plugin Failed: not zip file");
                    return;
                }

                LogUtils.addLog("Install Plugin Success: " + file.getName());
                String pluginDirName = PluginFileUtils.getPluginDirName();

                File pluginDir = PluginFileUtils.unzip(file, pluginDirName);

                if (pluginDir != null && pluginDir.exists()) {
                    File[] files = pluginDir.listFiles();
                    if (files != null) {
                        for (File file1 : files) {
                            LogUtils.addLog("Install Plugin sub files: " + file1.getName());
                        }
                        boolean b1 = Arrays.stream(files).noneMatch(f -> "__main__.py".equals(f.getName()));
                        Optional<File> pluginConfig = Arrays.stream(files).filter(f -> "plugin.json".equals(f.getName())).findAny();
                        if (b1 || !pluginConfig.isPresent()) {
                            LogUtils.addLog("Install Plugin Failed: " + file.getName());
                            PluginFileUtils.deleteDir(pluginDir);
                            JOptionPane.showMessageDialog(null, "Install Plugin Failed, no __main__.py or plugin.json", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        String name = JsonUtils.readJsonFileStringValue(pluginConfig.get(), "name");
                        if (StringUtils.isEmpty(name)) {
                            PluginFileUtils.deleteDir(pluginDir);
                            JOptionPane.showMessageDialog(null, "Install Plugin Failed, no name in plugin.json", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        JOptionPane.showMessageDialog(null, "Install " + name + " Success", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        PluginFileUtils.deleteDir(pluginDir);
                        JOptionPane.showMessageDialog(null, "Install Plugin Failed: " + file.getName());
                    }
                }else {
                    JOptionPane.showMessageDialog(null, "Install Plugin Failed: " + file.getName()+ ", unzip file failed", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

    }


}
