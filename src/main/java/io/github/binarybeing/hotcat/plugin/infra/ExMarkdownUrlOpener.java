package io.github.binarybeing.hotcat.plugin.infra;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension;
import org.intellij.plugins.markdown.extensions.MarkdownExtension;
import org.intellij.plugins.markdown.extensions.jcef.MarkdownJCEFPreviewExtension;
import org.intellij.plugins.markdown.ui.preview.ResourceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gn.binarybei
 * @date 2023/2/16
 * @note
 */
public class ExMarkdownUrlOpener implements MarkdownJCEFPreviewExtension {
    private ClassUrlOpener opener = new ClassUrlOpener();

    public void openMarkdownLink(String url){
        if (url.startsWith("class://")) {
            DataContext dataContext = DataManager.getInstance().getDataContext();
            Project project = DataKeys.PROJECT.getData(dataContext);
            if (project == null) {
                return;
            }
            Editor editor = DataKeys.EDITOR.getData(dataContext);
            if (editor == null) {
                return;
            }
            //get editor PsiFile
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            opener.openClass(url, project, psiFile);
        }
    }


    @NotNull
    @Override
    public Map<String, Function1<String, Unit>> getEvents() {
        Map<String , Function1<String, Unit>> map = new HashMap<>(2);
        map.put("openLink", s -> {
            openMarkdownLink(s);
            return Unit.INSTANCE;
        });
        return map;
    }

    @NotNull
    @Override
    public Priority getPriority() {
        return Priority.BEFORE_ALL;
    }

    @NotNull
    @Override
    public ResourceProvider getResourceProvider() {
        return ResourceProvider.Companion.getDefault();
    }

    @NotNull
    @Override
    public List<String> getScripts() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<String> getStyles() {
        return Collections.emptyList();
    }

    @Override
    public int compareTo(@NotNull MarkdownBrowserPreviewExtension markdownBrowserPreviewExtension) {
        return 1;
    }
}
