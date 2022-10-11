package io.github.binarybeing.hotcat.plugin;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.remoteServer.ServerType;
import io.github.binarybeing.hotcat.plugin.action.HotCatSubPluginAction;
import io.github.binarybeing.hotcat.plugin.action.InstallPluginAction;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.handlers.InvokePythonPluginHandler;
import io.github.binarybeing.hotcat.plugin.server.Server;
import io.github.binarybeing.hotcat.plugin.utils.PluginFileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class HotCatActionGroup extends ActionGroup {
    private static Server server;

    static {
        try {
            server = Server.INSTANCE;
            server.start();
        } catch (Exception e) {
            server = null;
        }
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        List<PluginEntity> pluginEntities = PluginFileUtils.listPlugin();

        List<AnAction> list = new ArrayList<>();
        IdeaEventHandler handler = new InvokePythonPluginHandler();
        pluginEntities.forEach((plugin) -> {
            list.add(new HotCatSubPluginAction(plugin, handler));
        });
        list.add(new InstallPluginAction());
        return list.toArray(new AnAction[0]);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(server != null);
    }
}
