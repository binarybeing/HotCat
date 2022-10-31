package io.github.binarybeing.hotcat.plugin.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author gn.binarybei
 * @date 2022/9/23
 * @note
 */
public class LogUtils {
    private static final ConcurrentSkipListSet<Pair<Long,String>> logs;
    static {
        logs = new ConcurrentSkipListSet<>((o1,o2)->{
           return (int)(o1.getLeft() - o2.getLeft());
        });
    }
    public static void addLog(String log){
        logs.add(Pair.of(System.currentTimeMillis(), log));
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
                Pair<Long, String> pollLast = logs.pollLast();
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
