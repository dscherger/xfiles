/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;

/**
 * This class represents selection of a named filter configuration from the list
 * of available configurations.
 * 
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class SelectFilterAction extends AnAction {

    private Logger log = Logger.getInstance(getClass().getName());

    private String name;
    private FilterListComboBoxAction filters;
    private XFilesVirtualFileFilter filter;

    // TODO: do we need to hold the filter here, or just it's name to use for a lookup?

    public SelectFilterAction(FilterListComboBoxAction filters, String name, XFilesVirtualFileFilter filter) {
        super(name, "description", XFilesIcons.FILTER_ICON);
        this.filters = filters;
        this.name = name;
        this.filter = filter;
    }

    public String getName() {
        return name;
    }

    public XFilesVirtualFileFilter getFilter() {
        return filter;
    }

    public void actionPerformed(AnActionEvent event) {
        log.debug("selected filter " + name);
        filters.setSelected(this, event);
    }
}
