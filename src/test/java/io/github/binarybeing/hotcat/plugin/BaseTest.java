package io.github.binarybeing.hotcat.plugin;

import com.google.gson.Gson;
import io.github.binarybeing.hotcat.plugin.editor.Editor;
import javassist.ClassPool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        basePath = BaseTest.class.getClassLoader().getResource("").getPath();
    }
    @Test
    public void test() throws Exception{
        String s = System.getenv("test.open");
        if(!"true".equals(s)){
            System.out.println("skip test class, set env test.open=true to open");
            return;
        }
        Class<?> aClass = this.getTestClass();
        if(aClass == null){
            System.out.println("skip test class, set env test.open=true to open");
            return;
        }

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
