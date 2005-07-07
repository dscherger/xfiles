/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
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

    private static Icon icon = new ImageIcon(FilterAction.class.getResource("/actions/sync.png"));

    private DefaultListModel model;

    public FilterAction(DefaultListModel model) {
        super("filter", "filter files", icon);
        this.model = model;
    }

    public void actionPerformed(AnActionEvent event) {
        log.debug("actionPerformed");

        Project project = (Project) event.getDataContext().getData(DataConstants.PROJECT);

        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

        VirtualFile[] files = fileEditorManager.getOpenFiles();
        for (int i = 0; i < files.length; i++) {
            VirtualFile file = files[i];
            log.debug("open file " + file.getPath());
            //model.addElement("open " + file.getName());
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

        // TODO: add a dropdown menu to the toolbar with a sorted list of open files?
        // TODO: type: any/java/non-java

        // ui should have a dropdown of defined filters to select one or create/edit/delete them
        // also various sort options

        // alternatively, how about we classify files into several different lists
        // and then have different list selection buttons
        // - open files
        // - changed files
        // - missing files
        // - selection of roots?

        final Map statusMap = new HashMap();

        ContentIterator content = new ContentIterator() {
            public boolean processFile(VirtualFile file) {
                if (fileStatusManager != null) {
                    String type = file.isDirectory() ? "directory " : "file ";

                    FileStatus status = fileStatusManager.getStatus(file);

                    statusMap.put(status.getText(), status);

                    log.debug(status.getText() + " " +
                              type + file.getPath() +
                              " java " + index.isJavaSourceFile(file) +
                              " open " + fileEditorManager.isFileOpen(file));

                    /*
                    // we may want all of the following, and the others that aren't listed as well?
                    index.isContentJavaSourceFile()
                    index.isIgnored()
                    index.isInContent()
                    index.isInLibraryClasses()
                    index.isInLibrarySource()
                    index.isInSource()
                    index.isInSourceContent()
                    index.isInTestSourceContent()
                    index.isJavaSourceFile()
                    index.isLibraryClassFile()
                    */

                    if (!file.isDirectory()) {
                        VirtualFileAdapter adapter = new VirtualFileAdapter(file, status, fileEditorManager.isFileOpen(file));
                        model.addElement(adapter);
                    }
                }
                return true;
            }
        };

        VirtualFile[] roots = projectRootManager.getContentRoots();

        log.debug("iterating content under roots");
        model.clear();

        for (int i = 0; i < roots.length; i++) {
            VirtualFile root = roots[i];
            log.debug("root " + root.getPath() + " isInContent " + index.isInContent(root));

            // TODO: possibly allow iterator.setRoot(root); so that we can remove the absolute paths?
            // TODO: keep a set of the unique status values seen here and save them in the workspace
            // to allow for display of selected statuses

            index.iterateContentUnderDirectory(root, content);
        }

        // nb: there seems to be no way to get the list of all file statuses in the 4.5.x openapi
        // I seem to recall that there is a getAllFileStatuses api in the 5.0 eap openapi?

        log.debug("available file statuses");

        for (Iterator iterator = statusMap.keySet().iterator(); iterator.hasNext();) {
            String text = (String) iterator.next();
            FileStatus status = (FileStatus) statusMap.get(text);
            log.debug("status " + text + " is " + status);

        }
    }
}
