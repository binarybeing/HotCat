package io.github.binarybeing.hotcat.plugin.server.controller;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.handlers.InvokePythonPluginHandler;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IdeaEventController extends BaseEventScriptController{
    @Override
    public String path() {
        return "/api/idea/events";
    }

    @Override
    protected Response handle(Request request, AnActionEvent event, String script) {
        HotCatIdeaEvents ideaEvents = new HotCatIdeaEvents(event);
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("events", ideaEvents);
        Object result = expression.evaluate(context);
        return Response.success(result);
    }


    public static class HotCatIdeaEvents{
        private AnActionEvent event;

        public HotCatIdeaEvents(AnActionEvent event) {
            this.event = event;
        }

        //#
        public String listenFileOpened(){
            if (event.getProject() == null) {
                throw new RuntimeException("event has no project");
            }
            Long eventId = EventContext.getEventId(event);
            Optional<PluginEntity> plugin = EventContext.getPluginEntity(eventId);
            plugin.ifPresent(p->{
                FileEditorManager editorManager = FileEditorManager.getInstance(event.getProject());
                String path = p.getFile().getAbsolutePath();
                if (!path.endsWith("/")) {
                    path += "/";
                }
                String callPath = path + " callback.py";
                editorManager.addFileEditorManagerListener(new FileEditorManagerListener() {
                    @Override
                    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        InvokePythonPluginHandler invokePythonPluginHandler = new InvokePythonPluginHandler();
                        Map<String, Object> map = new HashMap<>();
                        map.put("file_path", file.getPath());
                        map.put("event_time", System.currentTimeMillis());
                        invokePythonPluginHandler.actionCallback(eventId, "file_opened", new Gson().toJson(map), callPath);
                    }
                });
            });
            return String.valueOf(plugin.isPresent());
        }

    }
}
