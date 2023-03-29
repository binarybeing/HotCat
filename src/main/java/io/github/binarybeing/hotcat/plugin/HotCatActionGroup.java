package io.github.binarybeing.hotcat.plugin;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.action.HotCatSubPluginAction;
import io.github.binarybeing.hotcat.plugin.action.InstallPluginAction;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.handlers.InvokePythonPluginHandler;
import io.github.binarybeing.hotcat.plugin.server.Server;
import io.github.binarybeing.hotcat.plugin.utils.DialogUtils;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import io.github.binarybeing.hotcat.plugin.utils.PluginFileUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class HotCatActionGroup extends ActionGroup {
    private static Server server;
    private static boolean setShellRunner = false;



    static {
        try {
            server = Server.INSTANCE;
            server.start();
        } catch (Exception e) {
            server = null;
        }
    }

    public HotCatActionGroup() {


    }

    @Override
    public @NotNull AnAction[] getChildren(@Nullable AnActionEvent e) {
        List<PluginEntity> pluginEntities = PluginFileUtils.listPlugin();

        IdeaEventHandler handler = new InvokePythonPluginHandler();
        List<AnAction> list = new ArrayList<>(getPlugins(pluginEntities, handler, "HotCat"));
        list.add(new InstallPluginAction());
        super.setSearchable(true);
        return list.toArray(new AnAction[0]);
        //return null;
    }

    private List<AnAction> getPlugins(List<PluginEntity> plugins, IdeaEventHandler handler, String groupName){
        LogUtils.addLog("setShellRunner: " + (setShellRunner || setShellRunner()));
        if (CollectionUtils.isEmpty(plugins)) {
            return Collections.emptyList();
        }
        List<AnAction> res = new ArrayList<>();
        for (PluginEntity plugin : plugins) {
            if (CollectionUtils.isEmpty(plugin.getSubMenus())) {
                HotCatSubPluginAction pluginAction = new HotCatSubPluginAction(plugin, handler);
                AnAction action = ActionManager.getInstance().getAction(groupName+"/" + plugin.getName());
                if (action == null) {
                    ActionManager.getInstance().registerAction(groupName+"/" + plugin.getName(), pluginAction);
                }
                res.add(pluginAction);
            } else {
                List<PluginEntity> subMenus = plugin.getSubMenus();
                List<AnAction> subActions = getPlugins(subMenus, handler, groupName + "/" + plugin.getName());
                ActionGroup group = new ActionGroup(plugin.getName(), true){
                    @Override
                    public @NotNull AnAction [] getChildren(@Nullable AnActionEvent e) {
                        return subActions.toArray(new AnAction[0]);
                    }
                };
                AnAction action = ActionManager.getInstance().getAction(groupName+"/" + plugin.getName());
                if (action == null) {
                    ActionManager.getInstance().registerAction(groupName+"/" + plugin.getName(), group);
                }
                res.add(group);
            }
        }
        return res;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(server != null);
    }

    private static boolean setShellRunner(){
        String cpFileName = PluginFileUtils.getPluginDirName()+"/shell_runner.sh";
        File file = new File(cpFileName);
        if (file.exists()) {
            file.delete();
        }
        try (InputStream stream = HotCatActionGroup.class.getClassLoader().getResourceAsStream("shell_runner.sh");
             FileOutputStream fileOutputStream = new FileOutputStream(cpFileName)) {
            assert stream != null;
            byte[] bytes = new byte[1024];
            int len;
            while ((len = stream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len);
                fileOutputStream.flush();
            }
            return setShellRunner = true;
        } catch (Exception e) {
            DialogUtils.showError("init ShellRunner error", e.getMessage());
            return false;
        }
    }
}
