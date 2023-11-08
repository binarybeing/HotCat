package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import io.github.binarybeing.hotcat.plugin.infra.hint.HotCatFactoryInlayHintsCollector;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.util.ArrayList;
import java.util.List;

public class IdeaHintsController extends BaseEventScriptController {

    @Override
    public String path() {
        return "/api/idea/hints";
    }


    @Override
    protected Response handle(Request request, AnActionEvent event, String script) {
        HotCatHints hints = new HotCatHints(event);
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("hints", hints);
        context.set("enableBlockOverLimitMsg", "__open_baidu");
        Object result = expression.evaluate(context);
        return Response.success(result);
    }

    public static class HotCatHints {
        private List<String[]> list = new ArrayList<>();

        private List<String[]> fileHintList = new ArrayList<>();
        private List<String> toRemove = new ArrayList<>();
        private List<String[]> fileHintRemoveList = new ArrayList<>();
        private AnActionEvent event;

        public HotCatHints(AnActionEvent event) {
            this.event = event;
        }
        //        public HotCatHints addHint(String text, String hint) {
//            return addHint(text, hint, null);
//        }

        public HotCatHints addHint(String text, String hint, String jumpUrl) {
            list.add(new String[]{text, hint, jumpUrl});
            return this;
        }
        public HotCatHints removeHint(String text) {
            toRemove.add(text);
            return this;
        }
        public HotCatHints addFileHint(String filePath, String text, String hint, String jumpUrl) {
            fileHintList.add(new String[]{filePath, text, hint, jumpUrl});
            return this;
        }

        public HotCatHints removeFileHint(String filePath, String text, String hint) {
            fileHintRemoveList.add(new String[]{filePath, text, hint});
            return this;
        }

        public String done(){
            for (String[] strings : list) {
                HotCatFactoryInlayHintsCollector.register(strings[0], strings[1], strings[2]);
            }
            for (String s : toRemove) {
                HotCatFactoryInlayHintsCollector.unregister(s);
            }
            for (String[] strings : fileHintList) {
                HotCatFactoryInlayHintsCollector.registerFile(strings[0], strings[1], strings[2], strings[3]);
            }
            for (String[] strings : fileHintRemoveList) {
                HotCatFactoryInlayHintsCollector.unregisterFile(strings[0], strings[1], strings[2]);
            }

            return "ok";
        }

        public String refresh(){
            PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(event.getDataContext());
            if (psiFile instanceof PsiJavaFile && event.getProject() != null) {
                FileEditorManager instance = FileEditorManager.getInstance(event.getProject());
                instance.closeFile(psiFile.getVirtualFile());
                instance.openFile(psiFile.getVirtualFile(), true);
            }
            return "ok";
        }
    }
}
