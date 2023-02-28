//package io.github.binarybeing.hotcat.plugin.infra;
//
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.fileEditor.FileEditorManager;
//import com.intellij.openapi.fileEditor.OpenFileDescriptor;
//import com.intellij.openapi.module.Module;
//import com.intellij.openapi.module.ModuleManager;
//import com.intellij.openapi.module.ModuleUtil;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.ui.Messages;
//import com.intellij.openapi.util.Disposer;
//import com.intellij.psi.JavaPsiFacade;
//import com.intellij.psi.PsiClass;
//import com.intellij.psi.search.GlobalSearchScope;
//import org.apache.commons.lang3.StringUtils;
//import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension;
//import org.intellij.plugins.markdown.ui.preview.BrowserPipe;
//import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
//import org.intellij.plugins.markdown.ui.preview.ResourceProvider;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Collections;
//import java.util.List;
//
///**
// * @author gn.binarybei
// * @date 2023/2/13
// * @note
// */
//public class ExUrlOpenerProvider implements  MarkdownBrowserPreviewExtension.Provider {
//    private ClassUrlOpener()
//    @Nullable
//    @Override
//    public MarkdownBrowserPreviewExtension createBrowserExtension(@NotNull MarkdownHtmlPanel markdownHtmlPanel) {
//        return new InternalOpener(markdownHtmlPanel);
//    }
//
//    @SuppressWarnings("UnstableApiUsage")
//    private class InternalOpener implements MarkdownBrowserPreviewExtension{
//        private MarkdownHtmlPanel panel;
//        public InternalOpener(@NotNull MarkdownHtmlPanel panel) {
//            this.panel = panel;
//            final BrowserPipe.Handler openLink = this::openMarkdownLink;
//            if (panel.getBrowserPipe() != null) {
//                panel.getBrowserPipe().subscribe("openLink", openLink);
//            }
//            Disposer.register(this, () -> {
//                if (panel.getBrowserPipe() != null) {
//                    panel.getBrowserPipe().removeSubscription("openLink", openLink);
//                }
//            });
//        }
//        public void openMarkdownLink(String url){
//            if (url.startsWith("class://")) {
//                openClass(url, panel.getProject());
//            }
//        }
//
//
//
//        @NotNull
//        @Override
//        public Priority getPriority() {
//            return Priority.AFTER_ALL;
//        }
//
//        @NotNull
//        @Override
//        public ResourceProvider getResourceProvider() {
//            return ResourceProvider.Companion.getDefault();
//        }
//
//        @NotNull
//        @Override
//        public List<String> getScripts() {
//            return Collections.emptyList();
//        }
//
//        @NotNull
//        @Override
//        public List<String> getStyles() {
//            return Collections.emptyList();
//        }
//
//        @Override
//        public int compareTo(@NotNull MarkdownBrowserPreviewExtension markdownBrowserPreviewExtension) {
//            return 1;
//        }
//
//        @Override
//        public void dispose() {
//
//        }
//    }
//}
