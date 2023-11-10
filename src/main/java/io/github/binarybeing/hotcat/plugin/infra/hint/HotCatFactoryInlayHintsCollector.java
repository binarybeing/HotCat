package io.github.binarybeing.hotcat.plugin.infra.hint;

import com.google.gson.*;
import com.intellij.codeInsight.hints.FactoryInlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.MouseButton;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.entity.PluginEntity;
import io.github.binarybeing.hotcat.plugin.utils.ApplicationRunnerUtils;
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
import java.util.stream.Collectors;

public class HotCatFactoryInlayHintsCollector extends FactoryInlayHintsCollector {

    private static Map<String, Pair<String, String>> listenerMap = new ConcurrentHashMap<>();

    private static Map<String, Map<String, List<Pair<String, String>>>> fileListenerMap = new ConcurrentHashMap<>();

    private static HashMap<PluginEntity, String> listeningPlugins = new HashMap<>();
    private static Map<PsiJavaFile, Map<String, Set<SimpleHint>>> hintCaches = new ConcurrentHashMap<>();

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
                .map(p-> Pair.of(p.getLeft().replaceAll("\"", ""), p.getRight()))
                .filter(s -> s.getLeft().length() < 40)
                .filter(s -> s.getLeft().length() > 0)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            return false;
        }

        params.put("tokens", list);
        params.put("file", javaFile.getVirtualFile().getPath());
        EventContext.empyEvent().thenAccept(e -> {
            for (Map.Entry<PluginEntity, String> entry : listeningPlugins.entrySet()) {
                PluginEntity pluginEntity = entry.getKey();
                Long eventId = EventContext.registerEvent(e, pluginEntity);
                CompletableFuture<String> future = ScriptUtils.commonPython3Run(eventId, entry.getValue(), "hint_collect", new Gson().toJson(params), true);
                future.thenAccept(s -> {
                    JsonElement element = JsonParser.parseString(s);
                    JsonArray array = element.getAsJsonArray();
                    Map<String, Set<SimpleHint>> map = new HashMap<>();
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
                        if (shortHint.length() > 10) {
                            shortHint = shortHint.substring(0, 10) + " ...";
                        }
                        if (StringUtils.isAnyEmpty(text, hint, shortHint)) {
                            continue;
                        }
                        SimpleHint simpleHint = new SimpleHint();
                        simpleHint.text = text;
                        simpleHint.hint = hint;
                        simpleHint.shortHint = shortHint;
                        simpleHint.offset = offset;
                        simpleHint.jump = jump;
                        simpleHint.listener = pluginEntity.getFile().getAbsolutePath();
                        Set<SimpleHint> simpleHints = map.computeIfAbsent(text, k -> {
                            return new HashSet<>();
                        });
                        simpleHints.add(simpleHint);
                    }

                    boolean diff = false;
                    Map<String, Set<SimpleHint>> tokensHints = hintCaches.computeIfAbsent(javaFile, k -> {
                        return new ConcurrentHashMap<>();
                    });
                    Set<String> tokensSet = new ArrayList<>(list).stream().map(p -> p.getLeft()).collect(Collectors.toSet());
                    for (Map.Entry<String, Set<SimpleHint>> listEntry : map.entrySet()) {
                        String text = listEntry.getKey();
                        tokensSet.remove(text);
                        Set<SimpleHint> hintsToAdd = listEntry.getValue();

                        Set<SimpleHint> hints = tokensHints.computeIfAbsent(text, k -> {
                            return new HashSet<>();
                        });

                        Set<SimpleHint> hintsToRemove = hints.stream().filter(h -> Objects.equals(h.listener, pluginEntity.getFile().getAbsolutePath()))
                                .collect(Collectors.toSet());

                        boolean noChange = Objects.equals(hintsToAdd, hintsToRemove);
                        if (!noChange) {
                            hints.removeAll(hintsToRemove);
                            hints.addAll(hintsToAdd);
                        }
                        diff = diff || !noChange;

                    }
                    diff = diff || !tokensSet.isEmpty();
                    for (String noMentioned : tokensSet) {
                        Set<SimpleHint> hints = tokensHints.get(noMentioned);
                        Set<SimpleHint> collect = hints.stream().filter(h -> Objects.equals(h.listener, pluginEntity.getFile().getAbsolutePath())).collect(Collectors.toSet());
                        hints.removeAll(collect);
                    }
                    if (diff) {
                        Runnable runner = () -> {
                            FileEditorManager instance = FileEditorManager.getInstance(javaFile.getProject());
                            FileEditor selectedEditor = instance.getSelectedEditor();
                            VirtualFile file = selectedEditor.getFile();
                            instance.closeFile(file);
                            instance.openFile(file, true);
                        };
                        ApplicationManager.getApplication().invokeLater(runner, ModalityState.defaultModalityState());

                    }

                });
            }
        });
        Map<String, Set<SimpleHint>> tokensHints = hintCaches.computeIfAbsent(javaFile, k -> {
            return new ConcurrentHashMap<>();
        });
        for (Pair<String, Integer> token : list) {
            Set<SimpleHint> hints = tokensHints.get(token.getLeft());
            if (hints != null && hints.size() > 0) {

                for (SimpleHint hintObj : hints) {
                    PresentationFactory factory = getFactory();
                    InlayPresentation presentation = factory.smallText(hintObj.shortHint);
                    presentation = invokeWrap(presentation, hintObj.hint, factory);
                    if (StringUtils.isNotBlank(hintObj.jump)) {
                        presentation = setHoverAndClick(presentation, editor, hintObj.jump, factory);
                    }
                    inlayHintsSink.addInlineElement(token.getRight()+ token.getLeft().length() + 2, true, presentation);
                            
                }
            }
        }
        return true;
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

    class SimpleHint{

        private String listener;
        private String text;
        private String hint;

        private String shortHint;
        private int offset;
        private String jump;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public String getJump() {
            return jump;
        }

        public void setJump(String jump) {
            this.jump = jump;
        }

        public String getListener() {
            return listener;
        }

        public void setListener(String listener) {
            this.listener = listener;
        }

        public String getShortHint() {
            return shortHint;
        }

        public void setShortHint(String shortHint) {
            this.shortHint = shortHint;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleHint that = (SimpleHint) o;
            return text.equals(that.text) && hint.equals(that.hint) && jump.equals(that.jump);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, hint, jump);
        }
    }
}
