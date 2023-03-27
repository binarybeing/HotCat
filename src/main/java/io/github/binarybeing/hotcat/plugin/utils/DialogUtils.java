package io.github.binarybeing.hotcat.plugin.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * @author gn.binarybei
 * @date 2022/9/23
 * @note
 */
public class DialogUtils extends DialogWrapper {
    private JPanel jPanel;

    protected DialogUtils(@Nullable Project project, String title, JPanel jPanel) {
        super(project);
        setTitle(title);
        this.jPanel = jPanel;
        init();
    }

    public static void showError(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
    }
    public static void showMsg(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }


    public static String showInputDialog(AnActionEvent event, String title, String label) {
        final String[] s = new String[]{""};
        JPanel panel = new JPanel();
        JLabel jLabel = new JLabel(label);
        panel.add(jLabel);
        JTextField field = new JTextField();
        panel.add(field);
        DialogUtils dialogUtils = new DialogUtils(event.getProject(), title, panel);
        dialogUtils.setOKButtonText("确认");
        dialogUtils.setCancelButtonText("取消");
        boolean b = dialogUtils.showAndGet();
        if (b) {
            s[0] = field.getText();
        }
        return s[0];
    }

    public static void showFormDialog(AnActionEvent event, String title, Map<String, String> fromInfo, Consumer<Map<String, String>> inputConsumer) {
        JPanel panel = new JPanel();
        Map<String, JTextField> input = new HashMap<>();
        for (Map.Entry<String, String> entry : fromInfo.entrySet()) {
            panel.add(new JLabel(entry.getValue()));
            JTextField field = new JTextField();
            input.put(entry.getKey(), field);
        }
        DialogUtils dialogUtils = new DialogUtils(event.getProject(), title, panel);

        dialogUtils.setOKButtonText("确认");
        dialogUtils.setCancelButtonText("取消");
        if (dialogUtils.showAndGet()) {
            Map<String, String> map = new HashMap<>();
            for (Map.Entry<String, JTextField> entry : input.entrySet()) {
                map.put(entry.getKey(), entry.getValue().getText());
            }
            inputConsumer.accept(map);
        }
    }

    public static void showChooseFileDialog(AnActionEvent event, String title, Consumer<File> consumer) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setName("choose file");
        //show dialog
        fileChooser.showOpenDialog(null);
        fileChooser.addActionListener(e -> {
            LogUtils.addLog("action"+ e.getActionCommand());
        });
        JPanel panel = new JPanel();
        panel.add(fileChooser);

        DialogUtils dialogUtils = new DialogUtils(event.getProject(), title, panel);
        //choose file
        dialogUtils.setOKButtonText("确认");
        dialogUtils.setCancelButtonText("取消");
        boolean b = dialogUtils.showAndGet();
        if (b) {
            consumer.accept(fileChooser.getSelectedFile());
        }
    }



    public static boolean showConfirmDialog(AnActionEvent event, String title, String message) {
        JPanel panel = new JPanel();
        JLabel field = new JLabel(message);
        panel.add(field);
        DialogUtils dialogUtils = new DialogUtils(event.getProject(), title, panel);

        dialogUtils.setOKButtonText("确认");
        dialogUtils.setCancelButtonText("取消");
        return dialogUtils.showAndGet();
    }

    public static boolean showPanelDialog(AnActionEvent event, String title, JPanel jPanel) {
        DialogUtils dialogUtils = new DialogUtils(event.getProject(), title, jPanel);
        dialogUtils.createCenterPanel();
        dialogUtils.setOKButtonText("确认");
        dialogUtils.setCancelButtonText("取消");
        return dialogUtils.showAndGet();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return jPanel;
    }

    private static void acquire(Semaphore semaphore) {
        try {
            semaphore.acquire(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
