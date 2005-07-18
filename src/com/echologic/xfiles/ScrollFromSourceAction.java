/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class ScrollFromSourceAction extends ToggleAction {

    private static Icon icon = new ImageIcon(ScrollFromSourceAction.class.getResource("/general/autoscrollFromSource.png"));

    boolean selected;

    public ScrollFromSourceAction() {
        super("Autoscroll from Source", "Enable/Disable Autoscroll from Source", icon);
    }

    public boolean isSelected(AnActionEvent e) {
        return selected;
    }

    public void setSelected(AnActionEvent e, boolean selected) {
        this.selected = selected;
    }
}
