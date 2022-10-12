package io.github.binarybeing.hotcat.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.utils.JsonUtils;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import io.github.binarybeing.hotcat.plugin.utils.PluginFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
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

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        EventContext.registerEvent(e);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return true;
            }
            @Override
            public String getDescription() {
                return "zip file";
            }
        });
        int i = fileChooser.showOpenDialog(null);
        if (i == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".zip")) {
                JOptionPane.showMessageDialog(null, "Install Plugin Failed, file must be zip file", "Error", JOptionPane.ERROR_MESSAGE);
                LogUtils.addLog("Install Plugin Failed: not zip file");
                return;
            }
            System.out.println(file);
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
                    JOptionPane.showMessageDialog(null, "Install Plugin Success", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    PluginFileUtils.deleteDir(pluginDir);
                    JOptionPane.showMessageDialog(null, "Install Plugin Failed: " + file.getName());
                }
            }
        }
    }
}
