/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.JComponent;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.diagnostic.Logger;

/**
 * This class represents a menu of available filter configurations to select from.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class FilterSelectionComboBoxAction extends ComboBoxAction {

    private static Logger log = Logger.getInstance(OpenFilesComboBoxAction.class.getName());

    private DefaultActionGroup group;
    private Presentation presentation;

    /**
     * Note that the only way the list of available FilterSelectionAction's can change
     * is through the FilterConfigurationAction. When some FilterSelectionAction is
     * performed we need to update the presentation text.
     */
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        log.debug("createPopupActionGroup");

        group = new DefaultActionGroup();

        group.add(new FilterSelectionAction("text files"));
        group.add(new FilterSelectionAction("changed files"));
        group.add(new FilterSelectionAction("open files"));
        group.addSeparator();
        group.add(new FilterConfigurationAction());

        return group;
    }

    public JComponent createCustomComponent(Presentation presentation) {
        this.presentation = presentation;
        presentation.setText("selected filter");
        presentation.setDescription("change/configure filter selection");

        return super.createCustomComponent(presentation);
    }
}
