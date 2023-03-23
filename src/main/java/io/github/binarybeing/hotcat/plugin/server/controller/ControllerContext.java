package io.github.binarybeing.hotcat.plugin.server.controller;

import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class ControllerContext {
    private static final Map<String, AbstractController> controllerMap = new HashMap<>();

    public static AbstractController get(String path) {
        return controllerMap.get(path);
    }

    public static void start() {
        Reflections reflections = new Reflections(AbstractController.class.getPackage().getName());
        reflections.getSubTypesOf(AbstractController.class).forEach((clazz) -> {
            try {
                AbstractController controller = clazz.newInstance();
                controllerMap.put(controller.path(), controller);
            } catch (InstantiationException | IllegalAccessException e) {
                LogUtils.addLog("ControllerContext" + e.getMessage());
            }
        });
    }

}
