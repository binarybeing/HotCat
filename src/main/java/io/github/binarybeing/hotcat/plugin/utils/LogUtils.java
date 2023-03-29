package io.github.binarybeing.hotcat.plugin.utils;

import io.github.binarybeing.hotcat.plugin.server.ServerException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author gn.binarybei
 * @date 2022/9/23
 * @note
 */
public class LogUtils {
    private static final ConcurrentSkipListSet<Pair<Long,String>> logs;

    private static final ConcurrentSkipListMap<Long, List<String>> pluginLogs;
    static {
        logs = new ConcurrentSkipListSet<>((o1,o2)->{
           return (int)(o1.getLeft() - o2.getLeft());
        });
        pluginLogs = new ConcurrentSkipListMap<>();
    }

    public static void addEventLogs(Long eventId, String msg){
        if (eventId == null) {
            return;
        }
        List<String> list = pluginLogs.computeIfAbsent(eventId, k -> new ArrayList<>());
        if (list.size() > 200) {
            list.remove(0);
        }
        list.add(msg);

        if (pluginLogs.size() > 100) {
            pluginLogs.remove(pluginLogs.firstKey());
        }
    }

    public static List<String> getEventLogs(Long eventId){
        if (eventId == null) {
            return Collections.emptyList();
        }
        List<String> eventLogs = pluginLogs.get(eventId);
        if (eventLogs != null) {
            return eventLogs;
        }
        return Collections.emptyList();
    }
    public static void addLog(String log){
        logs.add(Pair.of(System.currentTimeMillis(), log));
        if(logs.size()>1000){
            try {
                logs.pollFirst();
            } catch (Exception e) {}
        }
    }
    public static void addError(Exception exp, String log){
        logs.add(Pair.of(System.currentTimeMillis(), log+" : "+exp.getMessage()));
        String message = ServerException.of(exp, log).getMessage();
        logs.add(Pair.of(System.currentTimeMillis(), message));
        if(logs.size()>1000){
            try {
                logs.pollFirst();
            } catch (Exception e) {}
        }
    }

    public static String[] getLogs(int i) {
        List<String> list = new ArrayList<>();
        while (i > 0 && !logs.isEmpty()) {
            try {
                Pair<Long, String> pollLast = logs.pollFirst();
                if(pollLast!=null){
                    list.add(pollLast.getRight());
                }
            } catch (Exception e) {
            }
            i--;
        }
        return list.toArray(new String[0]);
    }
}
