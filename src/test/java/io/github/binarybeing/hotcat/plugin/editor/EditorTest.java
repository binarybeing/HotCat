package io.github.binarybeing.hotcat.plugin.editor;

import io.github.binarybeing.hotcat.plugin.BaseTest;

/**
 * @author gn.binarybei
 * @date 2023/3/24
 * @note
 */
public class EditorTest extends BaseTest {

    @Override
    protected Class<?> getTestClass() {
        String testOpen = System.getenv("test.open");
        return "true".equals(testOpen) ? Editor.class : null;
    }
}
