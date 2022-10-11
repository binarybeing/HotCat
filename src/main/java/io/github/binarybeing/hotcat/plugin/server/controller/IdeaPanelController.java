package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.ComboBox;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.DialogUtils;
import io.github.binarybeing.hotcat.plugin.utils.JsonUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gn.binarybei
 * @date 2022/9/29
 * @note
 */
public class IdeaPanelController extends AbstractController {
    @Override
    String path() {
        return "/api/idea/panel";
    }
    @Override
    public @NotNull Response handle(Request request) {
        Long eventId = JsonUtils.readJsonLongValue(request.getJsonObject(), "eventId");
        String script = JsonUtils.readJsonStringValue(request.getJsonObject(), "script");
        AnActionEvent event = EventContext.getEvent(eventId);
        if (event == null) {
            return Response.error("event not found");
        }
        if (StringUtils.isEmpty(script)) {
            return Response.error("script is empty");
        }

        IdeaPanel panel = new IdeaPanel(event);
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("panel", panel);
        Object result = expression.evaluate(context);
        return Response.success(result);
    }

    public static class IdeaPanel {
        private final JPanel jPanel;

        private final AnActionEvent event;

        private String title;

        private final HashMap<String, JComponent> res = new HashMap<>();

        public IdeaPanel(AnActionEvent event) {
            this.jPanel = new JPanel();
            Dimension dimension = new Dimension();

            jPanel.setSize(dimension);
            this.jPanel.setLayout(new BoxLayout(this.jPanel, BoxLayout.Y_AXIS));
            this.event = event;
        }

        public IdeaPanel setTitle(String title) {
            this.title = title;
            return this;
        }

        public IdeaPanel showInput(String label, String filed, String defaultValue) {
            JLabel jLabel = new JLabel(label);
            jLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            jPanel.add(jLabel);
            if (StringUtils.isNotEmpty(filed)) {
                JTextField jTextField = new JTextField(15);
                jTextField.setText(defaultValue);
                jLabel.setLabelFor(jTextField);
                jPanel.add(jTextField);
                res.put(filed, jTextField);
            }
            return this;
        }
        public IdeaPanel showSelect(String label, String filed, String[] options, String defaultValue) {
            jPanel.add(new JLabel(label));
            ComboBox<String> box = new ComboBox<>(options);
            if (StringUtils.isNotEmpty(defaultValue)) {
                box.setSelectedItem(defaultValue);
            }
            jPanel.add(box);
            res.put(filed, box);
            return this;
        }


        public IdeaPanel showForm(String label, Map<String, String> formInfo){
            return this;
        }

        public Map<String, String> showAndGet(){
            boolean ok = DialogUtils.showPanelDialog(event, title, jPanel);
            Map<String,String> inputInfo = new HashMap<>(4);
            if (ok) {
                for (Map.Entry<String, JComponent> entry : res.entrySet()) {
                    String field = entry.getKey();
                    JComponent component = entry.getValue();
                    if (component instanceof JTextField) {
                        JTextField jTextField = (JTextField) component;
                        inputInfo.put(field, jTextField.getText());
                    } else if (component instanceof ComboBox) {
                        ComboBox<String> box = (ComboBox<String>) component;
                        inputInfo.put(field, box.getSelectedItem().toString());
                    }
                }
            }else{
                throw new RuntimeException("cancel");
            }
            return inputInfo;
        }
    }
}
