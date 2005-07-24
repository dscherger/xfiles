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
 * This class represents selection of a named filter configuration from the list
 * of available configurations.
 * 
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class FilterSelectionAction extends AnAction {

    public static final Icon ICON = new ImageIcon(FilterSelectionAction.class.getResource("/runConfigurations/hidePassed.png"));

    public FilterSelectionAction(String name) {
        super(name, "description", ICON);
    }

    /**
     * When a selection is made we need to update the text in the FilterSelectionComboBoxAction
     * and rerun the FilterAction with the selected filter.
     */
    public void actionPerformed(AnActionEvent e) {
    }
}
