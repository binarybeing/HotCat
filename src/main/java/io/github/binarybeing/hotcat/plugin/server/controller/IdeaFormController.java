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

    public static class FormPanel extends ComponentAddableBuilder {

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

        @Override
        public FormPanel addInput(String label, String field, String defaultValue) {
            newGroup().addInput(label, field, defaultValue).compose();
            return this;
        }

        @Override
        public FormPanel addPwdInput(String label, String field, String defaultValue) {
            newGroup().addPwdInput(label, field, defaultValue).compose();
            return this;
        }

        @Override
        public FormPanel addChecks(String label, String[] options) {
            newGroup().addChecks(label, options).compose();
            return this;
        }

        @Override
        public FormPanel addCheck(String label, String field, String text, boolean checked) {
            newGroup().addCheck(label, field, text, checked).compose();
            return this;
        }

        @Override
        public FormPanel addRadios(String label, String field, String[] options, int selectedIndex) {
            newGroup().addRadios(label, field, options, selectedIndex).compose();
            return this;
        }

        @Override
        public FormPanel addSelect(String label, String field, String[] options, String defaultValue) {
            newGroup().addSelect(label, field, options, defaultValue).compose();
            return this;
        }
    }

    public static class GroupBuilder extends ComponentAddableBuilder {

        private static final int INPUT_DEFAULT_LENGTH = 30;
        private final FormPanel parent;
        private final ComponentGroup componentGroup = new ComponentGroup();

        public GroupBuilder(FormPanel parent) {
            this.parent = parent;
        }

        @Override
        public GroupBuilder addInput(String label, String field, String defaultValue) {
            JLabel jLabel = null;
            if (StringUtils.isNotEmpty(label)) {
                jLabel = new JLabel(label);
                PanelBasedJComponent<JLabel> labelPanel = new PanelBasedJComponent<>(jLabel);
                componentGroup.addComponent(labelPanel);
            }

            if (StringUtils.isNotEmpty(field)) {
                JTextField jTextField = new JTextField(INPUT_DEFAULT_LENGTH);
                jTextField.setText(defaultValue);
                Optional.ofNullable(jLabel).ifPresent(l -> l.setLabelFor(jTextField));
                componentGroup.addComponentWithField(field, jTextField);
            }
            return this;
        }

        @Override
        public GroupBuilder addPwdInput(String label, String field, String defaultValue) {
            JLabel jLabel = null;
            if (StringUtils.isNotEmpty(label)) {
                jLabel = new JLabel(label);
                PanelBasedJComponent<JLabel> labelPanel = new PanelBasedJComponent<>(jLabel);
                componentGroup.addComponent(labelPanel);
            }

            if (StringUtils.isNotEmpty(field)) {
                JPasswordField pwdField = new JPasswordField(INPUT_DEFAULT_LENGTH);
                pwdField.setText(defaultValue);
                Optional.ofNullable(jLabel).ifPresent(l -> l.setLabelFor(pwdField));
                componentGroup.addComponentWithField(field, pwdField);
            }
            return this;
        }

        @Override
        public GroupBuilder addChecks(String label, String[] options) {
            JLabel jLabel = null;
            if (StringUtils.isNotEmpty(label)) {
                jLabel = new JLabel(label);
                PanelBasedJComponent<JLabel> labelPanel = new PanelBasedJComponent<>(jLabel);
                componentGroup.addComponent(labelPanel);
            }

            JPanel container = new JPanel();
            container.setLayout(new FlowLayout(FlowLayout.LEADING));
            componentGroup.addComponent(container);
            List<String> optionList = Arrays.asList(options);
            optionList.forEach(option -> {
                JCheckBox jCheckBox = new JCheckBox(option);
                container.add(jCheckBox);
                componentGroup.addChildComponentWithField(option, jCheckBox);
            });
            Optional.ofNullable(jLabel).ifPresent(l -> l.setLabelFor(container));
            return this;
        }

        @Override
        public GroupBuilder addCheck(String label, String field, String text, boolean checked) {
            JLabel jLabel = null;
            if (StringUtils.isNotEmpty(label)) {
                jLabel = new JLabel(label);
                PanelBasedJComponent<JLabel> labelPanel = new PanelBasedJComponent<>(jLabel);
                componentGroup.addComponent(labelPanel);
            }

            JPanel container = new JPanel();
            container.setLayout(new FlowLayout(FlowLayout.LEADING));
            componentGroup.addComponent(container);
            JCheckBox jCheckBox = new JCheckBox(text);
            jCheckBox.setSelected(checked);
            container.add(jCheckBox);
            componentGroup.addChildComponentWithField(field, jCheckBox);
            Optional.ofNullable(jLabel).ifPresent(l -> l.setLabelFor(container));
            return this;
        }

        @Override
        public GroupBuilder addRadios(String label, String field, String[] options, int selectedIndex) {
            JLabel jLabel = null;
            if (StringUtils.isNotEmpty(label)) {
                jLabel = new JLabel(label);
                PanelBasedJComponent<JLabel> labelPanel = new PanelBasedJComponent<>(jLabel);
                componentGroup.addComponent(labelPanel);
            }

            RadioBtnPanel radioBtns = new RadioBtnPanel();
            componentGroup.addComponentWithField(field, radioBtns);
            ButtonGroup buttonGroup = new ButtonGroup();
            for (int i = 0; i < options.length; i++) {
                String option = options[i];
                JRadioButton radioButton = new JRadioButton(option);
                radioButton.setSelected(i == selectedIndex);
                buttonGroup.add(radioButton);
                radioBtns.addChild(radioButton);
                radioBtns.add(radioButton);
            }
            Optional.ofNullable(jLabel).ifPresent(l -> l.setLabelFor(radioBtns));
            return this;
        }

        @Override
        public GroupBuilder addSelect(String label, String field, String[] options, String defaultValue) {
            if (StringUtils.isNotEmpty(label)) {
                JLabel jLabel = new JLabel(label);
                PanelBasedJComponent<JLabel> labelPanel = new PanelBasedJComponent<>(jLabel);
                componentGroup.addComponent(labelPanel);
            }

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

    /**
     * 「控件可添加」的构造器抽象类
     *
     * @implSpec    需要支持新控件时，向该接口添加新方法，
     *              并同时在 {@link GroupBuilder} 和 {@link FormPanel} 中编写实现逻辑
     */
    private abstract static class ComponentAddableBuilder {

        public ComponentAddableBuilder addInput(String label, String field) {
            addInput(label, field, "");
            return this;
        }

        public ComponentAddableBuilder addPwdInput(String label, String field) {
            addPwdInput(label, field, "");
            return this;
        }

        public ComponentAddableBuilder addCheck(String label, String field, String text) {
            addCheck(label, field, text, false);
            return this;
        }

        public ComponentAddableBuilder addRadios(String label, String field, String[] options) {
            addRadios(label, field, options, -1);
            return this;
        }

        public abstract ComponentAddableBuilder addInput(String label, String field, String defaultValue);

        public abstract ComponentAddableBuilder addPwdInput(String label, String field, String defaultValue);

        public abstract ComponentAddableBuilder addCheck(String label, String field, String text, boolean checked);

        public abstract ComponentAddableBuilder addChecks(String label, String[] options);

        public abstract ComponentAddableBuilder addRadios(String label, String field, String[] options, int selectedIndex);

        public abstract ComponentAddableBuilder addSelect(String label, String field, String[] options, String defaultValue);
    }
}
