package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
import io.github.binarybeing.hotcat.plugin.utils.DialogUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * @author gn.binarybei
 * @date 2022/9/29
 * @note
 */
public class IdeaPanelController extends BaseEventScriptController {
    @Override
    String path() {
        return "/api/idea/panel";
    }
    @Override
    protected @NotNull Response handle(Request request, AnActionEvent event, String script) {
        IdeaPanel panel = new IdeaPanel(event);
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("panel", panel);
        Object result = expression.evaluate(context);
        return Response.success(result);
    }

    public static class IdeaPanel {
        private final JPanel jPanel;

        private final AnActionEvent event;

        private String title;

        private final HashMap<String, JComponent> res = new HashMap<>();

        public IdeaPanel(AnActionEvent event) {
            this.jPanel = new JPanel();
            Dimension dimension = new Dimension();

            jPanel.setSize(dimension);
            this.jPanel.setLayout(new BoxLayout(this.jPanel, BoxLayout.Y_AXIS));
            this.event = event;
        }

        public IdeaPanel setTitle(String title) {
            this.title = title;
            return this;
        }

        public IdeaPanel showInput(String label, String filed, String defaultValue) {
            JLabel jLabel = new JLabel(label);
            jLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            jPanel.add(jLabel);
            if (StringUtils.isNotEmpty(filed)) {
                JTextField jTextField = new JTextField(15);
                jTextField.setText(defaultValue);
                jLabel.setLabelFor(jTextField);
                jPanel.add(jTextField);
                res.put(filed, jTextField);
            }
            return this;
        }

        public IdeaPanel showSelect(String label, String filed, String[] options, String defaultValue) {
            jPanel.add(new JLabel(label));
            ComboBox<String> box = new ComboBox<>(options);
            if (StringUtils.isNotEmpty(defaultValue)) {
                box.setSelectedItem(defaultValue);
            }
            jPanel.add(box);
            res.put(filed, box);
            return this;
        }

        public IdeaPanel showCheck(String label, String[] optionArr) {
            jPanel.add(new JLabel(label));
            List<String> optionList = Arrays.asList(optionArr);
            optionList.forEach(option -> {
                JCheckBox jCheckBox = new JCheckBox(option);
                jPanel.add(jCheckBox);
                res.put(option, jCheckBox);
            });
            return this;
        }

        public void showMsg(String title, String info, String type) {
            if ("error".equals(type)) {
                DialogUtils.showError(title, info);
            } else {
                DialogUtils.showMsg(title, info);
            }
        }

        public String showFileChooserAndGet(String path, Integer type, String[] suffixes) {
            VirtualFile file = null;
            if (StringUtils.isNoneBlank(path)) {
                file = LocalFileSystem.getInstance().refreshAndFindFileByPath(FileUtilRt.toSystemIndependentName(path));
            }
            final FileChooserDescriptor descriptor = new FileChooserDescriptor((type & 1) > 0, ((type >> 1)& 1) > 0,
                    true, true,
                    false, false);
            if (ArrayUtils.isNotEmpty(suffixes)) {
                descriptor.withFileFilter(f -> {
                    for (String suffix : suffixes) {
                        if (f.getName().endsWith(suffix)) {
                            return true;
                        }
                    }
                    return false;
                });
            }
            Project project = event.getProject();
            final VirtualFile fileToSelect = file;
            return ApplicationRunnerUtils.run(()->{
                VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, fileToSelect);
                if (virtualFile == null) {
                    return null;
                }
                return virtualFile.getPath();
            });
        }

        public IdeaPanel showForm(String label, Map<String, String> formInfo){
            return this;
        }

        public Map<String, String> showAndGet(){
            return showAndGet(jPanel.getWidth());
        }

        public Map<String, String> showAndGet(int width){
            jPanel.setSize(width, jPanel.getHeight());
            boolean ok = DialogUtils.showPanelDialog(event, title, jPanel);
            Map<String,String> inputInfo = new HashMap<>(4);
            if (ok) {
                for (Map.Entry<String, JComponent> entry : res.entrySet()) {
                    String field = entry.getKey();
                    JComponent component = entry.getValue();
                    if (component instanceof JTextField) {
                        JTextField jTextField = (JTextField) component;
                        inputInfo.put(field, jTextField.getText());
                    } else if (component instanceof ComboBox) {
                        ComboBox<String> box = (ComboBox<String>) component;
                        inputInfo.put(field, box.getSelectedItem().toString());
                    } else if (component instanceof JCheckBox) {
                        JCheckBox checkbox = (JCheckBox) component;
                        inputInfo.put(field, String.valueOf(checkbox.isSelected()));
                    }
                }
            }else{
                throw new RuntimeException("cancel");
            }
            return inputInfo;
        }

        public String showProcessing(){
            DataContext dataContext = event.getDataContext();
            Project project = CommonDataKeys.PROJECT.getData(dataContext);
            if (project == null) {
                throw new RuntimeException("project is null");
            }
            String processId = UUID.randomUUID().toString();
            ApplicationManager.getApplication().invokeLater(()->{
                ProgressManager.getInstance().runProcessWithProgressSynchronously(new ProcessingRunnable(processId), title, false, project);
            });
            return processId;
        }
        public String closeProcessing(String processId){
            DataContext dataContext = event.getDataContext();
            Project project = CommonDataKeys.PROJECT.getData(dataContext);
            if (project == null) {
                throw new RuntimeException("project is null");
            }
            ProcessingRunnable runnable = ProcessingRunnable.PROCESS_MAP.get(processId);
            if (runnable != null) {
                runnable.stop = true;
                runnable.waits();
            }
            return processId;
        }
    }
    private static class ProcessingRunnable implements Runnable{
        private static final Map<String, ProcessingRunnable> PROCESS_MAP = new ConcurrentHashMap<>();

        private String processId;

        private boolean stop = false;

        private Semaphore semaphore = new Semaphore(0);

        public ProcessingRunnable(String processId) {
            this.processId = processId;
            PROCESS_MAP.put(processId, this);
        }

        @Override
        public void run() {
            while (!stop){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            semaphore.release(1);
        }
        public void waits(){
            try {
                semaphore.acquire(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
