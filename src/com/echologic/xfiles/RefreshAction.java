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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.fileEditor.FileEditorManager;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class RefreshAction extends AnAction {

    private Logger log = Logger.getInstance(getClass().getName());

    private static Icon icon = new ImageIcon(RefreshAction.class.getResource("/actions/sync.png"));

    private XFilesListModel model;
    private XFilesVirtualFileFilter filter;

    public RefreshAction(XFilesListModel model) {
        super("refresh filter", "refresh filter", icon);
        this.model = model;
    }

    public VirtualFileFilter getFilter() {
        return filter;
    }

    public void setFilter(XFilesVirtualFileFilter filter) {
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

        CountingFilterListener listener = new CountingFilterListener();
        filter.setListener(listener);

        XFilesContentIterator content = new XFilesContentIterator(project);
        content.setFilter(filter);
        content.iterate();

        listener.log();

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
        statusBar.setInfo("filter refreshed in " + delta + "ms; " + content + listener);

        // TODO: decide how to do logical combination of selection based on the information above
        // i.e. if two statuses and two file types are selected for inclusion what is the expected result
        // clearly, logical or between selections of the same type (file type, file status, etc.)
        // but logical and/or between selections of different types might both be useful
        // i.e. these statuses AND those types
        //   vs these status OR those types

        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        VirtualFile[] files = editorManager.getSelectedFiles();

        if (files != null && files.length > 0)
            model.setSelectedItem(files[0]);
    }

}
