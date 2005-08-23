/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.Iterator;
import javax.swing.JComponent;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.fileTypes.StdFileTypes;

/**
 * This class represents a menu of available filter configurations to select from.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class FilterListComboBoxAction extends ComboBoxAction {

    private static Logger log = Logger.getInstance(FilterListComboBoxAction.class.getName());

    private Project project;
    private RefreshAction refreshAction;
    private XFilesConfiguration configuration;
    private DefaultActionGroup group;
    private Presentation presentation;

    public FilterListComboBoxAction(Project project, RefreshAction refreshAction) {
        this.project = project;
        this.refreshAction = refreshAction;
        this.group = new DefaultActionGroup();
    }

    public void setConfiguration(XFilesConfiguration configuration) {
        this.configuration = configuration;

        log.debug("initializing configuration");

        // intitialize with some sensible values until we're persisting properly

        XFilesFilterConfiguration filter;

        filter = new XFilesFilterConfiguration();
        filter.NAME = "config files";
        filter.ACCEPTED_TYPE_NAMES.add(StdFileTypes.PROPERTIES.getName());
        filter.ACCEPTED_TYPE_NAMES.add(StdFileTypes.XML.getName());
        filter.ACCEPTED_TYPE_NAMES.add(StdFileTypes.DTD.getName());
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.NAME = "changed files";
        filter.ACCEPTED_STATUS_NAMES.add(FileStatus.ADDED.getText());
        filter.ACCEPTED_STATUS_NAMES.add(FileStatus.DELETED.getText());
        filter.ACCEPTED_STATUS_NAMES.add(FileStatus.MODIFIED.getText());
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.NAME = "text files";
        filter.ACCEPTED_TYPE_NAMES.add(StdFileTypes.PLAIN_TEXT.getName());
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.NAME = "unknown files";
        filter.ACCEPTED_STATUS_NAMES.add(FileStatus.UNKNOWN.getText());
        filter.ACCEPTED_TYPE_NAMES.add(StdFileTypes.UNKNOWN.getName());
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.NAME = "ignored files";
        filter.ACCEPTED_OTHERS.add(XFilesVirtualFileFilter.IGNORED);
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.NAME = "open files";
        filter.ACCEPTED_OTHERS.add(XFilesVirtualFileFilter.OPEN);
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.NAME = "filter files";
        filter.ACCEPTED_NAME_GLOBS.add("Filter*");
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.NAME = "all files";
        filter.ACCEPTED_OTHERS.add(XFilesVirtualFileFilter.FILE);
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.NAME = "all directories";
        filter.ACCEPTED_OTHERS.add(XFilesVirtualFileFilter.DIRECTORY);
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.NAME = "hibernate mappings";
        filter.ACCEPTED_NAME_GLOBS.add("*.hbm.xml");
        configuration.CONFIGURED_FILTERS.add(filter);

        configuration.SELECTED_FILTER = "open files";

        configurePopupActionGroup();

    }

    public void setSelected(SelectFilterAction selection, AnActionEvent event) {
        presentation.setText(selection.getName());
        refreshAction.setFilter(selection.getFilter());
        refreshAction.actionPerformed(event);
    }

    /**
     * This method is called shortly before createPopupActionGroup to create the component
     * which is passed to createPopupActionGroup. It is overridden here so we can grab the
     * presentation and use it for changing the displayed text when the filter selection
     * changes.
     */
    public JComponent createCustomComponent(Presentation presentation) {
        this.presentation = presentation;
        presentation.setText(configuration.SELECTED_FILTER);
        presentation.setDescription("change/configure filter selections");
        return super.createCustomComponent(presentation);
    }

    /**
     * Note that the only way the list of available SelectFilterAction's can change
     * is through the EditConfigurationsAction. When some SelectFilterAction is
     * performed we need to update the presentation text.
     */
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        log.debug("createPopupActionGroup");
        return group;
    }

    /**
     * This should be called when the configuration changes
     */
    public void configurePopupActionGroup() {
        log.debug("configurePopupActionGroup");
        group.removeAll();

        refreshAction.setFilter(null);

        if (configuration != null) {
            for (Iterator iterator = configuration.CONFIGURED_FILTERS.iterator(); iterator.hasNext();) {
                XFilesFilterConfiguration config = (XFilesFilterConfiguration) iterator.next();
                XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);
                filter.setConfiguration(config);

                if (filter.getName().equals(configuration.SELECTED_FILTER)) {
                    refreshAction.setFilter(filter);
                    log.debug("selected filter " + configuration.SELECTED_FILTER);
                }

                AnAction action = new SelectFilterAction(this, filter.getName(), filter);
                group.add(action);
                log.debug("added filter " + filter.getName());
            }
        } else {
            log.debug("null configuration");
        }

        // TODO: consider adding this to the tool window's toolbar rather than here

        if (group.getChildrenCount() > 0) group.addSeparator();
        group.add(new EditConfigurationsAction());

        if (refreshAction.getFilter() == null) {
            log.debug("null filter selected");
            XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);
            filter.setName("default filter");
            refreshAction.setFilter(filter);
        }
    }

}
