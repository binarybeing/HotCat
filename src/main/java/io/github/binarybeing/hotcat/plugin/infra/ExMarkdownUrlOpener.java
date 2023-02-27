//package io.github.binarybeing.hotcat.plugin.infra;
//
//import com.intellij.ide.DataManager;
//import com.intellij.openapi.actionSystem.DataContext;
//import com.intellij.openapi.actionSystem.DataKeys;
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.fileEditor.FileEditorManager;
//import com.intellij.openapi.fileEditor.OpenFileDescriptor;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.ui.Messages;
//import com.intellij.psi.JavaPsiFacade;
//import com.intellij.psi.PsiClass;
//import com.intellij.psi.search.GlobalSearchScope;
//import kotlin.Unit;
//import kotlin.jvm.functions.Function1;
//import org.apache.commons.lang3.StringUtils;
//import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension;
//import org.intellij.plugins.markdown.extensions.jcef.MarkdownJCEFPreviewExtension;
//import org.intellij.plugins.markdown.ui.preview.ResourceProvider;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author gn.binarybei
// * @date 2023/2/16
// * @note
// */
//public class ExMarkdownUrlOpener implements MarkdownJCEFPreviewExtension {
//
//    public void openMarkdownLink(String url){
//        if (url.startsWith("class://")) {
//            DataContext dataContext = DataManager.getInstance().getDataContext();
//            Project project = DataKeys.PROJECT.getData(dataContext);
//            if (project == null) {
//                return;
//            }
//            openClass(url, project);
//        }
//    }
//
//    private boolean openClass(String url, Project project) {
//        String prefix = "class://";
//        if (!url.startsWith(prefix)) {
//            return false;
//        }
//        try {
//            String className = url.substring(prefix.length());
//            String[] split = className.split(":");
//            GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
//            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
//            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
//            ApplicationManager.getApplication().invokeLater(()->{
//                if(fileEditorManager == null){
//                    Messages.showErrorDialog("FileEditorManager not found", "Error");
//                    return;
//                }
//
//                PsiClass psiClass = javaPsiFacade.findClass(split[0], searchScope);
//                if (psiClass == null) {
//                    Messages.showErrorDialog("Class not found: "+ split[0] +" or can not navigate", "Error");
//                    return;
//                }
//                int line = 1;
//                if (split.length > 1 && StringUtils.isNotBlank(split[1])) {
//                    line = Integer.parseInt(split[1]);
//                }
//                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, psiClass.getContainingFile().getVirtualFile(), line-1, 1);
//                fileEditorManager.openEditor(descriptor, true);
//            });
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    @NotNull
//    @Override
//    public Priority getPriority() {
//        return Priority.AFTER_ALL;
//    }
//
//    @NotNull
//    @Override
//    public ResourceProvider getResourceProvider() {
//        return ResourceProvider.Companion.getDefault();
//    }
//
//    @NotNull
//    @Override
//    public List<String> getScripts() {
//        return Collections.emptyList();
//    }
//
//    @NotNull
//    @Override
//    public List<String> getStyles() {
//        return Collections.emptyList();
//    }
//
//    @Override
//    public int compareTo(@NotNull MarkdownBrowserPreviewExtension markdownBrowserPreviewExtension) {
//        return 1;
//    }
//
//    @NotNull
//    @Override
//    public Map<String, Function1<String, Unit>> getEvents() {
//        Map<String , Function1<String, Unit>> map = new HashMap<>(2);
//        map.put("openLink", s -> {
//            openMarkdownLink(s);
//            return Unit.INSTANCE;
//        });
//        return map;
//    }
//
//
//    //@Override
//    //public void dispose() {
//    //
//    //}
//}
