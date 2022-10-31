package io.github.binarybeing.hotcat.plugin.server.controller;

import com.google.gson.JsonObject;
import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkType;
import com.intellij.ide.bookmark.BookmarksManager;
import com.intellij.ide.bookmark.FileBookmark;
import com.intellij.ide.bookmark.providers.LineBookmarkImpl;
import com.intellij.ide.bookmark.providers.LineBookmarkProvider;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
import io.github.binarybeing.hotcat.plugin.utils.JsonUtils;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class IdeaEditorController extends AbstractController {
    @Override
    String path() {
        return "/api/idea/editor";
    }
    @Override
    public Response handle(Request request) {
        Long eventId = JsonUtils.readJsonLongValue(request.getJsonObject(), "eventId");
        String script = JsonUtils.readJsonStringValue(request.getJsonObject(), "script");

        AnActionEvent event = EventContext.getEvent(eventId);
        if (event == null) {
            return Response.error("event not found");
        }
        if (StringUtils.isEmpty(script)) {
            return Response.error("script is empty");
        }

        DataContext dataContext = event.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            return Response.error("editor not found");
        }
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return Response.error("project not found");
        }
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        if (virtualFile == null) {
            return Response.error("virtualFile not found");
        }

        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("editor", editor);
        return ApplicationRunnerUtils.run(() -> {
            Object result = expression.evaluate(context);
            return Response.success(result);
        });
    }


}
