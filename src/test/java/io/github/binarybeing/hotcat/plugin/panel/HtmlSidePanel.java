package io.github.binarybeing.hotcat.plugin.panel;

import com.intellij.ide.browsers.actions.WebPreviewEditorProvider;
import com.intellij.ide.browsers.actions.WebPreviewVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Urls;
import com.intellij.util.ui.SwingHelper;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import io.github.binarybeing.hotcat.plugin.server.controller.IdeaWebBrowserController;
import io.github.binarybeing.hotcat.plugin.utils.SidePanelUtils;
import org.apache.commons.io.FileUtils;
import org.intellij.plugins.markdown.ui.preview.MarkdownSplitEditorProvider;

import javax.swing.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

public class HtmlSidePanel extends BaseTest {

    @Override
    public Object doExecute() throws Exception {
//        SwingHelper.HtmlViewerBuilder builder = new SwingHelper.HtmlViewerBuilder();
//        JEditorPane editorPane = builder.create();


        File file = new File("/Users/momo/plugin_projects/HotCat/src/test/resources/index.html");
//        editorPane.setText(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
//        VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        WebPreviewVirtualFile virtualFile = new WebPreviewVirtualFile(event.getProject().getProjectFile(),  Urls.newFromEncoded("https://www.baidu.com"));

        WebPreviewEditorProvider editorProvider = FileEditorProvider.EP_FILE_EDITOR_PROVIDER.findExtension(WebPreviewEditorProvider.class);
        FileEditor fileEditor = editorProvider.createEditor(event.getProject(), virtualFile);

        SidePanelUtils.showSidePanel(event, "testHtml", "", fileEditor.getComponent(), () -> {});

        return null;
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {

    }

    @Override
    public long until() throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse("2023-08-30").getTime();
    }

    public static class HotCatWebPreviewEditorProvider extends WebPreviewEditorProvider {

    }
}
