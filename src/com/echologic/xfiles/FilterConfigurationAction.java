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

    /**
     * Display a configuration panel with something like the following:
     *
     * list of named filters
     * add/drop/copy/move up/move down on list
     * name: ___
     * module: _ aaa _ bbb _ ccc				(checkboxes)
     * editor: _ open _ closed					(checkboxes)
     * vcs status: _ unknown _ ignored ...			(checkboxes and named vcs)
     * file classification: _ source _ test _ java _ text	(checkboxes not FileType instances perhaps classification)
     * file type: _ java _ text _ ...				(checkboxes FileType instances)
     * file name: ______					(glob)
     * compile status: _ ok _ errors				(checkboxes)
     * sort: type, name, status, module, ...			(table with sortable columns?)
     * sync editor with selected files?				(single checkbox)
     * ok, cancel, apply, help buttons
     *
     * then when this panel is applied update the filter selection action to include the
     * updated list of filters
     *
     * edit the FilterConfigurationList on the FilterConfigurationPanel
     *
     * FilterConfigurationList should implement ProjectComponent to be persisted in the workspace
     * 
     * @param e
     */
    public void actionPerformed(AnActionEvent e) {
        // TODO: display a modal configuration window
    }
}
