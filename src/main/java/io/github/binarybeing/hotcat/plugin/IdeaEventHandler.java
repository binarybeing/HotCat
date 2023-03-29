package io.github.binarybeing.hotcat.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;

import java.io.IOException;

/**
 * @author gn.binarybei
 * @date 2022/9/25
 * @note
 */
public interface IdeaEventHandler {

    void handle(String name, AnActionEvent event);
    void handle(PluginEntity plugin, AnActionEvent event) throws Exception;

}
