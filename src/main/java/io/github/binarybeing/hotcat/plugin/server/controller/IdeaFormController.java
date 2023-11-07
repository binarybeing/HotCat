package io.github.binarybeing.hotcat.plugin.server.controller;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.ComboBox;
import io.github.binarybeing.hotcat.plugin.server.dto.Request;
import io.github.binarybeing.hotcat.plugin.server.dto.Response;
import io.github.binarybeing.hotcat.plugin.utils.DialogUtils;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 静态流式布局 表单窗体
 */
public class IdeaFormController extends BaseEventScriptController {

    @Override
    public String path() {
        return "/api/idea/form";
    }

    @Override
    protected @NotNull Response handle(Request request, AnActionEvent event, String script) {
        FormPanel panel = new FormPanel(event);
        JexlExpression expression = super.jexlEngine.createExpression(script);
        MapContext context = new MapContext();
        context.set("panel", panel);
        Object result = expression.evaluate(context);
        return Response.success(result);
    }

    public static class FormPanel {

        private final JPanel jPanel;
        private final AnActionEvent event;
        private String title;
        private final List<ComponentGroup> componentGroupList;

        public FormPanel(AnActionEvent event) {
            this.jPanel = new JPanel();
            jPanel.setSize(new Dimension());
            this.event = event;
            this.componentGroupList = new ArrayList<>();
        }

        public GroupBuilder newGroup() {
            return new GroupBuilder(this);
        }

        public FormPanel addComponentGroup(ComponentGroup componentGroup) {
            componentGroupList.add(componentGroup);
            return this;
        }

        public FormPanel setTitle(String title) {
            this.title = title;
            return this;
        }

        public void showMsg(String title, String info, String type) {
            if ("error".equals(type)) {
                DialogUtils.showError(title, info);
            } else {
                DialogUtils.showMsg(title, info);
            }
        }

        public Map<String, String> showAndGet() {
            defineLayout();
            boolean ok = DialogUtils.showPanelDialog(event, title, jPanel);
            if (!ok) {
                return Collections.emptyMap();
            }

            Map<String, JComponent> allField2Component = new HashMap<>();
            componentGroupList.stream()
                    .map(ComponentGroup::field2Component)
                    .forEach(allField2Component::putAll);
            Map<String, String> inputInfo = new HashMap<>();
            for (Map.Entry<String, JComponent> entry : allField2Component.entrySet()) {
                String field = entry.getKey();
                JComponent component = entry.getValue();
                if (component instanceof JPasswordField) {
                    JPasswordField pwdField = (JPasswordField) component;
                    inputInfo.put(field, new String(pwdField.getPassword()));
                } else if (component instanceof JTextField) {
                    JTextField jTextField = (JTextField) component;
                    inputInfo.put(field, jTextField.getText());
                } else if (component instanceof JCheckBox) {
                    JCheckBox checkbox = (JCheckBox) component;
                    inputInfo.put(field, String.valueOf(checkbox.isSelected()));
                } else if (component instanceof RadioBtnPanel) {
                    RadioBtnPanel radioBtns = (RadioBtnPanel) component;
                    radioBtns.getChildren().stream()
                            .filter(JRadioButton::isSelected)
                            .findFirst()
                            .map(JRadioButton::getText)
                            .ifPresent(value -> inputInfo.put(field, value));
                } else if (component instanceof ComboBox) {
                    ComboBox<?> box = (ComboBox<?>) component;
                    Optional.ofNullable(box.getSelectedItem())
                            .map(Object::toString)
                            .ifPresent(value -> inputInfo.put(field, value));
                }
            }
            return inputInfo;
        }

        private void defineLayout() {
            GroupLayout layout = new GroupLayout(jPanel);
            jPanel.setLayout(layout);

            // 垂直连续组 组织从上到下每一行中的组件
            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
            componentGroupList.forEach(componentGroup -> {
                GroupLayout.ParallelGroup pGroup = layout.createParallelGroup();
                componentGroup.components().forEach(pGroup::addComponent);
                vGroup.addGroup(pGroup);
            });
            layout.setVerticalGroup(vGroup);

            // 水平连续组 组织从左到右每一列中的组件
            GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
            int maxColumnCount = componentGroupList.stream()
                    .map(ComponentGroup::components)
                    .map(List::size)
                    .mapToInt(t -> t)
                    .max().orElse(0);
            for (int i = 0; i < maxColumnCount; i++) {
                GroupLayout.ParallelGroup pGroup = layout.createParallelGroup();
                for (ComponentGroup componentGroup : componentGroupList) {
                    List<JComponent> components = componentGroup.components();
                    if (i < components.size()) {
                        pGroup.addComponent(components.get(i));
                    }
                }
                hGroup.addGroup(pGroup);
            }
            layout.setHorizontalGroup(hGroup);
        }
    }

    public static class GroupBuilder {

        private static final int INPUT_DEFAULT_LENGTH = 30;
        private final FormPanel parent;
        private final ComponentGroup componentGroup = new ComponentGroup();

