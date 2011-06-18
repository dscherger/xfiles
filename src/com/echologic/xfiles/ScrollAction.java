/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class ScrollAction extends ToggleAction {

    boolean selected;
    private ChangeListener listener;

    private ScrollAction(String text, String description, Icon icon, ChangeListener listener) {
        super(text, description, icon);
        this.listener = listener;
    }

    public static ScrollAction scrollToSource(ChangeListener listener) {
        return new ScrollAction("Autoscroll to Source",
                                "Enable/Disable Autoscroll to Source",
                                XFilesIcons.SCROLL_TO_ICON, listener);
    }

    public static ScrollAction scrollFromSource(ChangeListener listener) {
        return new ScrollAction("Autoscroll from Source",
                                "Enable/Disable Autoscroll from Source",
                                XFilesIcons.SCROLL_FROM_ICON, listener);
    }

    public boolean isSelected(AnActionEvent event) {
        return selected;
    }

    public void setSelected(AnActionEvent event, boolean selected) {
        this.selected = selected;
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
