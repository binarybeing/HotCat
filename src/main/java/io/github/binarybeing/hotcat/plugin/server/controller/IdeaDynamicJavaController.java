package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
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
        Object result = expression.evaluate(context);
        return Response.success(result);
    }

    public static class JavaExecutor {

        private AnActionEvent event;
        private JavaExecutor(AnActionEvent event){
            this.event = event;
        }

        public List<Object> execute(String className, String classBytes) throws Exception {
            byte[] bytes = classBytes.getBytes();
            List<Object> invokeRes = new ArrayList<>();
            try {

                Class<?> aClass = hotCatClassLoader.defineClass(className, Base64.getDecoder().decode(bytes));
                Method[] classMethods = aClass.getMethods();
                for (Method method : classMethods) {
                    if (method.getParameterTypes().length == 1
                            && method.getParameterTypes()[0].getName().equals(AnActionEvent.class.getName())) {
                        invokeRes.add(invokeMethod(aClass, method));
                    }
                }
                return invokeRes;
            }catch (Exception e){
                LogUtils.addError(e, "execute class error：" + className);
                throw e;
            }finally {
                hotCatClassLoader = new HotCatClassLoader();
            }
        }

        private Object invokeMethod(Class<?> defineClass, Method method) throws Exception {
            Object o = defineClass.newInstance();
            return method.invoke(o, event);
        }

    }

    private static class HotCatClassLoader extends ClassLoader{
        private ClassLoader classLoader = IdeaDynamicJavaController.class.getClassLoader();

        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return this.classLoader.loadClass(name);
        }
    }

}
