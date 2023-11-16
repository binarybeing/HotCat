package io.github.binarybeing.hotcat.plugin.config;

import io.github.binarybeing.hotcat.plugin.server.grpc.PanelRpcService;
import io.github.binarybeing.hotcat.plugin.server.grpc.ProjectRpcService;
import io.github.binarybeing.hotcat.plugin.server.grpc.interceptors.ApplicationRunnerInterceptor;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * @author gn.binarybei
 * @date 2022/7/26
 * @note
 */
public class GrpcServerConfig {
    private Server server;

    public static final GrpcServerConfig INSTANCE = new GrpcServerConfig();

    public GrpcServerConfig() {

    }

    public void start() throws IOException, InterruptedException {
        final int port = 17222;
        new Thread(() -> {
            try {
                ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4,
                        0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10)){
                    @Override
                    public void execute(@NotNull Runnable command) {
                        super.execute(() -> ApplicationRunnerUtils.runWithNoException(command));
                    }
                };


                ApplicationRunnerInterceptor interceptor = new ApplicationRunnerInterceptor();
                server = ServerBuilder.forPort(port)
                        .executor(executor)
                        .addService(ServerInterceptors.intercept(new PanelRpcService(), interceptor))
                        .addService(ServerInterceptors.intercept(new ProjectRpcService(), interceptor))
                        .build()
                        .start();

                Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
                server.awaitTermination();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }


    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

}
