/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleRootManager;
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

    public boolean displayTextInToolbar() {
        return true;
    }

    public void actionPerformed(AnActionEvent event) {
        log.debug("actionPerformed");

        Project project = (Project) event.getDataContext().getData(DataConstants.PROJECT);

        // various index methods that look interesting and possibly relevant
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        final FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        final ProjectFileIndex index = projectRootManager.getFileIndex();
        final FileStatusManager fileStatusManager = FileStatusManager.getInstance(project);

        VirtualFile[] files = fileEditorManager.getOpenFiles();

        for (int i = 0; i < files.length; i++) {
            VirtualFile file = files[i];
            log.debug("open file " + file.getPath());
        }

        FileType[] registeredFileTypes = fileTypeManager.getRegisteredFileTypes();

        for (int i = 0; i < registeredFileTypes.length; i++) {
            FileType type = registeredFileTypes[i];
            log.debug("file type " + i + " is " + type.getName() + "; " + type.getDescription());
        }

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getModules();

        for (int i = 0; i < modules.length; i++) {
            Module module = modules[i];
            log.debug("module " + i + " is " + module.getName());
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            VirtualFile[] roots = moduleRootManager.getContentRoots();
            for (int j = 0; j < roots.length; j++) {
                VirtualFile root = roots[j];
                log.debug(module.getName() + " root " + j + " is " + root.getPath());
            }

            //ModuleFileIndex moduleIndex = moduleRootManager.getFileIndex();
        }

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
        final List selected = new ArrayList();

        // TODO: perhaps one iterator over everything that collects details and then filter displayed
        // things from this list?


        ContentIterator content = new ContentIterator() {
            public boolean processFile(VirtualFile file) {
                if (fileStatusManager != null) {
                    //String node = file.isDirectory() ? "directory " : "file ";

                    FileStatus status = fileStatusManager.getStatus(file);
                    FileType type = fileTypeManager.getFileTypeByFile(file);
                    statusMap.put(status.getText(), status);

                    /*
                    log.debug(status.getText() + " " +
                              node + file.getPath() +
                              " java " + index.isJavaSourceFile(file) +
                              " open " + fileEditorManager.isFileOpen(file));
                    */

                    //VirtualFileIndexEntry entry = VirtualFileIndexEntry.getIndexEntry(file, index);

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

                    // TODO: consult a filter to decide whether we add this file or not
                    // filter defintion needs to contain
                    // - file status: selected/all
                    // - editor state: open/closed/all
                    // - module: selected/all
                    // - file type: java/non-java/all
                    // - file name glob:
                    // - compiler status: ok/errors/all

                    // this is monotone specific
                    String statusCode = status.toString();
                    if (!file.isDirectory() &&
                        !statusCode.equals("UNCHANGED") &&
                        !statusCode.equals("EXTERNAL"))
                    {
                        VirtualFileAdapter adapter = new VirtualFileAdapter(file, status, type);
                        selected.add(adapter);
                    }
                }
                return true;
            }
        };

        VirtualFile[] roots = projectRootManager.getContentRoots();

        log.debug("iterating content" +
                  " under roots");

        for (int i = 0; i < roots.length; i++) {
            VirtualFile root = roots[i];
            log.debug("root " + root.getPath() + " isInContent " + index.isInContent(root));

            index.iterateContentUnderDirectory(root, content);
        }

        // TODO: FilterConfigurationPanel to edit configurations
        // TODO: FilterConfiguration to store individual configurations
        // TODO: XFilesFilter that uses selected FilterConfiguration to filter files
        // TODO: XFilesConfiguration class to hold FilterConfigurations and selected filter

        // TODO: the collected list should be run through the selected filter which should
        // define the sort order explicitly
        Collections.sort(selected);

        model.clear();
        for (Iterator iterator = selected.iterator(); iterator.hasNext();) {
            VirtualFileAdapter adapter = (VirtualFileAdapter) iterator.next();
            model.addElement(adapter);
        }

        // nb: there seems to be no way to get the list of all file statuses in the 4.5.x openapi
        // I seem to recall that there is a getAllFileStatuses api in the 5.0 eap openapi?

        log.debug("available file statuses");

        for (Iterator iterator = statusMap.keySet().iterator(); iterator.hasNext();) {
            String text = (String) iterator.next();
            FileStatus status = (FileStatus) statusMap.get(text);
            log.debug("status " + text + " is " + status);

        }

        // this set of file statuses should be persisted in the workspace file (.iws)
        // and only flushed when requested or something so that we can accumulate the
        // complete list?!? although this is rather lame.

    }
}
