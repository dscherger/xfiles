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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFileFilter;

/**
 * This class represents selection of a named filter configuration from the list
 * of available configurations.
 * 
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class SelectFilterAction extends AnAction {

    //public static final Icon ICON = new ImageIcon(SelectFilterAction.class.getResource("/runConfigurations/hidePassed.png"));
    public static final Icon ICON = new ImageIcon(SelectFilterAction.class.getResource("/debugger/class_filter.png"));

    private static Logger log = Logger.getInstance(OpenFilesComboBoxAction.class.getName());

    private String name;
    private FilterSelectionComboBoxAction combo;
    private VirtualFileFilter filter;

    // TODO: do we need to hold the filter here, or just it's name to use for a lookup?

    public SelectFilterAction(FilterSelectionComboBoxAction combo, String name, VirtualFileFilter filter) {
        super(name, "description", ICON);
        this.combo = combo;
        this.name = name;
        this.filter = filter;
    }

    public String getName() {
        return name;
    }

    public VirtualFileFilter getFilter() {
        return filter;
    }

    public void actionPerformed(AnActionEvent event) {
        log.debug("selected filter " + name);
        combo.setSelected(this, event);
    }
}
