package io.github.binarybeing.hotcat.plugin.server.controller.java;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import io.github.binarybeing.hotcat.plugin.entity.IdeaParsedJavaEntity;
import io.github.binarybeing.hotcat.plugin.server.controller.BaseEventScriptController;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.JavaParseUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


public class IdeaJavaParserController extends BaseEventScriptController {

    @Override
    public String path() {
        return "/api/idea/java_parser";
    }
    @Override
    protected Response handle(Request request, AnActionEvent event, String script) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return Response.error("project not found");
        }
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        MyJavaParser parser = new MyJavaParser(event);
        context.set("javaParser", parser);
        Object result = expression.evaluate(context);
        return Response.success(result);
    }

    public static class MyJavaParser {
        private AnActionEvent event;

        public MyJavaParser(AnActionEvent event) {
            this.event = event;
        }

        public String getPackage(){
            return null;
        }

        public String getSelectedMethodModel() {
            DataContext context = event.getDataContext();
            VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(context);
            Editor editor = CommonDataKeys.EDITOR.getData(context);
            Assert.assertNotNull("there is no opening file selected ", virtualFile);
            Assert.assertNotNull("no opening editor founded", editor);
            SelectionModel model = editor.getSelectionModel();
            IdeaParsedJavaEntity entity = JavaParseUtils.parseJavaFile(context, virtualFile.getPath(),
                    model.getSelectionStart(), model.getSelectionEnd());
            return new Gson().toJson(entity.toMap());
        }

        public String writeJava(String className, boolean isTest, String content) throws IOException {
            Project project = CommonDataKeys.PROJECT.getData(event.getDataContext());
            VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
            Assert.assertNotNull("project not found", project);
            Assert.assertNotNull("file not found", virtualFile);
            Module module = ProjectFileIndex.SERVICE.getInstance(project).getModuleForFile(virtualFile);
            Assert.assertNotNull("module not found", module);
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            JavaSourceRootType source = isTest ? JavaSourceRootType.TEST_SOURCE : JavaSourceRootType.SOURCE;
            List<VirtualFile> roots = moduleRootManager.getSourceRoots(source);
            Assert.assertTrue("source root not founded", roots.size() > 0);
            String path = roots.get(0).getPath();
            String filePath = path + "/" + className.replace(".", "/") + ".java";
            FileUtils.writeStringToFile(new File(filePath), content, "utf-8");
            return filePath;
        }
        public String insertCode(String javaFile, int line, String[] codeLines) throws IOException {
            File file = new File(javaFile);
            Assert.assertNotNull("file not exist", file);
            VirtualFile jVFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            Assert.assertNotNull("file not exist", jVFile);
            DataContext dataContext = event.getDataContext();
            Project project = CommonDataKeys.PROJECT.getData(dataContext);
            Assert.assertNotNull("project not founded", project);
            Editor data = CommonDataKeys.EDITOR.getData(dataContext);
            PsiFile psiFile = PsiManager.getInstance(project).findFile(jVFile);
            Assert.assertTrue("psi java file not founded", psiFile instanceof PsiJavaFile);
            String content = FileUtils.readFileToString(file, "utf-8");
            List<String> contentLines = content.lines().collect(Collectors.toList());
            StringBuilder builder = new StringBuilder();
            LinkedList<String> stack = new LinkedList<>();
            for (int i = 0; i < contentLines.size(); i++) {
                if (i == line) {
                    String prefix = String.join("", stack);
                    Arrays.stream(codeLines).forEach(codeLine -> builder.append(prefix).append(codeLine).append("\n"));
                }else{
                    contentLines.get(i).chars().forEach(c -> {
                        if (c == '{') {
                            stack.addLast("    ");
                        } else if (c == '}') {
                            stack.removeLast();
                        }
                    });
                }

            }
            content = builder.toString();
            FileUtils.writeStringToFile(file, builder.toString(), "utf-8");
            return content;
        }


    }
}
