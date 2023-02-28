package io.github.binarybeing.hotcat.plugin.infra;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.apache.commons.lang3.StringUtils;
import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension;
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
public class ExMarkdownUrlOpenerExtension implements MarkdownJCEFPreviewExtension {
    private ClassUrlOpener classUrlOpener = new ClassUrlOpener();

    public void openMarkdownLink(String url){
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
        classUrlOpener.openClass(url, project, psiFile);
    }

    @NotNull
    @Override
    public Priority getPriority() {
        return Priority.AFTER_ALL;
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

}
