package io.github.binarybeing.hotcat.plugin.infra.completion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompletionReferenceContext {

    private static final Map<String, String> map = new ConcurrentHashMap<>();

    private static CompletionReferenceContext instance = new CompletionReferenceContext();

    public static CompletionReferenceContext getInstance(){
        return instance;
    }



}
