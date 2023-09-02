package io.github.binarybeing.hotcat.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.EventContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author gn.binarybei
 * @date 2023/3/24
 * @note
 */
public class EmptyAction extends AnAction{
    private AnActionEvent event;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        EventContext.registerEvent(e, null);
        this.event = e;
    }

    public AnActionEvent getEvent() {
        return event;
    }
}
