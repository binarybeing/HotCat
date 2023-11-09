package io.github.binarybeing.hotcat.plugin.infra.hint;

import com.google.gson.*;
import com.intellij.codeInsight.hints.FactoryInlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.MouseButton;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
import io.github.binarybeing.hotcat.plugin.utils.ScriptUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HotCatFactoryInlayHintsCollector extends FactoryInlayHintsCollector {

    private static Map<String, Pair<String, String>> listenerMap = new ConcurrentHashMap<>();

    private static Map<String, Map<String, List<Pair<String, String>>>> fileListenerMap = new ConcurrentHashMap<>();

    private static HashMap<PluginEntity, String> listeningPlugins = new HashMap<>();

    static {
        listenerMap.put("__open_baidu", Pair.of("百度", "https://www.baidu.com"));
        listenerMap.put("__open_baibu", Pair.of("百度", null));
    }

    public static void listenHintCollectEvent(PluginEntity plugin, String pythonListenerScriptPath) {
        listeningPlugins.put(plugin, pythonListenerScriptPath);
    }

    public HotCatFactoryInlayHintsCollector(@NotNull Editor editor) {
        super(editor);
    }


    private boolean collectByHintCollectListener(@NotNull PsiElement psiElement,
                                                 @NotNull Editor editor,
                                                 @NotNull InlayHintsSink inlayHintsSink){
        if (listeningPlugins.isEmpty()) {
            return false;
        }
        if (!(psiElement instanceof PsiJavaFile)) {
            return false;
        }
        PsiJavaFile javaFile = (PsiJavaFile) psiElement;
        Map<String, Object> params = new HashMap<>();

        List<Pair<String, Integer>> list = PsiTreeUtil.collectElementsOfType(psiElement, PsiJavaToken.class)
                .stream().map(t -> Pair.of(t.getText().trim(), t.getTextOffset()))
                .filter(s -> s.getLeft().startsWith("\""))
                .filter(s -> s.getLeft().endsWith("\""))
                .filter(s -> s.getLeft().length() < 20)
                .filter(s -> s.getLeft().length() > 0)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            return false;
        }
        Pair<String, Integer> pair = list.get(0);
        params.put("tokens", list);
        params.put("file", javaFile.getVirtualFile().getPath());
        CompletableFuture<Void> completableFuture = EventContext.empyEvent().thenAccept(e -> {

            ArrayList<CompletableFuture<String>> arrayList = new ArrayList<>();
            for (Map.Entry<PluginEntity, String> entry : listeningPlugins.entrySet()) {
                PluginEntity pluginEntity = entry.getKey();
                Long eventId = EventContext.registerEvent(e, pluginEntity);
                CompletableFuture<String> future = ScriptUtils.commonPython3Run(eventId, entry.getValue(), "hint_collect", new Gson().toJson(params), true);
                arrayList.add(future);
            }
            for (CompletableFuture<String> future : arrayList) {
                try {
                    String s = future.get(2, TimeUnit.SECONDS);
                    JsonElement element = JsonParser.parseString(s);
                    JsonArray array = element.getAsJsonArray();
                    for (JsonElement jsonElement : array) {
                        JsonObject object = jsonElement.getAsJsonObject();
                        String text = object.get("text").getAsString();
                        String hint = object.get("hint").getAsString();
                        String jump = null;
                        if (object.has("jump")) {
                            jump = object.get("jump").getAsString();
                        }

                        int offset = object.get("offset").getAsInt();
                        if (hint.length() > 200) {
                            hint = hint.substring(0, 200);
                        }
                        String shortHint = hint;
                        if (shortHint.length() > 5) {
                            shortHint = shortHint.substring(0, 6) + " ...";
                        }
                        offset = offset + text.length() + 2;
                        PresentationFactory factory = getFactory();
                        InlayPresentation presentation = factory.smallText(shortHint);
                        presentation = invokeWrap(presentation, hint, factory);
                        if (StringUtils.isNotBlank(jump)) {
                            presentation = setHoverAndClick(presentation, editor, jump, factory);
                        }

                        inlayHintsSink.addInlineElement(offset, false, presentation);
                    }

                } catch (Exception exception) {
                    LogUtils.addError(exception, "CompletableFuture get failed");
                }
            }


        });
        try {
            completableFuture.get(1, TimeUnit.SECONDS);
            return true;
        } catch (Exception exp) {
            return false;
        }

    }

    @Override
    public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
        return collectByHintCollectListener(psiElement, editor, inlayHintsSink);
    }

    /**
     * 部分版本方法不存在，不存在就略过(非核心。。。)
     * @return
     */
    private InlayPresentation invokeWrap(InlayPresentation presentation, String hint,
                                         PresentationFactory factory){
        try {
            Class<?> paddingClass = Class.forName("com.intellij.codeInsight.hints.InlayPresentationFactory$Padding");

            Constructor<?> constructor = paddingClass.getConstructor(int.class, int.class, int.class, int.class);
            Object padding = constructor.newInstance(5, 0, 6, 0);

            Class<?> roundedClass = Class.forName("com.intellij.codeInsight.hints.InlayPresentationFactory$RoundedCorners");
            Constructor<?> roundedClassConstructor = roundedClass.getConstructor(int.class, int.class);
            Object roundCorner = roundedClassConstructor.newInstance(4, 4);

            Class<? extends PresentationFactory> factoryClass = factory.getClass();
            Method container = factoryClass.getMethod("container", InlayPresentation.class, paddingClass, roundedClass, Color.class, float.class);
            Object res = container.invoke(factory, presentation, padding, roundCorner, Color.ORANGE, 0);



            Method tooltips = factoryClass.getMethod("withTooltip", String.class, InlayPresentation.class);
            return (InlayPresentation) tooltips.invoke(factory, hint, res);
        } catch (Exception e) {
            return presentation;
        }
    }

    private InlayPresentation setHoverAndClick(InlayPresentation presentation, Editor editor, String jumpUrl,
                                               PresentationFactory factory){
        try {
            Class<? extends PresentationFactory> factoryClass = factory.getClass();
            Class<?> listenerClass = Class.forName("com.intellij.codeInsight.hints.InlayPresentationFactory$ClickListener");
            Method referenceMethod = factoryClass.getMethod("referenceOnHover", InlayPresentation.class, listenerClass);
            Object onClickListener = Proxy.newProxyInstance(factory.getClass().getClassLoader(), new Class[]{listenerClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (Objects.equals(method.getName(), "onClick")) {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            try {
                                Desktop.getDesktop().browse(new URI(jumpUrl));
                            } catch (Exception e) {
                                LogUtils.addError(e, "open url error:" + jumpUrl);
                            }
                        }
                    }
                    return null;
                }
            });

            return (InlayPresentation) referenceMethod.invoke(factory, presentation, onClickListener);

        } catch (Exception e) {
            return setHoverClickWithOutReference(presentation, editor, jumpUrl, factory);
        }
    }

    /**
     * 非核心方法，使用反射提高版本兼容性
     * @return
     */
    private InlayPresentation setHoverClickWithOutReference(InlayPresentation presentation, Editor editor, String jumpUrl,
                                       PresentationFactory factory){
        try {
            Class<?> hoverClass = Class.forName("com.intellij.codeInsight.hints.InlayPresentationFactory$HoverListener");
            Object hoverListener = Proxy.newProxyInstance(factory.getClass().getClassLoader(), new Class[]{hoverClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (!(editor instanceof EditorImpl)) {
                        return null;
                    }
                    EditorImpl editor1 = (EditorImpl) editor;
                    if (Objects.equals(method.getName(), "onHover")) {
                        Cursor handCursor = Cursor.getPredefinedCursor(12);
                        editor1.setCustomCursor(factory, handCursor);
                    }
                    if (Objects.equals(method.getName(), "onHoverFinished")) {
                        editor1.setCustomCursor(factory, null);
                    }
                    return null;
                }
            });

            Method method = factory.getClass().getMethod("onHover", InlayPresentation.class, hoverClass);
            InlayPresentation inlayPresentation = (InlayPresentation) method.invoke(factory, presentation, hoverListener);

            return factory.onClick(inlayPresentation, MouseButton.Left, new Function2<MouseEvent, Point, Unit>() {
                @Override
                public Unit invoke(MouseEvent event, Point point) {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        try {
                            Desktop.getDesktop().browse(new URI(jumpUrl));
                        } catch (Exception e) {
                            LogUtils.addError(e, "open url error:"+ jumpUrl);
                        }
                    }
                    return Unit.INSTANCE;
                }
            });
        } catch (Exception e) {
            return presentation;
        }
    }
}
