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
 *
 * TODO: rename to SelectFilterAction
 */
public class FilterSelectionAction extends AnAction {

    public static final Icon ICON = new ImageIcon(FilterSelectionAction.class.getResource("/runConfigurations/hidePassed.png"));

    private static Logger log = Logger.getInstance(OpenFilesComboBoxAction.class.getName());

    private String name;
    private FilterSelectionComboBoxAction combo;
    private VirtualFileFilter filter;

    public FilterSelectionAction(FilterSelectionComboBoxAction combo, String name, VirtualFileFilter filter) {
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

    /**
     * When a filter is selected we update the text of the FilterSelectionComboBoxAction and then
     * run the RefreshAction with the selected filter.
     *
     * - persist each configured filter
     * - persist which filter is currently selected
     * - need working copies of filters when configuring so we can apply or cancel
     *
     * - need connections between list of filters, filter selection, filter configuration,
     *   and filter execution
     *
     * - filter configuration
     *      - add/edit/delete filters to list of configured filters
     * - filter selection
     *      - change currently selected filter
     *      - execute selected filter when selection changes
     * - filter execution
     *      - execute currently selected filter
     *
     * i.e.
     * - select the filter configuration associated with this selection
     * - then run the filter over the selected configuration
     *
     * so FilterSelectionAction's have associated FilterConfiguration's
     * probably also have a FilterConfigurationPanel for the FilterConfigurationAction to use
     * and a FilterConfigurationList that contains the various configurations in order
     * which would be the thing we persist in the workspace file
     */
    public void actionPerformed(AnActionEvent event) {
        log.debug("selected filter " + name);
        combo.setSelected(this, event);
    }
}
