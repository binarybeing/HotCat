package io.github.binarybeing.hotcat.plugin.parser;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiElement;
import io.github.binarybeing.hotcat.plugin.BaseTest;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ParserTest extends BaseTest {
    /**
     *
     * Object
     * [class com.intellij.psi.impl.compiled.ClsFileImpl     class com.intellij.psi.impl.compiled.ClsClassImpl]
     *
     * execute
     * [class com.intellij.psi.impl.source.PsiClassImpl     class com.intellij.psi.impl.source.PsiMethodImpl]
     *
     * AnActionEvent
     * [class com.intellij.psi.impl.compiled.ClsFileImpl     class com.intellij.psi.impl.compiled.ClsClassImpl]
     *
     * public Object execute(AnActionEvent event
     * [class com.intellij.psi.impl.source.PsiParameterListImpl     class com.intellij.psi.impl.source.PsiParameterImpl]
     *
     * CommonDataKeys.PSI_ELEMENT
     * [class com.intellij.psi.impl.compiled.ClsClassImpl     class com.intellij.psi.impl.compiled.ClsFieldImpl]
     *
     * PSI_ELEMENT.getData
     * [class com.intellij.psi.impl.compiled.ClsClassImpl     class com.intellij.psi.impl.compiled.ClsMethodImpl]
     *
     * element.getParent()
     * [class com.intellij.psi.impl.compiled.ClsClassImpl     class com.intellij.psi.impl.compiled.ClsMethodImpl]
     *
     * @param event
     * @return
     * @throws Exception
     */


    public Object doExecute() throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        // com.intellij.psi.impl.compiled.ClsClassImpl
        int i = RandomUtils.nextInt();

        PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(event.getDataContext());

        String parentClass = element.getParent()==null?"":element.getParent().getClass().toString();

        return toString(element).toString();
    }

    private List<String> toString(PsiElement element) {
        List<String> list = new ArrayList<>();
        list.add(element.getClass().getName() + " " + element.getText()+"\n");
        for (PsiElement child : element.getChildren()) {
            list.addAll(toString(child) );
        }
        return list;
    }

//    @Override
//    public Object execute(AnActionEvent event) throws Exception {
//        return null;
//    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        Assert.assertEquals(200, code);
    }

    @Override
    public long until() throws Exception {
        String expireAt = "2023-09-31";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }
}
