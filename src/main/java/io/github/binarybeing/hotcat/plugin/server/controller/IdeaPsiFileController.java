package io.github.binarybeing.hotcat.plugin.server.controller;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gn.binarybei
 * @date 2022/9/28
 * @note
 */
public class IdeaPsiFileController extends BaseEventScriptController {
    @Override
    public String path() {
        return "/api/idea/psi_file";
    }

    @Override
    protected @NotNull Response handle(Request request, AnActionEvent event, String script) {

        DataContext dataContext = event.getDataContext();
        PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        if (psiFile == null) {
            return Response.error("psiFile not found");
        }
        JexlExpression expression = super.jexlEngine.createExpression(script);
        HotCatFile catFile = new HotCatFile(event);
        MapContext context = new MapContext();
        context.set("psiFile", psiFile);
        context.set("hotcatFile", catFile);
        Object result = expression.evaluate(context);
        return Response.success(result);
    }

    private void playLand(PsiFile psiFile){
        psiFile.getFileType().getName();
        psiFile.getVirtualFile().getPath();
        Project project = psiFile.getProject();
    }

    public static class HotCatFile{
        private AnActionEvent event;

        public HotCatFile(AnActionEvent event) {
            this.event = event;
        }

        public String getJavaTokens(){
            PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(event.getDataContext());
            List<String> list = PsiTreeUtil.collectElementsOfType(psiFile, PsiJavaToken.class)
                    .stream().map(PsiElement::getText).collect(Collectors.toList());
            return new Gson().toJson(list);
        }
    }

}
