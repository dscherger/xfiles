/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.Iterator;
import javax.swing.JComponent;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

/**
 * This class represents a menu of available filter configurations to select from.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class FilterListComboBoxAction extends ComboBoxAction {

    private Logger log = Logger.getInstance(getClass().getName());

    private Project project;
    private RefreshAction refreshAction;
    private XFilesConfiguration configuration;

    private DefaultActionGroup group;

    private Presentation presentation;

    public FilterListComboBoxAction(Project project, XFilesConfiguration configuration, RefreshAction refreshAction) {
        this.project = project;
        this.configuration = configuration;
        this.refreshAction = refreshAction;

        configuration.setListener(this);

        log.debug("initializing configuration");
        log.debug("selected " + configuration.SELECTED_FILTER);
        log.debug("scroll to source " + configuration.SCROLL_TO_SOURCE);
        log.debug("scroll from source " + configuration.SCROLL_FROM_SOURCE);
        log.debug(configuration.CONFIGURED_FILTERS.size() + " configured filters");

        for (Iterator iterator = configuration.CONFIGURED_FILTERS.iterator(); iterator.hasNext();) {
            XFilesFilterConfiguration filter = (XFilesFilterConfiguration) iterator.next();
            log.debug("filter " + filter.NAME);
        }
    }

    public void setSelected(AnActionEvent event, XFilesVirtualFileFilter filter) {
        presentation.setText(filter.getName());
        refreshAction.setFilter(filter);
        refreshAction.actionPerformed(event);
        configuration.SELECTED_FILTER = filter.getName();
    }

    /**
     * This method is called shortly after construction to create the drop down list for the
     * toolbar. It is overridden here so we can grab the presentation and use it for changing
     * the displayed text when the filter selection changes. The component this method creates
     * is passed to createPopupActionGroup when the popup is first activated.
     */
    public JComponent createCustomComponent(Presentation presentation) {
        log.debug("createCustomComponent");
        this.presentation = presentation;
        presentation.setDescription("change filter selection");

        group = new DefaultActionGroup();

        // this creates all the configured filters and sets the selected filter so that it
        // making it available to the editor listener in the tool window during startup
        // without this the list is initially empty and requires a manual sync
        configurePopupActionGroup();

        return super.createCustomComponent(presentation);
    }

    /**
     * This method returns the group that was created and configured in
     * createCustomComponent above.
     *
     * Note that the only way the list of available SelectFilterAction's can change
     * is through the EditConfigurationsAction. When some SelectFilterAction is
     * performed we need to update the presentation text.
     */
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        log.debug("createPopupActionGroup");
        return group;
    }

    /**
     * This should be called when the configuration changes.
     *
     * Both the group and the presentation must be available when this runs.
     */
    public void configurePopupActionGroup() {
        log.debug("configurePopupActionGroup");

        int size = 0;
        if (configuration != null) size = configuration.CONFIGURED_FILTERS.size();

        group.removeAll();

        XFilesVirtualFileFilter selected = null;

        if (size > 0) {

            for (int i=0; i<size; i++) {
                XFilesFilterConfiguration config = (XFilesFilterConfiguration) configuration.CONFIGURED_FILTERS.get(i);
                XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);
                filter.setConfiguration(config);

                SelectFilterAction action = new SelectFilterAction(this, filter);
                group.add(action);

                // select the first filter by default but select the named filter when it matches
                if (selected == null || filter.getName().equals(configuration.SELECTED_FILTER))
                    selected = filter;

                log.debug("added filter " + filter.getName() + " selected " + selected.getName());
            }

        } else {
            // TODO: we shouldn't really need a default filter
            // it's possible that we need one for the group to be properly created?!?
            // i.e. the drop down doesn't *ever* work if it has no elements initially 

            String name = "default filter";

            XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);
            filter.setName(name);

            SelectFilterAction action = new SelectFilterAction(this, filter);
            group.add(action);

            configuration.SELECTED_FILTER = "";
            selected = filter;

            log.debug("added filter " + filter.getName() + " selected " + selected.getName());
        }

        refreshAction.setFilter(selected);
        presentation.setText(selected.getName());
    }

}
