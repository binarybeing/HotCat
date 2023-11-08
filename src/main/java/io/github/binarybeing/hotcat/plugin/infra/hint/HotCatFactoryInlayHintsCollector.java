package io.github.binarybeing.hotcat.plugin.infra.hint;

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.codeInsight.hints.InlayPresentationFactory;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.MouseButton;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.binarybeing.hotcat.plugin.utils.LogUtils;
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
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class HotCatFactoryInlayHintsCollector extends FactoryInlayHintsCollector {

    private static Map<String, Pair<String, String>> listenerMap = new ConcurrentHashMap<>();
    static {
        listenerMap.put("__open_baidu", Pair.of("百度", "https://www.baidu.com"));
        listenerMap.put("__open_baibu", Pair.of("百度", null));
    }

    public static void register(String text, String hint, String jumpUrl){
        listenerMap.put(text, Pair.of(hint, jumpUrl));
    }

    public static void unregister(String text){
        listenerMap.remove(text);
    }

    public HotCatFactoryInlayHintsCollector(@NotNull Editor editor) {
        super(editor);
    }


    @Override
    public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
        Collection<PsiJavaToken> tokens = PsiTreeUtil.collectElementsOfType(psiElement, PsiJavaToken.class);

        boolean ans = false;
        for (PsiJavaToken token : tokens) {
            String text = token.getText();
            if (!text.startsWith("\"") || !text.endsWith("\"")) {
                continue;
            }
            text = text.substring(1, text.length() - 1);
            Pair<String, String> hintAndJump = listenerMap.get(text);
            if (hintAndJump == null || StringUtils.isBlank(hintAndJump.getKey())) {
                continue;
            }
            String hint = hintAndJump.getKey();

            if (hint.length() > 200) {
                hint = hint.substring(0, 200);
            }

            String shortHint = hint;
            if (shortHint.length() > 5) {
                shortHint = shortHint.substring(0, 6) + " ...";
            }
            int offset = token.getTextOffset() + text.length() + 2;
            PresentationFactory factory = getFactory();
            InlayPresentation presentation = factory.smallText(shortHint);
            presentation = invokeWrap(presentation, hint, factory);


            InlayPresentationFactory.Padding padding = new InlayPresentationFactory.Padding(5, 0, 6, 0);
            InlayPresentationFactory.RoundedCorners corners = new InlayPresentationFactory.RoundedCorners(4, 4);
            presentation = factory.container(presentation, padding, corners, Color.darkGray, 0);
            presentation = factory.withTooltip(hint, presentation);
            if (StringUtils.isNotBlank(hintAndJump.getRight())) {
                presentation = setHoverAndClick(presentation, editor, hintAndJump.getRight(), factory);
                presentation = factory.referenceOnHover(presentation, (event, point)->{
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        try {
                            Desktop.getDesktop().browse(new URI(hintAndJump.getRight()));
                        } catch (Exception e) {
                            LogUtils.addError(e, "open url error:"+ hintAndJump.getRight());
                        }
                    }
                });
            }

            inlayHintsSink.addInlineElement(offset, false, presentation);
            ans = true;
        }
        return ans;
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
            Method referenceMethod = factoryClass.getMethod("referenceOnHover", InlayPresentation.class, InlayPresentationFactory.ClickListener.class);

            return (InlayPresentation) referenceMethod.invoke(factory, presentation, (InlayPresentationFactory.ClickListener) (mouseEvent, point) -> {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI(jumpUrl));
                    } catch (Exception e) {
                        LogUtils.addError(e, "open url error:"+ jumpUrl);
                    }
                }
            });

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
