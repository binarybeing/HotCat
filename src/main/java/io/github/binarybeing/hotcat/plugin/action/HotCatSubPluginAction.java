package io.github.binarybeing.hotcat.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.actionSystem.ShortcutSet;
import io.github.binarybeing.hotcat.plugin.IdeaEventHandler;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class HotCatSubPluginAction extends AnAction {
    private IdeaEventHandler handler;

    private PluginEntity plugin;

    public HotCatSubPluginAction(@Nullable PluginEntity plugin, IdeaEventHandler handler) {
        super(plugin.getName());
        this.plugin = plugin;
        this.handler = handler;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            handler.handle(plugin, e);
        }catch (Exception ex){
            LogUtils.addError(ex, "HotCatSubPluginAction.actionPerformed error");
        }
    }



}
