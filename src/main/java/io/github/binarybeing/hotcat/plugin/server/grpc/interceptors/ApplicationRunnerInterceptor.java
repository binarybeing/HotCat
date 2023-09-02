package io.github.binarybeing.hotcat.plugin.server.grpc.interceptors;

import io.grpc.*;

public class ApplicationRunnerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {
            @Override
            public void onHalfClose() {
                super.onHalfClose();
            }

            @Override
            public void onMessage(ReqT message) {
                super.onMessage(message);
            }
        };
    }

}
