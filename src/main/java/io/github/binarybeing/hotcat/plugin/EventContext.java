package io.github.binarybeing.hotcat.plugin;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.ActionCallback;
import io.github.binarybeing.hotcat.plugin.action.EmptyAction;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author gn.binarybei
 * @date 2022/9/23
 * @note
 */
public class EventContext {
    private static Long eventId;

    private static ConcurrentLinkedDeque<Pair<Long, AnActionEvent>> eventQueue = new ConcurrentLinkedDeque<>();
    private static ConcurrentHashMap<Long, PluginEntity> pluginMap = new ConcurrentHashMap<>();

    public static CompletableFuture<AnActionEvent> empyEvent() {
        CompletableFuture<AnActionEvent> future = new CompletableFuture<>();
        EventQueue.invokeLater(()->{
            EmptyAction emptyAction = new EmptyAction();
            try {
                ActionCallback callback = ActionManager.getInstance().tryToExecute(emptyAction, null, null, null, true);
                callback.waitFor(5 * 1000L);
                future.complete(emptyAction.getEvent());
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static Long registerEvent(AnActionEvent event, PluginEntity plugin){
        eventId = System.currentTimeMillis() + RandomUtils.nextInt(1000, 9999);
        eventQueue.offer(Pair.of(eventId, event));
        if (plugin != null) {
            pluginMap.put(eventId, plugin);
        }
        if (eventQueue.size() > 100) {
            Pair<Long, AnActionEvent> pair = eventQueue.poll();
            pluginMap.remove(pair.getLeft());
        }
        return eventId;
    }
    public static boolean hasEvent(){
        return eventQueue.size() > 0;
    }

    public static Long getEventId(AnActionEvent event){
        if (event == null) {
            return null;
        }
        for (Pair<Long, AnActionEvent> pair : eventQueue) {
            if (pair.getRight() == event) {
                return pair.getLeft();
            }
        }
        return null;
    }

    public static Optional<PluginEntity> getPluginEntity(Long eventId){
        if (eventId == null) {
            return Optional.empty();
        }
        for (Map.Entry<Long, PluginEntity> entry : pluginMap.entrySet()) {
            if (Objects.equals(eventId, entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    public static AnActionEvent getEvent(Long id) throws Exception{
        if (id == 999999999L) {
            AnActionEvent evt = ApplicationRunnerUtils.run(() -> {
                EmptyAction emptyAction = new EmptyAction();
                try {
                    ActionCallback callback = ActionManager.getInstance().tryToExecute(emptyAction, null, null, null, true);
                    callback.waitFor(5 * 1000L);
                    return emptyAction.getEvent();
                } catch (Throwable e) {
                    return null;
                }
            });
            if (evt != null) {
                return evt;
            }
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
