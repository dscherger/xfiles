/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.Icon;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class ScrollAction extends ToggleAction {

    boolean selected;

    private ScrollAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    public static ScrollAction scrollToSource() {
        return new ScrollAction("Autoscroll to Source",
                                "Enable/Disable Autoscroll to Source",
                                XFilesIcons.SCROLL_TO_ICON);
    }

    public static ScrollAction scrollFromSource() {
        return new ScrollAction("Autoscroll from Source",
                                "Enable/Disable Autoscroll from Source",
                                XFilesIcons.SCROLL_FROM_ICON);
    }

    public boolean isSelected(AnActionEvent event) {
        return selected;
    }

    public void setSelected(AnActionEvent event, boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
