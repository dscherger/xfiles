/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class RefreshAction extends AnAction {

    private Logger log = Logger.getInstance(getClass().getName());

    private static Icon icon = new ImageIcon(RefreshAction.class.getResource("/actions/sync.png"));

    private OpenFilesComboBoxModel model;
    private VirtualFileFilter filter;

    public RefreshAction(OpenFilesComboBoxModel model) {
        super("refresh filter", "refresh filter", icon);
        this.model = model;
    }

    public VirtualFileFilter getFilter() {
        return filter;
    }

    public void setFilter(VirtualFileFilter filter) {
        this.filter = filter;
    }

    public void actionPerformed(AnActionEvent event) {
        log.debug("actionPerformed");

        if (filter == null) {
            log.debug("no filter selected");
            return;
        }

        long start = System.currentTimeMillis();

        final Project project = (Project) event.getDataContext().getData(DataConstants.PROJECT);

        final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        final ProjectFileIndex index = projectRootManager.getFileIndex();

        VirtualFile[] roots = projectRootManager.getContentRoots();

        log.debug("iterating content under roots");

        XFilesContentIterator content = new XFilesContentIterator(filter);

        for (int i = 0; i < roots.length; i++) {
            VirtualFile root = roots[i];
            log.debug("root " + root.getPath());
            index.iterateContentUnderDirectory(root, content);
        }

        // TODO: FilterConfigurationPanel to edit configurations

        List included = content.getIncluded();

        model.clear();
        for (Iterator iterator = included.iterator(); iterator.hasNext();) {
            VirtualFile file = (VirtualFile) iterator.next();
            model.addElement(file);
        }

        long finish = System.currentTimeMillis();
        long delta = finish - start;

        WindowManager windowManager = WindowManager.getInstance();
        StatusBar statusBar = windowManager.getStatusBar(project);
        statusBar.setInfo("filter refreshed in " + delta + "ms; " + content + " " + filter);

        // TODO: decide how to do logical combination of selection based on the information above
        // i.e. if two statuses and two file types are selected for inclusion what is the expected result
        // clearly, logical or between selections of the same type (file type, file status, etc.)
        // but logical and/or between selections of different types might both be useful
        // i.e. these statuses AND those types
        //   vs these status OR those types

    }

}
