package io.github.binarybeing.hotcat.plugin.panel;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import io.github.binarybeing.hotcat.plugin.server.controller.IdeaPanelController;

import java.text.SimpleDateFormat;

public class BasicPanel extends BaseTest {

    @Override
    public Object doExecute() throws Exception {
        return new IdeaPanelController.IdeaPanel(event).showSelect("test_label", "test_field",
         new String[]{"label1", "label2"}, new String[]{"value1", "value2"} , "value2")
                .showAndGet();
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert code == 200;
    }

    @Override
    public long until() throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse("2023-08-20").getTime();
    }
}
