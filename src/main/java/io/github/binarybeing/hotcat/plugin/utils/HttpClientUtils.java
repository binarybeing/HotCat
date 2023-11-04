package io.github.binarybeing.hotcat.plugin.utils;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class HttpClientUtils {
    //do post request
    private static HttpClient client = new HttpClient();
    private static ExecutorService service = Executors.newFixedThreadPool(10);


    public static void post(String url, String body){

    }
    public static CompletableFuture<String> get(String url){
        return CompletableFuture.supplyAsync(() -> {
            GetMethod method = new GetMethod(url);
            try {
                client.executeMethod(method);
                return new String(method.getResponseBody(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return null;
            }
        }, service);
    }
}
