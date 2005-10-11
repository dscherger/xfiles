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

    private XFilesVirtualFileFilter filter;
    private FilterListComboBoxAction list;

    public SelectFilterAction(FilterListComboBoxAction filters, XFilesVirtualFileFilter filter) {
        super(filter.getName(), filter.getName(), XFilesIcons.FILTER_ICON);
        this.list = filters;
        this.filter = filter;
    }

    public void actionPerformed(AnActionEvent event) {
        log.debug("selected filter " + filter.getName());
        list.setSelected(event, filter);
    }

    public XFilesVirtualFileFilter getFilter() {
        return filter;
    }
}
