package io.github.binarybeing.hotcat.plugin;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.ActionCallback;
import io.github.binarybeing.hotcat.plugin.action.EmptyAction;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

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
    public static boolean hasEvent(){
        return eventQueue.size() > 0;
    }

    public static AnActionEvent getEvent(Long id){
        if (id == 999999999L) {
            ApplicationRunnerUtils.run(() ->{
                EmptyAction emptyAction = new EmptyAction();
                ActionCallback callback = ActionManager.getInstance().tryToExecute(emptyAction, null, null, null, true);
                callback.wait();
                return emptyAction.getEvent();
            });
            if(eventQueue.size() > 0){
                return eventQueue.getLast().getRight();
            }
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
