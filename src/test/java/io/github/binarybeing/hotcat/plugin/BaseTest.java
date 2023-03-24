package io.github.binarybeing.hotcat.plugin;

import com.google.gson.Gson;
import io.github.binarybeing.hotcat.plugin.editor.Editor;
import org.junit.Test;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gn.binarybei
 * @date 2023/3/24
 * @note
 */
public class BaseTest {
    private int port = 17123;

    protected Class<?>  getTestClass(){
        throw new RuntimeException("please override this method");
    }

    @Test
    public void test() throws Exception{
        Class<?> aClass = getTestClass();
        String classPath = aClass.getName().replace(".", "/") + ".class";
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(classPath);
        byte[] bytecode = stream.readAllBytes();

        //byte[] bytecode = aClass.toBytecode();
        HttpRequest.Builder builder = HttpRequest.newBuilder(new URI("http://localhost:"+port+"/api/idea/java_executor"));
        builder.setHeader("Content-Type", "text/json;charset=utf-8");
        byte[] encode = Base64.getEncoder().encode(bytecode);
        Map<String, String> map = new HashMap<>();
        map.put("eventId", "999999999");
        map.put("script", String.format("java_executor.execute(\"%s\", \"%s\")", aClass.getName(), new String(encode)));
        HttpRequest.Builder post = builder.POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(map)));
        HttpRequest request = post.build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        //builder.POST(HttpRequest.BodyPublishers.ofString());
        System.out.println(this.getClass());
    }
}
