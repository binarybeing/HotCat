package io.github.binarybeing.hotcat.plugin;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
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
            HotCatSubPluginAction pluginAction = new HotCatSubPluginAction(plugin, handler);
            AnAction action = ActionManager.getInstance().getAction(plugin.getName());
            if (action == null) {
                ActionManager.getInstance().registerAction(plugin.getName(), pluginAction);
            }
            list.add(pluginAction);
        });
        list.add(new InstallPluginAction());
        super.setSearchable(true);

        return list.toArray(new AnAction[0]);
        //return null;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(server != null);
    }
}
