/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * This class represents selection of a named filter configuration from the list
 * of available configurations.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class FilterConfigurationAction extends AnAction {

    public FilterConfigurationAction() {
        super("Edit Configurations");
    }

    public void actionPerformed(AnActionEvent e) {
    }
}
