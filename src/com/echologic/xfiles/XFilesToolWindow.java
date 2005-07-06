/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesToolWindow extends JPanel {

    private Logger log = Logger.getInstance(getClass().getName());
    private DefaultListModel model = new DefaultListModel();

    public XFilesToolWindow() {
        super(new BorderLayout());

        AnAction action = new FilterAction(model);

        DefaultActionGroup group = new DefaultActionGroup("xfiles group", false);
        group.addSeparator();
        group.add(action);
        group.addSeparator();

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("here", group, true);

        add(toolbar.getComponent(), BorderLayout.NORTH);


        JScrollPane scroller = new JScrollPane();
        JList list = new JList();

        model.addElement("aaa");
        model.addElement("bbb");
        model.addElement("ccc");
        list.setModel(model);

        scroller.getViewport().setView(list);

        add(scroller, BorderLayout.CENTER);
    }


}
