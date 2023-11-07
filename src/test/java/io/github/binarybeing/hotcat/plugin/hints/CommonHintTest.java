package io.github.binarybeing.hotcat.plugin.hints;

import com.intellij.psi.PsiElement;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import io.github.binarybeing.hotcat.plugin.infra.hint.HotCatFactoryInlayHintsCollector;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommonHintTest extends BaseTest {
    @Override
    public Object doExecute() throws Exception {
//        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
//            Desktop.getDesktop().browse(new URI("https://www.baidu.com"));
////        }
//        String s = "sxx";
//        // PsiJavaToken
//        List<String> list = new ArrayList<>();
//        PsiElement element = super.psiFile.findElementAt(super.editor.getSelectionModel().getSelectionStart());
//        list.add(element.getText());
//        list.add(String.valueOf(element.getTextOffset()));
//        for (PsiElement child : element.getChildren()) {
//            list.add(child.getClass().getSimpleName());
//        }
        Class<HotCatFactoryInlayHintsCollector> aClass = HotCatFactoryInlayHintsCollector.class;

        Field field = aClass.getDeclaredField("listenerMap");
        field.setAccessible(true);
        Map<String, Pair<String, String>> o = (Map<String, Pair<String, String>>)field.get(null);
        ArrayList<String> list = new ArrayList<>();
        for (Pair<String, String> pair : o.values()) {
            list.add(pair.getValue());
        }
        return list.toString();
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert code == 200;
    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-11-25";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }
}
