/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class FilterAction extends AnAction {

    private Logger log = Logger.getInstance(getClass().getName());

    private static Icon icon = new ImageIcon("icons/filter.png");

    private DefaultListModel model;

    public FilterAction(DefaultListModel model) {
        super("filter", "filter files", icon);
        this.model = model;
    }

    public void actionPerformed(AnActionEvent event) {
        log.debug("actionPerformed");

        Project project = (Project) event.getDataContext().getData(DataConstants.PROJECT);

        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

        model.clear();

        VirtualFile[] files = fileEditorManager.getOpenFiles();
        for (int i = 0; i < files.length; i++) {
            VirtualFile file = files[i];
            log.debug("open file " + file.getPath());
            model.addElement(file.getPath());
        }

        // various index methods that look interesting and possibly relevant
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

        final ProjectFileIndex index = projectRootManager.getFileIndex();
        final FileStatusManager fileStatusManager = FileStatusManager.getInstance(project);

        // TODO: once a filter is defined we iterate over everything and see if it fits
        // if it does we add it to the list we're hopefully collecting
        // then sort the list and display it with the proper file status colors
        // and icon indications of open or closed?
        // and also icon indications of errors or not?
        // little endian camel case sort might be nice too

        // defining a filter
        // - vcs status: any, or selected (modified, unknown, ignored, missing, ...)
        // - editor status: any, open, closed
        // - compile status: any, errors, ok
        // - name glob match
        //
        // all open files
        // all changed files
        // all unknown files
        // all files like *Entry or Member*Entry (intellij must have globbing code somewhere -- oro?)
        // all files with compile errors (not sure if this is possible)

        // ui should have a dropdown of defined filters to select one or create/edit/delete them
        // also various sort options

        // alternatively, how about we classify files into several different lists
        // and then have different list selection buttons
        // - open files
        // - changed files
        // - missing files

        ContentIterator iterator = new ContentIterator() {
            public boolean processFile(VirtualFile fileOrDir) {
                if (fileStatusManager != null) {
                    String type = fileOrDir.isDirectory() ? "directory " : "file ";

                    FileStatus status = fileStatusManager.getStatus(fileOrDir);
                    log.debug(status.getText() + " " +
                              type + fileOrDir.getPath() +
                              " java " + index.isJavaSourceFile(fileOrDir) +
                              " open " + fileEditorManager.isFileOpen(fileOrDir));
                }
                return true;
            }
        };

        VirtualFile[] roots = projectRootManager.getContentRoots();

        log.debug("iterating content under roots");
        for (int i = 0; i < roots.length; i++) {
                VirtualFile root = roots[i];
                log.debug("root " + root.getPath() + " isInContent " + index.isInContent(root));
                index.iterateContentUnderDirectory(root, iterator);
        }

    }
}
