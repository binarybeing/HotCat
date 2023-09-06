package io.github.binarybeing.hotcat.plugin.server.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.intellij.icons.AllIcons;
import com.intellij.ide.browsers.actions.WebPreviewEditorProvider;
import com.intellij.ide.browsers.actions.WebPreviewVirtualFile;
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
import io.github.binarybeing.hotcat.plugin.handlers.InvokePythonPluginHandler;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.DialogUtils;
import io.github.binarybeing.hotcat.plugin.utils.JavaParseUtils;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import io.github.binarybeing.hotcat.plugin.utils.SidePanelUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.debugger.JavaDebuggerEditorsProvider;
import org.junit.Assert;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.*;
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

        public IdeaPanel showInput(String label, String field, String defaultValue) {
            JLabel jLabel = new JLabel(label);
            jLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            jPanel.add(jLabel);
            if (StringUtils.isNotEmpty(field)) {
                JTextField jTextField = new JTextField(15);
                jTextField.setText(defaultValue);
                jLabel.setLabelFor(jTextField);
                jPanel.add(jTextField);
                res.put(field, jTextField);
            }
            return this;
        }

        public IdeaPanel showSelect(String label, String field, String[] names, String[] values, String defaultValue) {
            jPanel.add(new JLabel(label));
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
            jPanel.add(box);
            res.put(field, box);
            return this;
        }

        public IdeaPanel showSelect(String label, String field, String[] options, String defaultValue) {
            jPanel.add(new JLabel(label));
            ComboBox<String> box = new ComboBox<>(options);
            if (StringUtils.isNotEmpty(defaultValue)) {
                box.setSelectedItem(defaultValue);
            }
            jPanel.add(box);
            res.put(field, box);
            return this;
        }

        public IdeaPanel showCheck(String label, String value, String field, boolean checked) {
            jPanel.add(new JLabel(label));
            JBCheckBox jCheckBox = new JBCheckBox();
            jCheckBox.setName(field);
            jCheckBox.setText(value);
            jCheckBox.setSelected(checked);
            jPanel.add(jCheckBox);
            res.put(field, jCheckBox);
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

        public String showSidePanelWebBrowser(String url) throws Exception{
            WebPreviewVirtualFile virtualFile = new WebPreviewVirtualFile(event.getProject().getProjectFile(),  Urls.newFromEncoded(url));
            WebPreviewEditorProvider editorProvider = FileEditorProvider.EP_FILE_EDITOR_PROVIDER.findExtension(WebPreviewEditorProvider.class);
            assert editorProvider != null;
            FileEditor fileEditor = editorProvider.createEditor(event.getProject(), virtualFile);
            SidePanelUtils.showSidePanel(event, "testHtml", "", fileEditor.getComponent(), () -> {});
            return "success";
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
