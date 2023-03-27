package io.github.binarybeing.hotcat.plugin;

import com.google.common.primitives.Bytes;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gn.binarybei
 * @date 2023/3/24
 * @note
 */
public abstract class BaseTest {
    private int port = 17123;

    protected abstract Class<?>  getTestClass();

    @Test
    public void test() throws Exception{
        Class<?> aClass = getTestClass();
        if(aClass == null) {
            System.out.println("set test.open=true to open test");
            return;
        }
        String classPath = aClass.getName().replace(".", "/") + ".class";
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(classPath);

        byte[] bytes = new byte[1024];
        int len;
        byte[] bytecode = new byte[0];
        while ((len = stream.read(bytes))>=0){
            bytecode = Bytes.concat(bytecode, Arrays.copyOfRange(bytes, 0 , len));
        }
        RequestConfig config = RequestConfig.custom().setConnectTimeout(3000).setSocketTimeout(3000).build();



        HttpPost httpPost = new HttpPost(new URI("http://localhost:" + port + "/api/idea/java_executor"));

        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");

        byte[] encode = Base64.getEncoder().encode(bytecode);
        Map<String, String> map = new HashMap<>();
        map.put("eventId", "999999999");
        map.put("script", String.format("java_executor.execute(\"%s\", \"%s\")", aClass.getName(), new String(encode)));
        StringEntity entity = new StringEntity(new Gson().toJson(map), "utf-8");

        httpPost.setEntity(entity);
//        httpPost.setConfig(config);

        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse httpResponse = client.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        reader.close();
        httpResponse.close();
        client.close();
    }
}
