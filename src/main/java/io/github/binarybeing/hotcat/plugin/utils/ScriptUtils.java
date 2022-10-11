package io.github.binarybeing.hotcat.plugin.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.internal.Engine;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class ScriptUtils {
    private static final JexlEngine jexlEngine = new Engine();

    public static Object runJava(AnActionEvent event, String cmd){
        try {
            JexlExpression expression = jexlEngine.createExpression(cmd);
            JexlContext context = new MapContext();
            context.set("event", event);
            return expression.evaluate(context);
        } catch (Exception e) {
            LogUtils.addLog("script error: " + e.getMessage() + " script=" + cmd + " "+ e.getStackTrace()[0]);
        }
        return null;
    }

}
