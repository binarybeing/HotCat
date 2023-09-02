package io.github.binarybeing.hotcat.plugin;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author gn.binarybei
 * @date 2023/3/24
 * @note
 */
public abstract class BaseTest {
    private int port = 17122;

    protected Class<?>  getTestClass(){
        throw new RuntimeException("please override this method");
    }
    private static final String basePath;
    static {
        URL url = BaseTest.class.getClassLoader().getResource("");
        if (url != null) {
            basePath = url.getPath();
        } else {
            basePath = null;
        }
    }
    @Test
    public void test() throws Exception{
        String s = System.getenv("test.open");
        boolean expired = System.currentTimeMillis() > until();
        if(!"true".equals(s) && expired){
            System.out.println("skip test class, set env test.open=true to open");
            return;
        }
        Class<?> aClass = this.getClass();

        List<Pair<String, String >> list = new ArrayList<>();
        File[] files = new File(basePath).listFiles();
        for (File file : files) {
            handleClassDependency(list, "", file.getAbsolutePath());
        }
        StringBuilder script = new StringBuilder();
        script.append("java_executor");
        for (Pair<String, String> pair : list) {
            script.append(String.format(".prepareClass(\"%s\", \"%s\")", pair.getLeft(), pair.getRight()));
        }
        script.append(String.format(".execute(\"%s\")", aClass.getName()));
        HttpRequest.Builder builder = HttpRequest.newBuilder(new URI("http://localhost:"+port+"/api/idea/java_executor"));
        builder.setHeader("Content-Type", "text/json;charset=utf-8");
        Map<String, String> map = new HashMap<>();
        map.put("eventId", "999999999");
        map.put("script", script.toString());
        HttpRequest.Builder post = builder.POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(map)));
        HttpRequest request = post.build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> resp = new Gson().fromJson(response.body(), Map.class);
        System.out.println(this.getClass() + " run resp:");
        System.out.println(resp.get("code"));
        System.out.println(resp.get("msg"));
        System.out.println(resp.get("data"));
        verify(Double.valueOf(resp.get("code").toString()).intValue(),
                String.valueOf(resp.get("msg")),
                String.valueOf(resp.get("data")));
    }
    protected Project project;
    protected Editor editor;
    protected VirtualFile virtualFile;
    protected PsiFile psiFile;

    protected DataContext dataContext;

    protected AnActionEvent event;
    public Object execute(AnActionEvent event) throws Exception{
        this.event = event;
        project = event.getProject();
        dataContext = event.getDataContext();
        editor = CommonDataKeys.EDITOR.getData(dataContext);
        psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);

        Method[] methods = this.getClass().getMethods();
        List<Object> list = new ArrayList<>();
        // test, wait, toString, hashCode, getClass, notify, notifyAll
        String[] ignoreMethods = new String[]{"test", "wait", "toString", "hashCode", "getClass", "notify", "notifyAll", "until"};
        for (Method method : methods) {
            if (Arrays.stream(method.getAnnotations()).anyMatch(a -> Objects.equals(a.annotationType(), Override.class))) {
                continue;
            }
            if (Arrays.stream(method.getAnnotations()).anyMatch(a -> Objects.equals(a.annotationType(), Ignore.class))) {
                continue;
            }
            if(method.getParameterCount() > 0){
                continue;
            }
            if(Arrays.stream(ignoreMethods).anyMatch(s -> s.equals(method.getName()))){
                continue;
            }
            list.add(method.invoke(this));
        }
        return list;
    }

    public abstract Object doExecute() throws Exception;
    public abstract void verify(int code, String msg, String data) throws Exception;

    public long until() throws Exception{
        String expireAt = "2023-08-25";
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse(expireAt).getTime();
    }

    private void handleClassDependency(List<Pair<String, String >> list, String prefix, String path){
        File file = new File(path);
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                handleClassDependency(list, prefix + file.getName() + ".", file1.getAbsolutePath());
            }
        }else {
            if(file.getName().endsWith(".class")){
                try {
                    byte[] bytes = FileUtils.readFileToByteArray(file);
                    byte[] encode = Base64.getEncoder().encode(bytes);
                    String name = file.getName().substring(0, file.getName().length() - 6);
                    list.add(Pair.of(prefix + name, new String(encode)));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
