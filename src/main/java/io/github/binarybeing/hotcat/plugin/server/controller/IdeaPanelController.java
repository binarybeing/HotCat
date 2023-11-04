package io.github.binarybeing.hotcat.plugin.server.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.popup.NotLookupOrSearchCondition;
import com.intellij.util.Urls;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.intellij.xdebugger.impl.ui.XDebuggerExpressionEditor;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.handlers.InvokePythonPluginHandler;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.*;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.debugger.JavaDebuggerEditorsProvider;
import org.junit.Assert;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author gn.binarybei
 * @date 2022/9/29
 * @note
 */
public class IdeaPanelController extends BaseEventScriptController {
    @Override
    public String path() {
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

        private JPanel jPanel;
        private JPanel row;

        private final AnActionEvent event;

        private String title;

        private HashMap<String, JComponent> formInfo = new LinkedHashMap<>();
        private HashMap<String, String> labelsMap = new LinkedHashMap<>();
        private Set<String> callbackWhenUpdateSet = new HashSet<>();
        private List<List<String>> rowsInfo = new ArrayList<>();

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
        public IdeaPanel rowGroupStart(){
            rowGroupEnd();
            row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            return this;
        }
        public IdeaPanel rowGroupEnd(){
            if (row != null) {
                rowsInfo.add(rowInfo(row));
                jPanel.add(row);
                row = null;
            }
            return this;
        }
        private JPanel newRow() {
            return new JPanel(new FlowLayout(FlowLayout.LEFT));
        }
        private JPanel newLabeledItem(int row, int col) {
            return new JPanel(new GridLayout(row, col));
        }

        private void finishRow(JPanel targetRow) {
            if (targetRow != row) {
                rowsInfo.add(this.rowInfo(targetRow));
                jPanel.add(targetRow);
            }
        }

        private List<String> rowInfo(JPanel targetRow){
            List<String> list = new ArrayList<>();
            for (Component component : targetRow.getComponents()) {
                //find inner item
                if (component instanceof JPanel) {
                    JPanel panel = (JPanel) component;
                    for (Component innerComponent : panel.getComponents()) {
                        Optional<Map.Entry<String, JComponent>> first = formInfo.entrySet().stream()
                                .filter(e -> innerComponent == e.getValue())
                                .findFirst();
                        first.ifPresent(e->{
                            list.add(e.getKey());
                        });
                    }
                }

            }
            return list;
        }
        private DocumentListener formChangedListener(String fieldName) {
            return new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    onFormChanged(fieldName, e.getOffset());
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    onFormChanged(fieldName, e.getOffset());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    onFormChanged(fieldName, e.getOffset());
                }
            };
        }

        private void onFormChanged(String fieldName, int offset){
            if (!callbackWhenUpdateSet.contains(fieldName)) {
                return;
            }

            Long eventId = EventContext.getEventId(event);
            InvokePythonPluginHandler invokePythonPluginHandler = new InvokePythonPluginHandler();
            List<Map<String, Map<String, Object>>> formInfoMap = this.extraFormInfo(this.formInfo);
            Map<String, Object> map = new HashMap<>(4);
            map.put("change_field", fieldName);
            map.put("new_form_data", formInfoMap);
            map.put("panel_id", String.valueOf(eventId));
            map.put("event_time", System.currentTimeMillis());
            Optional<PluginEntity> plugin = EventContext.getPluginEntity(EventContext.getEventId(event));
            plugin.ifPresent(p->{
                String parent = p.getFile().getPath();
                if (p.getFile().isFile()) {
                    parent = p.getFile().getParent();
                }
                if (!parent.endsWith("/")) {
                    parent += "/";
                }

                String callPath = parent + "callback.py";
                try {
                    jPanel.removeAll();
                    URL url = new File(PluginFileUtils.getPluginDirName()+"/loading.gif").toURI().toURL();
                    Icon myImgIcon = new ImageIcon(url);
                    JLabel imageLbl = new JLabel(myImgIcon);
                    jPanel.add(imageLbl);
                    jPanel.setLayout(new BorderLayout());
                    jPanel.add(imageLbl, BorderLayout.CENTER);

                    CompletableFuture<String> future = invokePythonPluginHandler.actionCallback(eventId, "form_updated", new Gson().toJson(map), callPath);
                    future.thenAccept(s->{
                        EventQueue.invokeLater(()->{
                            if (StringUtils.isBlank(s)) {
                                return;
                            }
                            updateForm(fieldName, offset, s);
                        });
                    });
                } catch (Exception e) {
                    LogUtils.addError(e, "call callPath timeout");
                }

            });
        }


        public IdeaPanel showInput(String label, String field, String defaultValue) {
            JLabel jLabel = new JLabel(label);
            formInfo.put("label:" + RandomUtils.nextInt(0, Integer.MAX_VALUE), jLabel);

            JPanel targetRow = row != null ? row : newRow();
            JPanel item = this.newLabeledItem(2, 1);
            item.add(jLabel);
            if (StringUtils.isNotEmpty(field)) {
                JTextField jTextField = new JTextField(15);
                jTextField.setText(defaultValue);
                labelsMap.put(field, label);
                item.add(jTextField);
                formInfo.put(field, jTextField);
                jTextField.getDocument().addDocumentListener(this.formChangedListener(field));
            }
            targetRow.add(item);
            this.finishRow(targetRow);
            return this;
        }
        public IdeaPanel showPassword(String label, String field, String defaultValue) {
            JLabel jLabel = new JLabel(label);
            formInfo.put("label:" + RandomUtils.nextInt(0, Integer.MAX_VALUE), jLabel);
            JPanel targetRow = row != null ? row : newRow();
            JPanel item = this.newLabeledItem(2, 1);
            item.add(jLabel);
            if (StringUtils.isNotEmpty(field)) {
                JPasswordField passwordField = new JPasswordField(15);
                passwordField.setText(defaultValue);
//                jLabel.setLabelFor(passwordField);
                labelsMap.put(field, label);
                item.add(passwordField);
                formInfo.put(field, passwordField);
                passwordField.getDocument().addDocumentListener(this.formChangedListener(field));
            }
            targetRow.add(item);
            this.finishRow(targetRow);
            return this;
        }

        public IdeaPanel showSelect(String label, String field, String[] names, String[] values, String defaultValue) {
            JLabel jLabel = new JLabel(label);
            formInfo.put("label:" + RandomUtils.nextInt(0, Integer.MAX_VALUE), jLabel);
            JPanel targetRow = row != null ? row : newRow();
            JPanel item = this.newLabeledItem(2, 1);
            item.add(jLabel);
            if (names == null || values == null || names.length != values.length) {
                throw new RuntimeException("names and values must be not null and length must be equal");
            }
            SelectItem[] items = new SelectItem[names.length];
            SelectItem defaultItem = null;
            for (int i = 0; i < names.length; i++) {
                String value = values[i];
                String label1 = names[i];
                SelectItem selectItem = new SelectItem();
                selectItem.setLabel(label1);
                selectItem.setValue(value);
                items[i] = selectItem;
                if (Objects.equals(defaultValue, selectItem.getValue())) {
                    defaultItem = selectItem;
                }
            }
            ComboBox<SelectItem> box = new ComboBox<>(items);
            if (defaultItem != null) {
                box.setSelectedItem(defaultItem);
            }
            box.addItemListener(e -> onFormChanged(field, -1));
            item.add(box);
            targetRow.add(item);
            formInfo.put(field, box);
            this.finishRow(targetRow);
            return this;
        }

        public IdeaPanel showSelect(String label, String field, String[] options, String defaultValue) {
            
            JLabel jLabel = new JLabel(label);
            formInfo.put("label:" + RandomUtils.nextInt(0, Integer.MAX_VALUE), jLabel);
            JPanel targetRow = row != null ? row : newRow();

            JPanel item = this.newLabeledItem(2, 1);
            item.add(jLabel);
            
            ComboBox<String> box = new ComboBox<>(options);
            if (StringUtils.isNotEmpty(defaultValue)) {
                box.setSelectedItem(defaultValue);
            }
            box.addItemListener(e -> onFormChanged(field, -1));
            item.add(box);
            targetRow.add(item);
            formInfo.put(field, box);
            this.finishRow(targetRow);
            return this;
        }

        public IdeaPanel showCheck(String label, String value, String field, boolean checked) {
            JLabel jLabel = new JLabel(label);
            formInfo.put("label:" + RandomUtils.nextInt(0, Integer.MAX_VALUE), jLabel);
            JPanel targetRow = row != null ? row : newRow();
            JPanel item = this.newLabeledItem(2, 1);
            item.add(jLabel);
            JBCheckBox jCheckBox = new JBCheckBox();
            jCheckBox.setName(field);
            jCheckBox.setText(value);
            jCheckBox.setSelected(checked);
            jCheckBox.addItemListener(e -> onFormChanged(field, -1));
            item.add(jCheckBox);
            targetRow.add(item);
            formInfo.put(field, jCheckBox);
            this.finishRow(targetRow);
            return this;
        }

        public IdeaPanel showCheck(String label, String[] optionArr) {
            JLabel jLabel = new JLabel(label);
            formInfo.put("label:" + RandomUtils.nextInt(0, Integer.MAX_VALUE), jLabel);
            JPanel targetRow = row != null ? row : newRow();
            JPanel item = this.newLabeledItem(2, 1);
            item.add(jLabel);
            List<String> optionList = Arrays.asList(optionArr);
            optionList.forEach(option -> {
                JCheckBox jCheckBox = new JCheckBox(option);
                jCheckBox.addItemListener(e -> onFormChanged(option, -1));
                item.add(jCheckBox);
                formInfo.put(option, jCheckBox);
            });
            targetRow.add(item);
            this.finishRow(targetRow);
            return this;
        }

        public void showMsg(String title, String info, String type) {
            if ("error".equals(type)) {
                DialogUtils.showError(title, info);
            } else {
                DialogUtils.showMsg(title, info);
            }
        }

        public boolean showConfirmDialog(String title, String message){
            return DialogUtils.showConfirmDialog(event, title, message);
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
            VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, fileToSelect);
            if (virtualFile == null) {
                return null;
            }
            return virtualFile.getPath();
        }

        public IdeaPanel showForm(String label, Map<String, String> formInfo){
            return this;
        }

        public IdeaPanel callbackWhenUpdate(String fieldName){
            callbackWhenUpdateSet.add(fieldName);
            return this;
        }

        public Map<String, String> showAndGet(){
            return showAndGet(jPanel.getWidth());
        }

        public Map<String, String> showAndGet(int width){
            jPanel.setSize(width, jPanel.getHeight());
            boolean ok = DialogUtils.showPanelDialog(event, title, jPanel);
            if (ok) {
                return extraInputInfo(formInfo);
            }
            return Collections.emptyMap();
        }
        private void updateForm(String fieldName, int offset, String jsonString) {
            try {
                HashMap<String, JComponent> newRes = new LinkedHashMap<>();
                Set<String> newCallbackWhenUpdateSet = new HashSet<>();
                List<List<String>> newRowsInfo = new ArrayList<>();
                List<JPanel> rowsPanel = new ArrayList<>();
                JsonElement jsonElement = JsonParser.parseString(jsonString);
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                JComponent toFocus = null;
                for (JsonElement ele : jsonArray) {
                    JsonObject jsonObject = ele.getAsJsonObject();
                    ArrayList<String> rowInfo = new ArrayList<>();
                    JPanel newRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JPanel item = this.newLabeledItem(2, 1);
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        String field = entry.getKey();
                        rowInfo.add(field);
                        JsonElement element = entry.getValue();
                        JsonObject info = element.getAsJsonObject();
                        String type = info.get("type").getAsString();
                        String value = info.get("value").getAsString();
                        boolean callback = info.get("callback").getAsBoolean();
                        if (callback) {
                            newCallbackWhenUpdateSet.add(field);
                        }
                        Type valuesType = new TypeToken<ArrayList<String>>() {}.getType();
                        ArrayList<String> values = new Gson().fromJson(info.get("values"), valuesType);
                        if (type != null) {
                            switch (type) {
                                case "password":
                                    JPasswordField passwordField = new JPasswordField(15);
                                    passwordField.setText(value);
                                    newRes.put(field, passwordField);

                                    item.add(passwordField);
                                    if (Objects.equals(fieldName, field)) {
                                        passwordField.getDocument().insertString(offset, "", null);
                                        toFocus = passwordField;
                                    }
                                    break;
                                case "input":
                                    JTextField jTextField = new JTextField(15);
                                    jTextField.setText(value);
                                    newRes.put(field, jTextField);
                                    if (Objects.equals(fieldName, field)) {
                                        jTextField.getDocument().insertString(offset, "", null);
                                        toFocus = jTextField;
                                    }
                                    item.add(jTextField);
                                    break;
                                case "select":
                                    ComboBox<String> comboBox = new ComboBox<>();
                                    for (String s : values) {
                                        comboBox.addItem(s);
                                    }
                                    comboBox.setSelectedItem(value);
                                    newRes.put(field, comboBox);
                                    if (Objects.equals(fieldName, field)) {
                                        toFocus = comboBox;
                                    }
                                    item.add(comboBox);
                                    break;
                                case "check":
                                    JBCheckBox checkbox = new JBCheckBox();
                                    checkbox.setName(field);
                                    checkbox.setText(values.get(0));
                                    if (StringUtils.isNotBlank(value)) {
                                        checkbox.setSelected(true);
                                    }
                                    newRes.put(field, checkbox);
                                    if (Objects.equals(fieldName, field)) {
                                        toFocus = checkbox;
                                    }
                                    item.add(checkbox);
                                    break;
                                case "label":
                                    JLabel label = new JLabel(value);
                                    newRes.put(field, label);
                                    item.add(label);
                                    break;
                                default:
                                    LogUtils.addLog("not support form type:" + type);
                            }
                        }
                        if (item.getComponents() != null && item.getComponents().length == 2) {
                            newRow.add(item);
                            item = this.newLabeledItem(2, 1);
                        }
                    }
                    newRowsInfo.add(rowInfo);
                    rowsPanel.add(newRow);
                }


                jPanel.removeAll();
                for (JPanel newRow : rowsPanel) {
                    jPanel.add(newRow);
                }

                jPanel.setLayout(new BoxLayout(this.jPanel, BoxLayout.Y_AXIS));
                jPanel.setSize(jPanel.getWidth(), 0);

                if (toFocus != null) {
                    toFocus.grabFocus();
                    if (toFocus instanceof JTextField) {
                        JTextField textField = (JTextField) toFocus;
                        textField.setCaretPosition(offset + 1);
                    }
                }
                formInfo = newRes;
                callbackWhenUpdateSet = newCallbackWhenUpdateSet;
                rowsInfo = newRowsInfo;
                addListener();

            } catch (Exception e) {
                LogUtils.addError(e, "updateForm error， json=" + jsonString);
            }
        }

        private void addListener() {
            for (Map.Entry<String, JComponent> entry : formInfo.entrySet()) {
                JComponent component = entry.getValue();
                String fieldName = entry.getKey();
                if (component instanceof JTextField) {
                    JTextField field = (JTextField) component;
                    field.getDocument().addDocumentListener(this.formChangedListener(fieldName));
                }
                if (component instanceof ComboBox) {
                    ComboBox box = (ComboBox) component;
                    box.addItemListener(e -> onFormChanged(fieldName, -1));
                }
                if (component instanceof JCheckBox) {
                    JCheckBox box = (JCheckBox) component;
                    box.addItemListener(e -> onFormChanged(fieldName, -1));
                }


            }

        }


        private List<Map<String, Map<String, Object>>> extraFormInfo(Map<String, JComponent> panelRes) {
            List<Map<String, Map<String, Object>>> formInfoList = new ArrayList<>();
            for (List<String> rowFields : rowsInfo) {
                Map<String, Map<String, Object>> rowsInfoMap = new LinkedHashMap<>(4);
                for (String field : rowFields) {
                    Map<String, Object> map = new HashMap<>();
                    JComponent component = panelRes.get(field);
                    map.put("callback", callbackWhenUpdateSet.contains(field));
                    if (component instanceof JPasswordField) {
                        JPasswordField passwordField = (JPasswordField) component;
                        String password = new String(passwordField.getPassword());
                        map.put("type", "password");
                        map.put("value", password);
                        map.put("values", new String[]{password});
                        rowsInfoMap.put(field, map);
                    }else if (component instanceof JTextField) {
                        JTextField jTextField = (JTextField) component;
                        map.put("type", "input");
                        map.put("value", jTextField.getText());
                        map.put("values", new String[]{jTextField.getText()});
                        rowsInfoMap.put(field, map);
                    } else if (component instanceof ComboBox) {
                        ComboBox<?> comboBox = (ComboBox<?>) component;
                        map.put("type", "select");
                        Object item = comboBox.getSelectedItem();
                        if(item instanceof SelectItem){
                            SelectItem selectItem = (SelectItem) item;
                            map.put("value", selectItem.getValue());
                        }else{
                            map.put("value", String.valueOf(item));
                        }
                        int startIdx = 0;
                        List<String> list = new ArrayList<>();
                        while (comboBox.getItemAt(startIdx) != null) {
                            Object item1 = comboBox.getItemAt(startIdx);
                            if(item1 instanceof SelectItem){
                                SelectItem selectItem = (SelectItem) item1;
                                list.add(selectItem.getValue());
                            }else{
                                list.add(String.valueOf(item1));
                            }
                            startIdx++;
                        }
                        map.put("values", list.toArray(new String[0]));
                        rowsInfoMap.put(field, map);
                    } else if (component instanceof JCheckBox) {
                        JCheckBox checkbox = (JCheckBox) component;
                        String text = checkbox.getText();
                        map.put("type", "check");
                        map.put("value", checkbox.isSelected() ? text : "");
                        map.put("values", new String[]{text});
                        rowsInfoMap.put(field, map);
                    } else if (component instanceof JLabel) {
                        JLabel jLabel = (JLabel) component;
                        map.put("type", "label");
                        map.put("value", jLabel.getText());
                        map.put("values", new String[]{jLabel.getText()});
                        rowsInfoMap.put(field, map);
                    }
                }
                if (!rowsInfoMap.isEmpty()) {
                    formInfoList.add(rowsInfoMap);
                }
            }
            return formInfoList;
        }

        private Map<String, String> extraInputInfo(Map<String, JComponent> panelRes) {
            Map<String, String> inputInfo = new LinkedHashMap<>(4);
            for (Map.Entry<String, JComponent> entry : panelRes.entrySet()) {
                String field = entry.getKey();
                JComponent component = entry.getValue();
                if (component instanceof JPasswordField) {
                    JPasswordField passwordField = (JPasswordField) component;
                    inputInfo.put(field, new String(passwordField.getPassword()));
                }else if (component instanceof JTextField) {
                    JTextField jTextField = (JTextField) component;
                    inputInfo.put(field, jTextField.getText());
                } else if (component instanceof ComboBox) {
                    Object item = ((ComboBox<?>) component).getSelectedItem();
                    if(item instanceof SelectItem){
                        SelectItem selectItem = (SelectItem) item;
                        inputInfo.put(field, selectItem.getValue());
                    }else{
                        inputInfo.put(field, String.valueOf(item));
                    }
                } else if (component instanceof JCheckBox) {
                    JCheckBox checkbox = (JCheckBox) component;
                    inputInfo.put(field, String.valueOf(checkbox.isSelected()));
                }
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
            ProcessingRunnable processingRunnable = new ProcessingRunnable(project, title, processId);
            ProgressIndicator progressIndicator = new BackgroundableProcessIndicator(processingRunnable);
            ProgressManager.getInstance().runProcessWithProgressAsynchronously(processingRunnable, progressIndicator);
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
                runnable.release();
            }
            return processId;
        }

        public String showSidePanelEditor(String panelName, String file) {
            File panelFile = new File(file);
            if (!panelFile.exists()) {
                throw new RuntimeException("file not exist");
            }
            if (event == null || event.getProject() == null) {
                throw new RuntimeException("project is null");
            }
            VirtualFile testVFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(panelFile);
            if (testVFile == null) {
                throw new RuntimeException(String.format("idea virtual file of %s not exist", file));
            }
            FileEditor editor = TextEditorProvider.getInstance().createEditor(event.getProject(), testVFile);
            try {
                SidePanelUtils.showSidePanel(event, panelName, panelFile.getName(), editor.getComponent(), () -> {
                });
            } catch (Exception e) {
                throw new RuntimeException("open file failed: "+ e.getMessage());
            }
            return "success";
        }

        public String showSidePanelWebBrowser(String panelName, String url) throws Exception{
            VirtualFile virtualFile = WebPreviewVirtualFileUtils.create(event.getProject().getProjectFile(),  Urls.newFromEncoded(url));
            try {
                Class<FileEditorProvider> clazz = (Class<FileEditorProvider>) Class.forName("com.intellij.ide.browsers.actions.WebPreviewEditorProvider");
                FileEditorProvider editorProvider = FileEditorProvider.EP_FILE_EDITOR_PROVIDER.findExtension(clazz);
                assert editorProvider != null;
                FileEditor fileEditor = editorProvider.createEditor(event.getProject(), virtualFile);
                SidePanelUtils.showSidePanel(event, panelName, "", fileEditor.getComponent(), () -> {
                });
                return "success";
            } catch (Exception e) {
                throw new RuntimeException("Unsupported Feature, Please Update Idea Version");
            }
        }

        public String showFloatMiniEditor(String title, String fromFile, int line,
                                          String floatDefaultContent, boolean multiline, String callbackEngine) {
            Long eventId = EventContext.getEventId(event);
            JavaDebuggerEditorsProvider editorsProvider = new JavaDebuggerEditorsProvider();
            Project project = CommonDataKeys.PROJECT.getData(event.getDataContext());
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(fromFile));
            Assert.assertNotNull(virtualFile);
            Editor editor = CommonDataKeys.EDITOR.getData(event.getDataContext());
            Assert.assertNotNull(editor);
            Assert.assertNotNull(editor.getSelectionModel());
            XSourcePositionImpl position = XSourcePositionImpl.create(virtualFile, line);
            XDebuggerExpressionEditor expressionEditor =
                    new XDebuggerExpressionEditor(project,
                            editorsProvider,
                            "", position,
                            XExpressionImpl.fromText(floatDefaultContent), multiline, true, false) {
                        @Override
                        protected JComponent decorate(JComponent component, boolean multiline, boolean showEditor) {
                            return component;
                        }
                    };
            ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance()
                    .createComponentPopupBuilder(expressionEditor.getComponent(),
                            null)
                    .setDimensionServiceKey(project, "ShowHotCatPopup", false);

            JButton button = new JButton("OK", AllIcons.Actions.Commit);
            AtomicBoolean isOk = new AtomicBoolean(false);

            AtomicReference<EvaluateResult> evaluateReference = new AtomicReference<>(new EvaluateResult());
            AtomicReference<JBPopup> popupRef = new AtomicReference<>();
            final JBPopup popup = popupBuilder.setRequestFocusCondition(project, NotLookupOrSearchCondition.INSTANCE)
                    .setProject(project)
                    .setMinSize(new Dimension(400, 250))
                    .setResizable(true)
                    .setMovable(true)
                    .setTitle(title)
                    .setKeyboardActions(Lists.newArrayList(Pair.create(e -> confirmHandler(project, expressionEditor.getEditor(), isOk, popupRef, evaluateReference),
                            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK))))
                    .setCancelButton(new IconButton(null, AllIcons.General.HideToolWindow))
                    .setSettingButtons(button)
                    .setCancelOnClickOutside(false)
                    .setCancelOnOtherWindowOpen(false)
                    .setCancelOnMouseOutCallback(event1 -> false)
                    .addListener(new JBPopupListener() {
                        @Override
                        public void onClosed(@NotNull LightweightWindowEvent event) {
                            InvokePythonPluginHandler invokePythonPluginHandler = new InvokePythonPluginHandler();
                            Map<String, Object> map = new HashMap<>(2);
                            try {
                                String expression = expressionEditor.getExpression().getExpression();
                                Map<String, String> expressionMap = Maps.newHashMapWithExpectedSize(2);

                                EvaluateResult result = evaluateReference.get();
                                if (!isOk.get()) {
                                    map.put("code", 20020);
                                    map.put("msg", "cancel");
                                }else if (result.success) {
                                    map.put("code", 20000);
                                    map.put("data", result);
                                }else{
                                    map.put("code", 50020);
                                    map.put("msg", result.getFailMsg());
                                }
                            } catch (Exception e) {
                                map.put("code", 50000);
                                map.put("msg", e.getMessage());
                            }
                            if (StringUtils.isNoneBlank(callbackEngine)) {
                                invokePythonPluginHandler.callback(eventId, new Gson().toJson(map), callbackEngine);
                            }
                            LogUtils.addLog("on close popup: data map=" + new Gson().toJson(map));
                        }
                        @Override
                        public void beforeShown(@NotNull LightweightWindowEvent event) {}
                    })
                    .createPopup();
            popupRef.set(popup);
            button.addActionListener(e -> {
                confirmHandler(project, expressionEditor.getEditor(), isOk, popupRef, evaluateReference);
            });
            popup.showInBestPositionFor(editor);
            return "success";
        }

        private void confirmHandler(Project project, Editor editor, AtomicBoolean isOk,
                                    AtomicReference<JBPopup> popupRef,
                                    AtomicReference<EvaluateResult> evaluateReference) {
            handleCodeFragment(project, editor, evaluateReference);
            isOk.set(true);
            popupRef.get().closeOk(null);
        }

        private void handleCodeFragment(Project project, Editor editor,
                                        AtomicReference<EvaluateResult> evaluateReference) {
            EvaluateResult result = new EvaluateResult();
            try {
                Assert.assertNotNull(editor);
                Document editorDocument = editor.getDocument();
                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editorDocument);
                Assert.assertNotNull(psiFile);
                boolean hasBlock = PsiTreeUtil.findChildrenOfType(psiFile, PsiBlockStatement.class).size()>0;
                boolean hasLambda = PsiTreeUtil.findChildrenOfType(psiFile, PsiLambdaExpression.class).size()>0;
                if (hasBlock || hasLambda) {
                    result.setFailMsg("not support block statement or lambda expression");
                    evaluateReference.set(result);
                    return;
                }
                List<PsiStatement> statementList = PsiTreeUtil.findChildrenOfType(psiFile, PsiStatement.class)
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                List<String> evaluatedList = new ArrayList<>();
                statementList.stream().forEach(s->{
                    evaluatedList.add(psiStatementToString(s));
                });
                String ognlStr = JavaParseUtils.transToOgnl(psiFile);
                String collect = String.join("\n", evaluatedList);
                result.setSuccess(true);
                result.setEvaluatedJava(collect);
                result.setEvaluatedOgnl(ognlStr);
                LogUtils.addLog("evaluatedList=" + evaluatedList);
                evaluateReference.set(result);
            } catch (Throwable e) {
                LogUtils.addLog("handleCodeFragment error "+ e.getMessage() + " " + e.getStackTrace()[0].toString() + " "+ e.getStackTrace()[1].toString());
                result.setFailMsg("handleCodeFragment error" + e.getMessage());
                evaluateReference.set(result);
            }
        }

        private String psiStatementToString(PsiElement element){
            StringBuilder builder = new StringBuilder();
            for (PsiElement child : element.getChildren()) {
                if(child instanceof PsiReferenceExpression){
                    PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) child;
                    PsiElement resolve = psiReferenceExpression.resolve();
                    if (resolve instanceof PsiClass) {
                        PsiClass aClass = (PsiClass) resolve;
                        builder.append(" ").append(aClass.getQualifiedName());
                        continue;
                    }
                }
                if (child instanceof PsiJavaCodeReferenceElement) {
                    PsiJavaCodeReferenceElement codeReferenceElement = (PsiJavaCodeReferenceElement) child;
                    PsiElement resolve = codeReferenceElement.resolve();
                    if (resolve instanceof PsiClass) {
                        PsiClass aClass = (PsiClass) resolve;
                        builder.append(" ").append(aClass.getQualifiedName());
                        continue;
                    }

                }
                if (child instanceof PsiTypeElement) {
                    PsiTypeElement psiTypeElement = (PsiTypeElement) child;
                    psiTypeElement.getType().getCanonicalText();
                    builder.append( psiTypeElement.getType().getCanonicalText());
                } else if(ArrayUtils.isEmpty(child.getChildren())){
                    builder.append(child.getText());
                } else {
                    builder.append(psiStatementToString(child));
                }
            }
            return builder.toString();
        }


    }
    private static class ProcessingRunnable extends Task.Backgroundable implements Runnable{
        private static final Map<String, ProcessingRunnable> PROCESS_MAP = new ConcurrentHashMap<>();

        private final Semaphore semaphore = new Semaphore(0);

        public ProcessingRunnable(Project project, String title,String processId) {
            super(project, title);
            PROCESS_MAP.put(processId, this);
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            progressIndicator.setIndeterminate(true);
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        public void release(){
            semaphore.release(100);
        }
    }

    private static class SelectItem{
        private String label;
        private String value;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SelectItem that = (SelectItem) o;
            return Objects.equals(label, that.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(label);
        }
    }


    private static class EvaluateResult{
        private boolean success = false;
        private String failMsg;
        private String evaluatedJava;
        private String evaluatedOgnl;

        public EvaluateResult() {
        }

        public EvaluateResult(boolean success, String failMsg, String evaluatedJava, String evaluatedOgnl) {
            this.success = success;
            this.failMsg = failMsg;
            this.evaluatedJava = evaluatedJava;
            this.evaluatedOgnl = evaluatedOgnl;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getFailMsg() {
            return failMsg;
        }

        public void setFailMsg(String failMsg) {
            this.failMsg = failMsg;
        }

        public String getEvaluatedJava() {
            return evaluatedJava;
        }

        public void setEvaluatedJava(String evaluatedJava) {
            this.evaluatedJava = evaluatedJava;
        }

        public String getEvaluatedOgnl() {
            return evaluatedOgnl;
        }

        public void setEvaluatedOgnl(String evaluatedOgnl) {
            this.evaluatedOgnl = evaluatedOgnl;
        }
    }
}
