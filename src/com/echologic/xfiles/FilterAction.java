/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 *
 * TODO: rename to RunFilterAction
 */

public class FilterAction extends AnAction {

    private Logger log = Logger.getInstance(getClass().getName());

    private static Icon icon = new ImageIcon(FilterAction.class.getResource("/actions/sync.png"));

    private DefaultListModel model;
    private Comparator comparator = new VirtualFileComparator();

    public FilterAction(DefaultListModel model) {
        super("refresh filter", "refresh filter", icon);
        this.model = model;
    }

    public void actionPerformed(AnActionEvent event) {
        log.debug("actionPerformed");

        long start = System.currentTimeMillis();

        final Project project = (Project) event.getDataContext().getData(DataConstants.PROJECT);

        final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        final ProjectFileIndex index = projectRootManager.getFileIndex();

        // TODO: once a filter is defined we iterate over everything and see if it fits
        // if it does we add it to the list we're hopefully collecting
        // then sort the list and display it with the proper file status colors
        // and ICON indications of open or closed?
        // and also ICON indications of errors or not?
        // little endian camel case sort might be nice too

        VirtualFile[] roots = projectRootManager.getContentRoots();

        log.debug("iterating content under roots");

        XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);
        filter.addAcceptedVcs(null);
        filter.addAcceptedType(StdFileTypes.PROPERTIES);
        filter.addAcceptedType(StdFileTypes.XML);
        filter.addAcceptedStatus(FileStatus.ADDED);
        filter.addAcceptedStatus(FileStatus.DELETED);
        filter.addAcceptedStatus(FileStatus.MODIFIED);

        XFilesContentIterator content = new XFilesContentIterator(filter);

        for (int i = 0; i < roots.length; i++) {
            VirtualFile root = roots[i];
            log.debug("root " + root.getPath());
            index.iterateContentUnderDirectory(root, content);
        }

        // TODO: FilterConfigurationPanel to edit configurations
        // TODO: FilterConfiguration to store individual configurations
        // TODO: XFilesFilter that uses selected FilterConfiguration to filter files
        // TODO: XFilesConfiguration class to hold FilterConfigurations and selected filter

        // TODO: the collected list should be run through the selected filter which should
        // define the sort order explicitly

        List included = content.getIncluded();
        Collections.sort(included, comparator);

        model.clear();
        for (Iterator iterator = included.iterator(); iterator.hasNext();) {
            VirtualFile file = (VirtualFile) iterator.next();
            model.addElement(file);
        }

        long finish = System.currentTimeMillis();
        long delta = finish - start;

        WindowManager windowManager = WindowManager.getInstance();
        StatusBar statusBar = windowManager.getStatusBar(project);
        statusBar.setInfo("filter refreshed in " + delta + "ms; " + content + "; " + filter);

        filter.log();

        // TODO: decide how to do logical combination of selection based on the information above
        // i.e. if two statuses and two file types are selected for inclusion what is the expected result
        // clearly, logical or between selections of the same type (file type, file status, etc.)
        // but logical and/or between selections of different types might both be useful
        // i.e. these statuses AND those types
        //   vs these status OR those types

    }

}
