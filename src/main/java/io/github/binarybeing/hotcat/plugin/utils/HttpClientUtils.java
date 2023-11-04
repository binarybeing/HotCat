package io.github.binarybeing.hotcat.plugin.utils;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private static HttpClient client;
    private static ExecutorService service = Executors.newFixedThreadPool(10);
    static {
        client = HttpClient.newHttpClient();
    }

    public static void post(String url, String body){

    }
    public static CompletableFuture<String> get(String url){
        return CompletableFuture.supplyAsync(() -> {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
            HttpRequest request = builder.GET().build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return response.body();
                } else {
                    LogUtils.addLog("request url:" + url + " error, body=" + response.body());
                }
            } catch (Exception e) {}
            return null;
        }, service);
    }
}
