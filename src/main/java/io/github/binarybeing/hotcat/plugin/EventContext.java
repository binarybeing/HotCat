package io.github.binarybeing.hotcat.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;

import java.util.Objects;

/**
 * @author gn.binarybei
 * @date 2022/9/23
 * @note
 */
public class EventContext {
    private static Long eventId;
    private static AnActionEvent actionEvent;

    public static Long registerEvent(AnActionEvent event){
        actionEvent = event;
        eventId = System.currentTimeMillis();
        return eventId;
    }

    public static AnActionEvent getEvent(Long id){
        if (Objects.equals(id, eventId)) {
            return actionEvent;
        }
        if (id == 999999999L) {
            return actionEvent;
        }
        return null;
    }
}
