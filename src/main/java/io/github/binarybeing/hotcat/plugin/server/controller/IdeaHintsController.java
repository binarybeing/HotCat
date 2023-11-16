package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.infra.hint.HotCatFactoryInlayHintsCollector;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.JsonUtils;
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
        Long eventId = JsonUtils.readJsonLongValue(request.getJsonObject(), "eventId");
        HotCatHints hints = new HotCatHints(event, eventId);

        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("hints", hints);
        Object result = expression.evaluate(context);
        return Response.success(result);
    }

    public static class HotCatHints {
        private AnActionEvent event;
        private Long eventId;

        public HotCatHints(AnActionEvent event, Long eventId) {
            this.event = event;
            this.eventId = eventId;
        }

        public String listenHintCollect(String pythonListenerScriptPath){
            EventContext.getPluginEntity(eventId)
                    .ifPresent(p->{
                        HotCatFactoryInlayHintsCollector.listenHintCollectEvent(p, pythonListenerScriptPath);
                    });
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
