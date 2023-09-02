package io.github.binarybeing.hotcat.plugin.server.dto;

import java.util.concurrent.Future;

public class FutureResponse<V> extends Response{

    @SuppressWarnings("unchecked")
    public Future<V> getData(){
        return (Future<V>) super.getData();
    }

    public void setData(Future<V> data) {
        super.setData(data);
    }
}
