package io.github.binarybeing.hotcat.plugin.panel;

import com.google.common.cache.RemovalListener;
import com.google.common.collect.Lists;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.evaluation.expression.EvaluatorBuilderImpl;
import com.intellij.debugger.engine.evaluation.expression.ExpressionEvaluator;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.popup.NotLookupOrSearchCondition;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.intellij.xdebugger.impl.ui.XDebuggerEditorBase;
import com.intellij.xdebugger.impl.ui.XDebuggerExpressionEditor;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import io.github.binarybeing.hotcat.plugin.handlers.InvokePythonPluginHandler;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.debugger.JavaDebuggerEditorsProvider;
import org.junit.Assert;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class FloatConditionPanel extends BaseTest {

//    @Override
    public Object execute(AnActionEvent event,String stop) throws Exception {
        ArrayList<Object> list = new ArrayList<>();
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(event.getDataContext());
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());

        CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());

        XSourcePositionImpl position = XSourcePositionImpl.create(virtualFile, 50);

        Optional<PsiMethod> methodOptional = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class)
                .stream().filter(p -> Objects.equals(p.getName(), "execute"))
                .findAny();
        JavaCodeFragment fragment = JavaCodeFragmentFactory.getInstance(event.getProject()).createCodeBlockCodeFragment("event.getProject()", methodOptional.get(), false);

        ExpressionEvaluator evaluator = EvaluatorBuilderImpl.getInstance().build(fragment, SourcePosition.createFromLine(psiFile, 50));

//        Value value = evaluator.getValue();
//        list.add(value.toString());


        JavaDebuggerEditorsProvider editorsProvider = new JavaDebuggerEditorsProvider();







//        PsiTreeUtil.findChildrenOfType(fragment, PsiReferenceExpression.class)
//                .forEach(r->{
//                    list.add(r.getReferenceName() + " " + (r.resolve() == null ? "null" : r.resolve()));
//                });



        final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(event.getProject());
        PsiPackage aPackage = psiFacade.findPackage("");

        PsiExpressionCodeFragment codeFragment = JavaCodeFragmentFactory.getInstance(event.getProject()).createExpressionCodeFragment("return event.getProject()", aPackage, null, true);
        LogUtils.addLog("codeFragment = " + codeFragment);



        for (PsiElement child : codeFragment.getChildren()) {

//            PsiTreeUtil.findChildrenOfType(child, PsiReferenceExpression.class)
//                    .forEach(r->{
//                        list.add(r.getReferenceName() + " " + (r.resolve() == null ? "null" : r.resolve()));
//            });


        }
        return list.toString();

//        IdeaPanelController.IdeaPanel panel = new IdeaPanelController.IdeaPanel(event);
//
//        panel.showFloatMiniEditor("hello", "/Users/momo/plugin_projects/HotCat/src/test/java/io/github/binarybeing/hotcat/plugin/panel/FloatConditionPanel.java",
//                65, "", false);