        public GroupBuilder(FormPanel parent) {
            this.parent = parent;
        }

        public GroupBuilder addInput(String label, String field, String defaultValue) {
            JLabel jLabel = new JLabel(label);
            PanelBasedJComponent<JLabel> labelPanel = new PanelBasedJComponent<>(jLabel);
            componentGroup.addComponent(labelPanel);

            if (StringUtils.isNotEmpty(field)) {
                JTextField jTextField = new JTextField(INPUT_DEFAULT_LENGTH);
                jTextField.setText(defaultValue);
                jLabel.setLabelFor(jTextField);
                componentGroup.addComponentWithField(field, jTextField);
            }
            return this;
        }

        public GroupBuilder addPwdInput(String label, String field, String defaultValue) {
            JLabel jLabel = new JLabel(label);
            PanelBasedJComponent<JLabel> labelPanel = new PanelBasedJComponent<>(jLabel);
            componentGroup.addComponent(labelPanel);

            if (StringUtils.isNotEmpty(field)) {
                JPasswordField pwdField = new JPasswordField(INPUT_DEFAULT_LENGTH);
                pwdField.setText(defaultValue);
                jLabel.setLabelFor(pwdField);
                componentGroup.addComponentWithField(field, pwdField);
            }
            return this;
        }

        public GroupBuilder addChecks(String label, String[] optionArr) {
            JLabel jLabel = new JLabel(label);
            PanelBasedJComponent<JLabel> labelPanel = new PanelBasedJComponent<>(jLabel);
            componentGroup.addComponent(labelPanel);

            JPanel container = new JPanel();
            container.setLayout(new FlowLayout(FlowLayout.LEADING));
            componentGroup.addComponent(container);
            List<String> optionList = Arrays.asList(optionArr);
            optionList.forEach(option -> {
                JCheckBox jCheckBox = new JCheckBox(option);
                container.add(jCheckBox);
                componentGroup.addChildComponentWithField(option, jCheckBox);
            });
            labelPanel.getChild().setLabelFor(container);
            return this;
        }

        public GroupBuilder addRadios(String label, String field, String[] optionArr) {
            JLabel jLabel = new JLabel(label);
            PanelBasedJComponent<JLabel> labelPanel = new PanelBasedJComponent<>(jLabel);
            componentGroup.addComponent(labelPanel);

            RadioBtnPanel radioBtns = new RadioBtnPanel();
            componentGroup.addComponentWithField(field, radioBtns);
            ButtonGroup buttonGroup = new ButtonGroup();
            Arrays.asList(optionArr).forEach(option -> {
                JRadioButton radioButton = new JRadioButton(option);
                buttonGroup.add(radioButton);
                radioBtns.addChild(radioButton);
                radioBtns.add(radioButton);
            });
            labelPanel.getChild().setLabelFor(radioBtns);
            return this;
        }

        public GroupBuilder addSelect(String label, String field, String[] options, String defaultValue) {
            JLabel jLabel = new JLabel(label);
            PanelBasedJComponent<JLabel> labelPanel = new PanelBasedJComponent<>(jLabel);
            componentGroup.addComponent(labelPanel);

            ComboBox<String> box = new ComboBox<>(options);
            if (StringUtils.isNotEmpty(defaultValue)) {
                box.setSelectedItem(defaultValue);
                componentGroup.addComponentWithField(field, box);
            }
            return this;
        }

        public FormPanel compose() {
            parent.addComponentGroup(componentGroup);
            return parent;
        }
    }

    public static class ComponentGroup {

        private final List<JComponent> components = new ArrayList<>();
        private final Map<String, JComponent> field2Component = new LinkedHashMap<>();

        ComponentGroup() {
        }

        public void addComponent(JComponent component) {
            components.add(component);
        }

        public void addComponentWithField(String field, JComponent component) {
            field2Component.put(field, component);
            addComponent(component);
        }

        public void addChildComponentWithField(String field, JComponent component) {
            field2Component.put(field, component);
        }

        public List<JComponent> components() {
            return Collections.unmodifiableList(components);
        }

        public Map<String, JComponent> field2Component() {
            return Collections.unmodifiableMap(field2Component);
        }
    }

    private static class PanelBasedJComponent<T extends JComponent> extends JPanel {

        private final T child;

        public PanelBasedJComponent(T child) {
            this.child = child;
            this.setLayout(new FlowLayout(FlowLayout.LEADING));
            this.add(child);
        }

        public T getChild() {
            return child;
        }
    }

    private static class RadioBtnPanel extends JPanel {

        private final List<JRadioButton> children = new ArrayList<>();

        public RadioBtnPanel() {
            this.setLayout(new FlowLayout(FlowLayout.LEADING));
        }

        public RadioBtnPanel addChild(JRadioButton child) {
            children.add(child);
            return this;
        }

        public List<JRadioButton> getChildren() {
            return children;
        }
    }
}
