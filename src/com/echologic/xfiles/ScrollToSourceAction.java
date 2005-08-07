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
public class ScrollToSourceAction extends ToggleAction {

    private static Icon icon = new ImageIcon(ScrollToSourceAction.class.getResource("/general/autoscrollToSource.png"));

    boolean selected = true; // TODO: persist setting in workspace

    public ScrollToSourceAction() {
        // TODO: create a scroll action and use two instances
        super("Autoscroll to Source", "Enable/Disable Autoscroll to Source", icon);
    }

    public boolean isSelected(AnActionEvent e) {
        return selected;
    }

    public void setSelected(AnActionEvent e, boolean selected) {
        this.selected = selected;
    }
}
