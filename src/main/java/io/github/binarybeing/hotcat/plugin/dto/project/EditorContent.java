package io.github.binarybeing.hotcat.plugin.dto.project;

public class EditorContent {
    //string text = 1;
    //optional TextSelectModel selectModel = 2;
    private String text;
    private TextSelectModel selectModel;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TextSelectModel getSelectModel() {
        return selectModel;
    }

    public void setSelectModel(TextSelectModel selectModel) {
        this.selectModel = selectModel;
    }
}
