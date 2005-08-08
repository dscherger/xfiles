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
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vcs.FileStatus;

/**
 * This class represents a menu of available filter configurations to select from.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 *
 * TODO: rename to FilterListComboBoxAction
 */
public class FilterSelectionComboBoxAction extends ComboBoxAction {

    private static Logger log = Logger.getInstance(OpenFilesComboBoxAction.class.getName());

    private Project project;
    private RefreshAction refreshAction;
    private XFilesConfiguration configuration;
    private DefaultActionGroup group;
    private Presentation presentation;


    public FilterSelectionComboBoxAction(Project project, RefreshAction refreshAction) {

        this.project = project;
        this.refreshAction = refreshAction;

        // TODO: we may want to move all of this initialization to the tool window
        // and do it as things are initially constructed -- it seems a bit awkward here

        configuration = project.getComponent(XFilesConfiguration.class);

        /*
	if (configuration == null) {
		log.debug("creating configuration");
		configuration = new XFilesConfiguration();
	}
	*/

        log.debug("initializing configuration");

        // intitialize with some sensible values until we're persisting properly

        XFilesFilterConfiguration filter;

        filter = new XFilesFilterConfiguration();
        filter.FILTER_NAME = "config files";
        filter.ACCEPTED_TYPE_NAMES.add(StdFileTypes.PROPERTIES.getName());
        filter.ACCEPTED_TYPE_NAMES.add(StdFileTypes.XML.getName());
        filter.ACCEPTED_TYPE_NAMES.add(StdFileTypes.DTD.getName());
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.FILTER_NAME = "changed files";
        filter.ACCEPTED_STATUS_NAMES.add(FileStatus.ADDED.getText());
        filter.ACCEPTED_STATUS_NAMES.add(FileStatus.DELETED.getText());
        filter.ACCEPTED_STATUS_NAMES.add(FileStatus.MODIFIED.getText());
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.FILTER_NAME = "text files";
        filter.ACCEPTED_TYPE_NAMES.add(StdFileTypes.PLAIN_TEXT.getName());
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.FILTER_NAME = "unknown files";
        filter.ACCEPTED_STATUS_NAMES.add(FileStatus.UNKNOWN.getText());
        filter.ACCEPTED_TYPE_NAMES.add(StdFileTypes.UNKNOWN.getName());
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.FILTER_NAME = "ignored files";
        filter.ACCEPT_IGNORED_FILES = true;
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.FILTER_NAME = "open files";
        filter.ACCEPT_OPEN_FILES = true;
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.FILTER_NAME = "filter files";
        filter.ACCEPTED_NAME_GLOBS.add("Filter*");
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.FILTER_NAME = "all files";
        filter.ACCEPT_FILES = true;
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.FILTER_NAME = "all directories";
        filter.ACCEPT_DIRECTORIES = true;
        configuration.CONFIGURED_FILTERS.add(filter);

        filter = new XFilesFilterConfiguration();
        filter.FILTER_NAME = "hibernate mappings";
        filter.ACCEPTED_NAME_GLOBS.add("*.hbm.xml");
        configuration.CONFIGURED_FILTERS.add(filter);

        configuration.SELECTED_FILTER = "open files";

        // TODO: here we need to get the XFilesConfiguration component
        // and reconstruct the filters it has persisted. when we get into
        // editing filters we'll need to keep the configuration data in sync
        // with the filters.

        // presumably editing the filters will pull data from the configuration
        // to the ui in reset and push datafrom the ui to the configuration in apply.
        // I beleive the configuration is saved to the workspace on apply as well
        // and we should probably drop and recreate all of the filters at that point

        group = new DefaultActionGroup();
        configurePopupActionGroup();
    }

    public void setSelected(FilterSelectionAction selection, AnActionEvent event) {
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
     * Note that the only way the list of available FilterSelectionAction's can change
     * is through the FilterConfigurationAction. When some FilterSelectionAction is
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

                AnAction action = new FilterSelectionAction(this, filter.getName(), filter);
                group.add(action);
                log.debug("added filter " + filter.getName());
            }
        } else {
            log.debug("null configuration");
        }

        if (group.getChildrenCount() > 0) group.addSeparator();
        group.add(new FilterConfigurationAction());

        if (refreshAction.getFilter() == null) {
            log.debug("null filter selected");
            XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);
            filter.setName("default filter");
            refreshAction.setFilter(filter);
        }
    }

}
