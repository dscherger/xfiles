/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
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
        final ProjectLevelVcsManager projectLevelVcsManager = ProjectLevelVcsManager.getInstance(project);
        final ProjectFileIndex index = projectRootManager.getFileIndex();
        final FileStatusManager fileStatusManager = FileStatusManager.getInstance(project);

        // TODO: once a filter is defined we iterate over everything and see if it fits
        // if it does we add it to the list we're hopefully collecting
        // then sort the list and display it with the proper file status colors
        // and ICON indications of open or closed?
        // and also ICON indications of errors or not?
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

        // TODO: type: any/java/non-java

        final Map statusMap = new HashMap();
        final Map typeMap = new HashMap();
        final Map vcsMap = new HashMap();
        final Map moduleMap = new HashMap();

        final List selected = new ArrayList();

        final VirtualFileCounter allFiles = new VirtualFileCounter();
        final VirtualFileCounter ignoredFiles = new VirtualFileCounter();
        final VirtualFileCounter sourceFiles = new VirtualFileCounter();
        final VirtualFileCounter testFiles = new VirtualFileCounter();

        // TODO: XFilesContentIterator?

        ContentIterator content = new ContentIterator() {
            public boolean processFile(VirtualFile file) {
                if (fileStatusManager != null) {
                    // TODO: may want to count files and directories
                    //String node = file.isDirectory() ? "directory " : "file ";

                    FileStatus status = fileStatusManager.getStatus(file);
                    VirtualFileCounter counter = (VirtualFileCounter) statusMap.get(status);
                    if (counter == null) {
                        counter = new VirtualFileCounter();
                        statusMap.put(status, counter);
                    }
                    counter.count(file);

                    FileType type = file.getFileType();
                    counter = (VirtualFileCounter) typeMap.get(type);
                    if (counter == null) {
                        counter = new VirtualFileCounter();
                        typeMap.put(type, counter);
                    }
                    counter.count(file);

                    AbstractVcs vcs = projectLevelVcsManager.getVcsFor(file);
                    String vcsName = "<None>";

                    if (vcs != null) vcsName = vcs.getName();

                    counter = (VirtualFileCounter) vcsMap.get(vcsName);
                    if (counter == null) {
                        counter = new VirtualFileCounter();
                        vcsMap.put(vcsName, counter);
                    }
                    counter.count(file);

                    // we may want all of the following, and the others that aren't listed as well?

                    Module module = index.getModuleForFile(file);
                    counter = (VirtualFileCounter) moduleMap.get(module);
                    if (counter == null) {
                        counter = new VirtualFileCounter();
                        moduleMap.put(module, counter);
                    }
                    counter.count(file);

                    if (index.isIgnored(file)) ignoredFiles.count(file);

                    // note that SourceContent is a superset TestSourceContent

                    if (index.isInTestSourceContent(file))
                        testFiles.count(file);
                    else if (index.isInSourceContent(file))
                        sourceFiles.count(file);

                    allFiles.count(file);

                    // TODO: consult a filter to decide whether we add this file or not
                    // filter defintion needs to contain
                    // - file status: selected/all
                    // - editor state: open/closed/all
                    // - module: selected/all
                    // - file type: java/non-java/all
                    // - file name glob:
                    // - compiler status: ok/errors/all

                    String statusCode = status.toString();
                    if (!file.isDirectory() &&
                        (vcs == null || (!statusCode.equals("NOT_CHANGED")) &&
                                         !statusCode.equals("UNCHANGED") &&
                                         !statusCode.equals("UNKNOWN")))
                    {
                        selected.add(file);
                    }


                    // for name matches we can use reges or globs (allowing only * chars)
                    // where foo*a*b*bar is evaluated with
                    // startsWith("foo")
                    // indexOf("a") > 0
                    // indexOf("b") > indexOf("a")
                    // endsWith("bar")
                }
                return true;
            }
        };

        VirtualFile[] roots = projectRootManager.getContentRoots();

        log.debug("iterating content under roots");

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

        Collections.sort(selected, comparator);

        model.clear();
        for (Iterator iterator = selected.iterator(); iterator.hasNext();) {
            VirtualFile file = (VirtualFile) iterator.next();
            model.addElement(file);
        }

        // nb: there seems to be no way to get the list of all file statuses in the 4.5.x openapi
        // I seem to recall that there is a getAllFileStatuses api in the 5.0 eap openapi?

        // there is, but it reports statuses from several vcs's ... how about displaying
        // counts of files in each status when configuring?

        log.debug("file statuses");

        for (Iterator iterator = statusMap.keySet().iterator(); iterator.hasNext();) {
            FileStatus status = (FileStatus) iterator.next();
            VirtualFileCounter counter = (VirtualFileCounter) statusMap.get(status);
            counter.log(status.getText());
        }

        log.debug("file types");

        for (Iterator iterator = typeMap.keySet().iterator(); iterator.hasNext();) {
            FileType type = (FileType) iterator.next();
            VirtualFileCounter counter = (VirtualFileCounter) typeMap.get(type);
            counter.log(type.getDescription());
        }

        log.debug("version control systems");

        for (Iterator iterator = vcsMap.keySet().iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            VirtualFileCounter counter = (VirtualFileCounter) vcsMap.get(name);
            counter.log(name);
        }

        log.debug("modules");

        for (Iterator iterator = moduleMap.keySet().iterator(); iterator.hasNext();) {
            Module module = (Module) iterator.next();
            VirtualFileCounter counter = (VirtualFileCounter) moduleMap.get(module);
            counter.log(module.getName());
        }

        ignoredFiles.log("ignored");
        sourceFiles.log("source");
        testFiles.log("test");

        long finish = System.currentTimeMillis();
        long delta = finish - start;

        WindowManager windowManager = WindowManager.getInstance();
        StatusBar statusBar = windowManager.getStatusBar(project);
        statusBar.setInfo("filter refreshed in " + delta + "ms; " +
            selected.size() + " selected files; " +
            allFiles.size() + " total files; " +
            sourceFiles.size() + " source files; " +
            testFiles.size() + " test files; " +
            ignoredFiles.size() + " ignored files; ");

        // TODO: decide how to do logical combination of selection based on the information above
        // i.e. if two statuses and two file types are selected for inclusion what is the expected result
        // clearly, logical or between selections of the same type (file type, file status, etc.)
        // but logical and/or between selections of different types might both be useful
        // i.e. these statuses AND those types
        //   vs these status OR those types

    }

    private class VirtualFileCounter {

        private List files = new ArrayList();

        public void count(VirtualFile file) {
            files.add(file);
        }

        public int size() {
            return files.size();
        }

        public List getFiles() {
            return files;
        }

        public void log(String type) {
            log.debug(files.size() + " " + type + " files");
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                VirtualFile file = (VirtualFile) iterator.next();
                log.debug(type + " " + file.getPath());
            }
        }
    }
}
