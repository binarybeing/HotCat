package io.github.binarybeing.hotcat.plugin.infra;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;
import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension;
import org.intellij.plugins.markdown.ui.preview.BrowserPipe;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.ui.preview.ResourceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author gn.binarybei
 * @date 2023/2/13
 * @note
 */
public class ExUrlOpenerProvider implements  MarkdownBrowserPreviewExtension.Provider {

    @Nullable
    @Override
    public MarkdownBrowserPreviewExtension createBrowserExtension(@NotNull MarkdownHtmlPanel markdownHtmlPanel) {
        return new InternalOpener(markdownHtmlPanel);
    }

    @SuppressWarnings("UnstableApiUsage")
    private class InternalOpener implements MarkdownBrowserPreviewExtension{
        private MarkdownHtmlPanel panel;
        public InternalOpener(@NotNull MarkdownHtmlPanel panel) {
            this.panel = panel;
            final BrowserPipe.Handler openLink = this::openMarkdownLink;
            if (panel.getBrowserPipe() != null) {
                panel.getBrowserPipe().subscribe("openLink", openLink);
            }
            Disposer.register(this, () -> {
                if (panel.getBrowserPipe() != null) {
                    panel.getBrowserPipe().removeSubscription("openLink", openLink);
                }
            });
        }
        public void openMarkdownLink(String url){
            if (url.startsWith("class://")) {
                openClass(url, panel.getProject());
            }
        }

        private boolean openClass(String url, Project project) {
            String prefix = "class://";
            if (!url.startsWith(prefix)) {
                return false;
            }
            try {
                String className = url.substring(prefix.length());
                String[] split = className.split(":");
                check(split);
                JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                if(fileEditorManager == null ){
                    Messages.showErrorDialog("FileEditorManager not found", "Error");
                    return false;
                }
                if(panel.getVirtualFile()==null){
                    Messages.showErrorDialog("Panel has no file", "Error");
                    return false;
                }
                Module module = ModuleUtil.findModuleForFile(panel.getVirtualFile(), project);
                GlobalSearchScope moduleScope = module == null ? null : GlobalSearchScope.moduleScope(module);
                GlobalSearchScope moduleRuntimeScope = GlobalSearchScope.moduleRuntimeScope(module, true);
                GlobalSearchScope dependentsScope = GlobalSearchScope.moduleWithDependentsScope(module);
                GlobalSearchScope librariesScope = GlobalSearchScope.moduleWithLibrariesScope(module);

                GlobalSearchScope allScope = GlobalSearchScope.allScope(project);

                PsiClass psiClass = findClass(split[0], javaPsiFacade,
                        new GlobalSearchScope[]{moduleScope, moduleRuntimeScope, dependentsScope, librariesScope, allScope});
                if (psiClass == null){
                    Messages.showErrorDialog("Class not found: " + split[0] + " or can not navigate", "Error");
                    return false;
                }
                int line = 1;
                if (split.length > 1 && StringUtils.isNotBlank(split[1])) {
                    line = Integer.parseInt(split[1]);
                }
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, psiClass.getContainingFile().getVirtualFile(), line-1, 1);
                fileEditorManager.openEditor(descriptor, true);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private PsiClass findClass(String className, JavaPsiFacade javaPsiFacade, GlobalSearchScope[] searchScopes) {
            for (GlobalSearchScope searchScope : searchScopes) {
                PsiClass psiClass = javaPsiFacade.findClass(className, searchScope);
                if (psiClass != null) {
                    return psiClass;
                }
            }
            return null;
        }
        private void check(String[] target) {
            if(target.length == 0){
                throw new IllegalArgumentException("url is empty");
            }
            String url = target[0];
            if (StringUtils.isBlank(url)) {
                throw new IllegalArgumentException("url is empty");
            }
            url = url.replaceAll(".java", "");
            url = url.replaceAll("/", ".");
            target[0] = url;
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

        @Override
        public void dispose() {

        }
    }
}
