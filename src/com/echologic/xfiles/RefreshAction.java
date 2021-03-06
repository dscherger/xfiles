/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.List;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class RefreshAction extends AnAction {

    private Logger log = Logger.getInstance(getClass().getName());

    private XFilesListModel model;
    private XFilesVirtualFileFilter filter;

    public RefreshAction(XFilesListModel model) {
        super("refresh filter", "refresh filter", XFilesIcons.SYNC_ICON);
        this.model = model;
    }

    public XFilesVirtualFileFilter getFilter() {
        return filter;
    }

    public void setFilter(XFilesVirtualFileFilter filter) {
        log.debug("refresh filter set to " + filter);
        this.filter = filter;
    }

    public void actionPerformed(AnActionEvent event) {
        log.debug("actionPerformed");

        if (filter == null) {
            log.debug("no filter selected");
            return;
        }

        long start = System.currentTimeMillis();

        DataContext context = event.getDataContext();
        final Project project = PlatformDataKeys.PROJECT.getData(context);

        CountingFilterListener listener = new CountingFilterListener();
        filter.setListener(listener);

        XFilesContentIterator content = new XFilesContentIterator(project);
        content.setFilter(filter);
        content.iterate();

        listener.log();

        List<VirtualFile> included = content.getIncluded();

        model.clear();
        for (VirtualFile file : included) {
            model.addElement(file);
        }

        long finish = System.currentTimeMillis();
        long delta = finish - start;

        WindowManager windowManager = WindowManager.getInstance();
        StatusBar statusBar = windowManager.getStatusBar(project);
        statusBar.setInfo(filter.getName() + " filter refreshed in " + delta + "ms; " + content + listener);

        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        VirtualFile[] files = editorManager.getSelectedFiles();

        if (files.length > 0)
            model.setSelectedItem(files[0]);
    }

}
