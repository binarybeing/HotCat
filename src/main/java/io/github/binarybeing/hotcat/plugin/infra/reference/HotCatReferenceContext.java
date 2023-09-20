package io.github.binarybeing.hotcat.plugin.infra.reference;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HotCatReferenceContext {

    private static final Map<String, String> map = new ConcurrentHashMap<>();

    private static HotCatReferenceContext instance = new HotCatReferenceContext();

    public static HotCatReferenceContext getInstance(){
        return instance;
    }



}
