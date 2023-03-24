package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AccessFlag;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gn.binarybei
 * @date 2023/3/24
 * @note
 */
public class IdeaDynamicJavaController extends BaseEventScriptController{

    private static HotCatClassLoader hotCatClassLoader = new HotCatClassLoader();

    @Override
    String path() {
        return "/api/idea/java_executor";
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
        JavaExecutor javaExecutor = new JavaExecutor(event);
        context.set("java_executor", javaExecutor);
        return ApplicationRunnerUtils.run(() -> {
            Object result = expression.evaluate(context);
            return Response.success(result);
        });
    }
    private static class HotCatClassLoader extends ClassLoader{

        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

    public static class JavaExecutor {
        private JavaCompiler javaCompiler;

        private AnActionEvent event;
        private JavaExecutor(AnActionEvent event){
            this.javaCompiler = ToolProvider.getSystemJavaCompiler();
            this.event = event;
        }


        public List<Object> execute(String javaSourceCode) throws Exception {
            File file = File.createTempFile("JavaExecutable", ".java");
            file.deleteOnExit();
            PrintWriter writer = new PrintWriter(Files.newOutputStream(file.toPath()));
            writer.println(javaSourceCode);
            writer.close();
            int run = javaCompiler.run(null, null, null, "-d", file.getParent(), file.getPath());
            if(run != 0){
                throw new RuntimeException("compile error");
            }
            File classFile = new File("JavaExecutable.class");
            CtClass ctClass = getCtClass(classFile);
            Class<?> defineClass = hotCatClassLoader.defineClass(ctClass.getName(), Files.readAllBytes(classFile.toPath()));
            CtMethod[] methods = ctClass.getMethods();
            List<Object> invokeRes = new ArrayList<>();
            for (CtMethod method : methods) {
                // not static
                if ((method.getModifiers() & AccessFlag.STATIC) > 0) {
                    LogUtils.addLog("class " + ctClass.getName() + "method " + method.getName() + " is static, skip");
                    continue;
                }

                if (method.getParameterTypes().length == 1
                        && method.getParameterTypes()[0].getName().equals(AnActionEvent.class.getName())) {
                    invokeRes.add(invokeMethod(defineClass, method));
                }
            }
            return invokeRes;
        }

        private Object invokeMethod(Class<?> defineClass, CtMethod method) throws Exception {
            Object o = defineClass.newInstance();
            return defineClass.getMethod(method.getName(), AnActionEvent.class).invoke(o, event);
        }

        private CtClass getCtClass(File classFile) throws Exception {
            return ClassPool.getDefault().makeClass(new FileInputStream(classFile));

        }
    }
}
