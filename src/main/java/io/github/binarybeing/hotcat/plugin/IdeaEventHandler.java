package io.github.binarybeing.hotcat.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;

import java.util.concurrent.CompletableFuture;

/**
 * @author gn.binarybei
 * @date 2022/9/25
 * @note
 */
public interface IdeaEventHandler {
    void handle(String name, AnActionEvent event);

    void callback(Long actionId, String resp, String callbackEngine);
    CompletableFuture<String> actionCallback(Long eventId, String action, String data, String callbackPath);
    void handle(PluginEntity plugin, AnActionEvent event) throws Exception;

}