//        return "success";
    }


    @Override
    public Object doExecute() throws Exception {
         String hello_a = "";
        Project project = event.getProject();
        Long eventId = 999999999L;
        String title = "this is my title";
        JavaDebuggerEditorsProvider editorsProvider = new JavaDebuggerEditorsProvider();

        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        Editor editor = CommonDataKeys.EDITOR.getData(event.getDataContext());
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(event.getDataContext());



        int line = editor.getSelectionModel().getSelectionStartPosition().line + 1;
        Assert.assertNotNull(editor);
        Assert.assertNotNull(editor.getSelectionModel());
        XSourcePositionImpl position = XSourcePositionImpl.create(virtualFile, line);



        XDebuggerExpressionEditor expressionEditor =
                        new XDebuggerExpressionEditor(project,
                                editorsProvider,
                                "", position,
                                XExpressionImpl.EMPTY_EXPRESSION, true, true, false) {
                            @Override
                            protected JComponent decorate(JComponent component, boolean multiline, boolean showEditor) {
                                return component;
                            }
                        };


        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(expressionEditor.getComponent(),
                        null)
                .setDimensionServiceKey(project, "ShowHotCatPopup", false);

        Class<? extends XDebuggerEditorBase> editorClass = XDebuggerEditorBase.class;
        Field contextMethod = editorClass.getDeclaredField("myContext");
        contextMethod.setAccessible(true);

        JButton button = new JButton("OK", AllIcons.Actions.Commit);
        button.setToolTipText("ctrl + enter");
        AtomicBoolean isOk = new AtomicBoolean(false);
        AtomicReference<JBPopup> popupRef = new AtomicReference<>(null);
        AtomicReference<Pair<Boolean, String >> evaluateResult = new AtomicReference<>(null);

        java.lang.StringBuilder builder = new  java.lang.StringBuilder();
        String javaCode = "com.intellij.openapi.module.ModuleManager instance =  com.intellij.openapi.module.ModuleManager.getInstance(project)";



        final JBPopup popup = popupBuilder.setRequestFocusCondition(project, NotLookupOrSearchCondition.INSTANCE)
                .setProject(project)
                .setMinSize(new Dimension(600, 250))
                .setResizable(true)
                .setMovable(true)
                .setKeyboardActions(Lists.newArrayList(Pair.create(e -> closeHandler(project, expressionEditor.getEditor(), isOk, popupRef, evaluateResult),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK)),
                        Pair.create(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                evaluateWhenStrokeEnter(project, expressionEditor.getEditor(), evaluateResult);
                            }
                        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK))))


                .setCancelButton(new IconButton(null, AllIcons.Actions.Close))
                .setTitle(title)
                .setSettingButtons(button)
                .setCancelOnClickOutside(false)
                .setCancelOnOtherWindowOpen(false)
                .setCancelOnMouseOutCallback(event1 -> false)
                .addListener(new JBPopupListener() {
                    @Override
                    public void onClosed(@NotNull LightweightWindowEvent event) {
                        Map<String, Object> map = new HashMap<>(2);
                        InvokePythonPluginHandler invokePythonPluginHandler = new InvokePythonPluginHandler();
                        try {
                            if (evaluateResult.get().first) {
                                map.put("code", "200");
                                map.put("text", evaluateResult.get().second);
                            } else {
                                map.put("code", "500");
                                map.put("msg", evaluateResult.get().second);
                            }

                        } catch (Throwable e) {
                            map.put("code", "500");
                            map.put("msg", e.getMessage());
                            LogUtils.addLog("response msg error: "  + e.getStackTrace()[0].toString() + " "+ e.getStackTrace()[1].toString());
                        }
                        //invokePythonPluginHandler.response(eventId, new Gson().toJson(map));
                    }
                    @Override
                    public void beforeShown(@NotNull LightweightWindowEvent event) {}
                })
                .createPopup();
        popupRef.set(popup);
        button.addActionListener(e -> {
            closeHandler(project, expressionEditor.getEditor(), isOk, popupRef, evaluateResult);
        });

        popup.showInBestPositionFor(editor);

        return "success";

    }

    private void evaluateWhenStrokeEnter(Project project, Editor editor, AtomicReference<Pair<Boolean, String>> evaluateResult) {
        handleCodeFragment(project, editor, evaluateResult);
        LogUtils.addLog("evaluateResult=" + evaluateResult.get().second);
    }

    private void closeHandler(Project project, Editor editor, AtomicBoolean isOk, AtomicReference<JBPopup> popupRef, AtomicReference<Pair<Boolean, String>> evaluateResult) {
        handleCodeFragment(project, editor, evaluateResult);
        isOk.set(true);
        popupRef.get().closeOk(null);
    }

    private void handleCodeFragment(Project project, Editor editor, AtomicReference<Pair<Boolean, String>> evaluateResult) {
        try {
            Assert.assertNotNull(editor);
            Document editorDocument = editor.getDocument();
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editorDocument);
            Assert.assertNotNull(psiFile);
            ArrayList<Object> list = new ArrayList<>();
            LogUtils.addLog("step psiFile: " + psiFile.getClass());
            boolean hasBlock = PsiTreeUtil.findChildrenOfType(psiFile, PsiBlockStatement.class).size()>0;
            if (hasBlock) {
                evaluateResult.set(Pair.create(false, "not support block statement"));
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
            evaluateResult.set(Pair.create(true, evaluatedList.toString()));
            LogUtils.addLog("evaluatedList=" + evaluatedList);
        } catch (Throwable e) {
            LogUtils.addLog("handleCodeFragment error "+ e.getMessage() + " " + e.getStackTrace()[0].toString() + " "+ e.getStackTrace()[1].toString());
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



    @Override
    public void verify(int code, String msg, String data) throws Exception {

    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-08-31";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }
}
