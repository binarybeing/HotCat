package io.github.binarybeing.hotcat.plugin;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.keymap.impl.keyGestures.GestureActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author gn.binarybei
 * @date 2022/9/23
 * @note
 */
public class EventContext {
    private static Long eventId;

    private static ConcurrentLinkedDeque<Pair<Long, AnActionEvent>> eventQueue = new ConcurrentLinkedDeque<>();

    public static Long registerEvent(AnActionEvent event){
        eventId = System.currentTimeMillis() + RandomUtils.nextInt(1000, 9999);
        eventQueue.offer(Pair.of(eventId, event));
        if (eventQueue.size() > 100) {
            eventQueue.poll();
        }
        return eventId;
    }

    public static AnActionEvent getEvent(Long id){
        if (id == 999999999L) {
            Pair<Long, AnActionEvent> longAnActionEventPair = eventQueue.peekLast();
            if (longAnActionEventPair != null) {
                return longAnActionEventPair.getRight();
            }
            return null;
        }

        if (eventQueue.size() == 0) {
            return null;
        }
        for (Pair<Long, AnActionEvent> pair : eventQueue) {
            if (Objects.equals(pair.getLeft(), id)) {
                return pair.getRight();
            }
        }

        return null;
    }
}
