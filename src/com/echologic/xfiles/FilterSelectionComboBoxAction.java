/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.JComponent;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
 * TODO: rename to SelectFilterComboBoxAction
 */
public class FilterSelectionComboBoxAction extends ComboBoxAction {

    private static Logger log = Logger.getInstance(OpenFilesComboBoxAction.class.getName());

    private DefaultActionGroup group;
    private Presentation presentation;
    private RefreshAction refreshAction;

    private XFilesVirtualFileFilter configFilter;
    private XFilesVirtualFileFilter changedFilter;
    private XFilesVirtualFileFilter textFilter;
    private XFilesVirtualFileFilter ignoredFilter;
    private XFilesVirtualFileFilter unknownFilter;
    private XFilesVirtualFileFilter openFilter;
    private XFilesVirtualFileFilter actionFilter;

    // TODO: hibernate mapping filter *.hbm.xml in build/mappings directory!?!

    public FilterSelectionComboBoxAction(Project project, RefreshAction refreshAction) {
        configFilter = new XFilesVirtualFileFilter(project);
        configFilter.addAcceptedType(StdFileTypes.PROPERTIES);
        configFilter.addAcceptedType(StdFileTypes.XML);
        configFilter.addAcceptedType(StdFileTypes.DTD);

        changedFilter = new XFilesVirtualFileFilter(project);
        changedFilter.addAcceptedStatus(FileStatus.ADDED);
        changedFilter.addAcceptedStatus(FileStatus.DELETED);
        changedFilter.addAcceptedStatus(FileStatus.MODIFIED);

        textFilter = new XFilesVirtualFileFilter(project);
        textFilter.addAcceptedType(StdFileTypes.PLAIN_TEXT);

        unknownFilter = new XFilesVirtualFileFilter(project);
        unknownFilter.addAcceptedStatus(FileStatus.UNKNOWN);
        unknownFilter.addAcceptedType(StdFileTypes.UNKNOWN);

        ignoredFilter = new XFilesVirtualFileFilter(project);
        ignoredFilter.setAcceptIgnored(true);

        openFilter = new XFilesVirtualFileFilter(project);
        openFilter.setAcceptOpen(true);

        actionFilter = new XFilesVirtualFileFilter(project);
        actionFilter.addAcceptedGlob("Filter*");

        this.refreshAction = refreshAction;
    }

    public void setSelected(FilterSelectionAction selection, AnActionEvent event) {
        presentation.setText(selection.getName());
        refreshAction.setFilter(selection.getFilter());
        refreshAction.actionPerformed(event);
    }

    /**
     * Note that the only way the list of available FilterSelectionAction's can change
     * is through the FilterConfigurationAction. When some FilterSelectionAction is
     * performed we need to update the presentation text.
     */
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        log.debug("createPopupActionGroup");

        group = new DefaultActionGroup();

        group.add(new FilterSelectionAction(this, "config files", configFilter));
        group.add(new FilterSelectionAction(this, "changed files", changedFilter));
        group.add(new FilterSelectionAction(this, "text files", textFilter));
        group.add(new FilterSelectionAction(this, "ignored files", ignoredFilter));
        group.add(new FilterSelectionAction(this, "unknown files", unknownFilter));
        group.add(new FilterSelectionAction(this, "open files", openFilter));
        group.add(new FilterSelectionAction(this, "actions", actionFilter));
        group.addSeparator();
        group.add(new FilterConfigurationAction());

        return group;
    }

    /**
     * TODO: is this going to work ok when there are several projects open?
     * @see OpenFilesComboBoxAction/Listener/Model for the specific problems
     */
    public JComponent createCustomComponent(Presentation presentation) {
        this.presentation = presentation;
        presentation.setDescription("change/configure filter selections");

        return super.createCustomComponent(presentation);
    }
}
