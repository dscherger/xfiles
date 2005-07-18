/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class ConfigurableFilterAction extends AnAction {

    public static final Icon ICON = new ImageIcon(ConfigurableFilterAction.class.getResource("/runConfigurations/hidePassed.png"));

    public ConfigurableFilterAction(String text, String description) {
        super(text, description, ICON);
    }

    public void actionPerformed(AnActionEvent e) {

    }

    public boolean displayTextInToolbar() {
        return true;
    }

}
