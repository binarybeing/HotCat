package io.github.binarybeing.hotcat.plugin.panel;

import io.github.binarybeing.hotcat.plugin.BaseTest;
import io.github.binarybeing.hotcat.plugin.utils.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;

public class BasicPanel extends BaseTest {

    @Override
    public Object doExecute() throws Exception {
        JPanel container = new JPanel();
        GridLayout gridLayout = new GridLayout(2, 0);
        container.setLayout(gridLayout);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel item = new JPanel(new GridLayout(2, 0));
        item.add(new JLabel("labelfdsafdsafd1"));
        item.add(new JTextField(15));


        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel item2 = new JPanel(new GridLayout(2, 1));
        item2.add(new JLabel("xxxxxxxxx"));
        item2.add(new JTextField(15));

        row1.add(item);
        row1.add(item2);

        container.add(row1);
//        container.add(row2);



//        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel jPanel = new JPanel();
        GroupLayout layout = new GroupLayout(jPanel);
        jPanel.setLayout(layout);
        JLabel jLabel1 = new JLabel("1");
        JLabel jLabel2 = new JLabel("2");
        JTextField field1 = new JTextField();
        JTextField field2 = new JTextField();
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(jLabel1).addComponent(field1))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                 .addComponent(jLabel2).addComponent(field2))
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel1)
                                .addComponent(field1)
                        ).addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel2)
                                .addComponent(field2)
                        ));

//        URL url = new File("/Users/momo/plugin_projects/HotCat/src/main/resources/loading-loading-forever.gif").toURI().toURL();
        URL url = new File("/Users/momo/plugin_projects/HotCat/src/main/resources/loading.gif").toURI().toURL();
        Icon myImgIcon = new ImageIcon(url);
//        JLabel imageLbl = new JLabel(myImgIcon);
//        JPanel row1 = new JPanel();
//        JPanel row2 = new JPanel();
////        GroupLayout layout = new GroupLayout(row1);
//
//        row1.add(new JLabel("1"));
//        row1.add(new JTextField());
//        row1.add(new JLabel("2"));
//        row1.add(new JTextField());
//        row1.add(new JLabel("3"));
//        row1.add(new JTextField());
//        ButtonGroup buttonGroup = new ButtonGroup();
//
//        row2.add(new JLabel("1"));
//        row2.add(new JPasswordField());
//        row2.add(new JLabel("2"));
//        row2.add(new JPasswordField());
//        row2.add(new JLabel("3"));
//        row2.add(new JPasswordField());
//
//        jPanel.add(row1);
//        jPanel.add(row2);
//        new JLabel();
//        jPanel.add(imageLbl);
//        jPanel.setLayout(new BorderLayout());
//        jPanel.add(imageLbl, BorderLayout.CENTER);
//        jPanel.updateUI();
        boolean ok = DialogUtils.showPanelDialog(event, "hello", container);
        return "success";
    }

    @Override
    public void verify(int code, String msg, String data) throws Exception {
        assert code == 200;
    }

    @Override
    public long until() throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd")
                .parse("2023-11-20").getTime();
    }
}
