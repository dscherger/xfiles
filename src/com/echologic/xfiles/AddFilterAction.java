/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class AddFilterAction extends EnableableAction {

    private static Icon ICON = new ImageIcon(AddFilterAction.class.getResource("/general/add.png"));

    public AddFilterAction() {
        super("Add", "Add Filter", ICON);
    }

    public void actionPerformed(AnActionEvent e) {
    }

}
