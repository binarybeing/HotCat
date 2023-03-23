package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import io.github.binarybeing.hotcat.plugin.utils.TerminalUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gn.binarybei
 * @date 2022/10/8
 * @note
 */
public class IdeaTerminalController extends BaseEventScriptController {
    @Override
    String path() {
        return "/api/idea/terminal";
    }
    @Override
    protected @NotNull Response handle(Request request, AnActionEvent event, String script) {
        Terminal terminal = new Terminal(event);
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("terminal", terminal);
        try {
            Object result = expression.evaluate(context);
            return Response.success(result);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }
    public final class Terminal {
        private String script;

        private String tab;

        private Map<String, String> conditions = new HashMap<>();

        private AnActionEvent event;
        public Terminal(AnActionEvent event) {
            this.event = event;
        }

        public String getScript() {
            return script;
        }

        public Terminal setScript(String script) {
            this.script = script;
            return this;
        }

        public String getTab() {
            return tab;
        }

        public Terminal setTab(String tab) {
            this.tab = tab;
            return this;
        }

        public Terminal addCondition(String condition, String command) {
            conditions.put(condition, command);
            return this;
        }

        public String start() throws Exception{
            try {
                Project project = event.getProject();
                return TerminalUtils.doCommandWithOutput(project, tab, script, conditions).toString();
            } catch (Exception e) {
                LogUtils.addError(e, "terminal error");
                throw e;
            }
        }

        public List<String> startAndGetResult() throws Exception {
            Project project = event.getProject();
            try {
                return TerminalUtils.doCommandWithOutput(project, tab, script, conditions);
            } catch (Exception e) {
                LogUtils.addLog("terminal error, " + e.getMessage());
                throw e;
            }
        }
        public List<String> getOutPut() {
            return TerminalUtils.getTerminalOutPut();
        }
    }
}
