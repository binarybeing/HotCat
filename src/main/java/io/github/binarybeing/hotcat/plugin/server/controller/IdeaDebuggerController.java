package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.remoteServer.configuration.RemoteServer;
import com.intellij.remoteServer.configuration.RemoteServersManager;
import com.intellij.remoteServer.impl.configuration.RemoteServerImpl;
import com.intellij.remoteServer.runtime.deployment.debug.JavaDebugConnectionData;
import com.intellij.remoteServer.runtime.deployment.debug.JavaDebuggerLauncher;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Semaphore;

/**
 * @author gn.binarybei
 * @date 2022/9/28
 * @note
 */
public class IdeaDebuggerController extends BaseEventScriptController{

    @Override
    String path() {
        return "/api/idea/debugger";
    }

    @Override
    protected Response handle(Request request, AnActionEvent event, String script) {
        Debugger debugger = new Debugger(event);
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("debugger", debugger);
        try {
            Object result = expression.evaluate(context);
            return Response.success(result);
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    public static class Debugger {
        private String host;
        private int port;

        private String desc;

        private AnActionEvent event;

        public Debugger(AnActionEvent event) {
            this.event = event;
        }

        public String getHost() {
            return host;
        }

        public Debugger setHost(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return port;
        }

        public Debugger setPort(int port) {
            this.port = port;
            return this;
        }

        public Debugger setDesc(String desc) {
            this.desc = desc;
            return this;
        }

        public String start() throws Exception{
            Project project = event.getProject();
            if (project == null) {
                throw new RuntimeException("project is null");
            }
            RunManager runManager = RunManager.getInstance(project);

            Executor executor = DefaultDebugExecutor.getDebugExecutorInstance();

            ProgramRunner<?> runner = ProgramRunner.findRunnerById(executor.getId());
            if (runner == null) {
                throw new RuntimeException("runner is null");
            }
            ConfigurationType debugType = null;
            for (ConfigurationType type : ConfigurationTypeBase.CONFIGURATION_TYPE_EP.getExtensionList()) {
                if ("Remote JVM Debug".equals(type.getDisplayName())
                     || "Remote".equals(type.getDisplayName())) {
                    debugType = type;
                    break;
                }
            }
            if (debugType == null) {
                throw new RuntimeException("debugType is null");
            }
            ConfigurationFactory factory = debugType.getConfigurationFactories()[0];
            String debugName = StringUtils.isEmpty(desc) ? "debug" : desc;
            RunnerAndConfigurationSettings setting = runManager.createConfiguration(debugName, factory);

            RunConfiguration configuration = setting.getConfiguration();
            ExecutionEnvironment executionEnvironment = new ExecutionEnvironmentBuilder(project, executor).runProfile(configuration).build();
            RemoteServer<?> remoteServer = null;
            RemoteServersManager remoteServersManager = RemoteServersManager.getInstance();
            //ProgramRunnerUtil.executeConfiguration(executionEnvironment, true, true);

            for (RemoteServer<?> server : remoteServersManager.getServers()) {
                if (server instanceof RemoteServerImpl) {
                    remoteServer = server;
                }
            }
            if (remoteServer == null) {
                //startDebugSession 实际不会用到
                remoteServer = new RemoteServerImpl<>("", null, null);
            }
            final RemoteServer<?> server = remoteServer;
            JavaDebugConnectionData data = new JavaDebugConnectionData(getHost(), getPort());
            JavaDebuggerLauncher.getInstance().startDebugSession(data, executionEnvironment , server);
            return "debugger started";
        }
    }

}
