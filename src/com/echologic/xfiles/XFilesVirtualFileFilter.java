/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;

import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesVirtualFileFilter implements VirtualFileFilter {

    private Logger log = Logger.getInstance(getClass().getName());

    private ProjectRootManager rootManager;
    private ProjectLevelVcsManager vcsManager;
    private ProjectFileIndex fileIndex;
    private FileStatusManager statusManager;
    private FileEditorManager editorManager;

    private List acceptedStatusList = new ArrayList();
    private List acceptedTypeList = new ArrayList();
    private List acceptedVcsList = new ArrayList();
    private List acceptedModuleList = new ArrayList();
    private List acceptedGlobList = new ArrayList();

    private GlobCompiler compiler = new GlobCompiler();
    private Perl5Matcher matcher = new Perl5Matcher();

    private boolean acceptIgnored;
    private boolean acceptSources;
    private boolean acceptTests;
    private boolean acceptFiles;
    private boolean acceptDirectories;
    private boolean acceptOpen;

    // available statuses, types, vcs's and modules

    private VirtualFileCounterMap statusMap = new VirtualFileCounterMap("file statuses");
    private VirtualFileCounterMap typeMap = new VirtualFileCounterMap("file types");
    private VirtualFileCounterMap vcsMap = new VirtualFileCounterMap("version control systems");
    private VirtualFileCounterMap moduleMap = new VirtualFileCounterMap("modules");

    private VirtualFileCounter files = new VirtualFileCounter("files");
    private VirtualFileCounter directories = new VirtualFileCounter("directories");
    private VirtualFileCounter ignored = new VirtualFileCounter("ignored");
    private VirtualFileCounter sources = new VirtualFileCounter("sources");
    private VirtualFileCounter tests = new VirtualFileCounter("tests");
    private VirtualFileCounter open = new VirtualFileCounter("open");

    public XFilesVirtualFileFilter(Project project) {
        rootManager = ProjectRootManager.getInstance(project);
        vcsManager = ProjectLevelVcsManager.getInstance(project);

        statusManager = FileStatusManager.getInstance(project);
        editorManager = FileEditorManager.getInstance(project);

        fileIndex = rootManager.getFileIndex();
    }

    public void addAcceptedStatus(String status) {
        acceptedStatusList.add(status);
    }

    public void addAcceptedType(String type) {
        acceptedTypeList.add(type);
    }

    public void addAcceptedVcs(String vcs) {
        acceptedVcsList.add(vcs);
    }

    public void addAcceptedModule(String module) {
        acceptedModuleList.add(module);
    }

    public void addAcceptedGlob(String glob) {
        try {
            Pattern pattern = compiler.compile(glob);
            log.debug("compiled glob " + glob + " to pattern " + pattern.getPattern());
            acceptedGlobList.add(pattern);
        } catch (MalformedPatternException e) {
            throw new RuntimeException("bad glob " + glob, e);
        }
    }

    public void setAcceptIgnored(boolean b) {
        acceptIgnored = b;
    }

    public void setAcceptSources(boolean b) {
        acceptSources = b;
    }

    public void setAcceptTests(boolean b) {
        acceptTests = b;
    }

    public void setAcceptFiles(boolean b) {
        acceptFiles = b;
    }

    public void setAcceptDirectories(boolean b) {
        acceptDirectories = b;
    }

    public void setAcceptOpen(boolean b) {
        acceptOpen = b;
    }

    public boolean accept(VirtualFile file) {

        boolean accepted = false;

        FileStatus status = statusManager.getStatus(file);
        String statusText = status.getText();
        accepted |= acceptedStatusList.contains(statusText);
        statusMap.count(statusText, file);

        FileType type = file.getFileType();
        String typeName = type.getName();
        accepted |= acceptedTypeList.contains(typeName);
        typeMap.count(typeName, file);

        AbstractVcs vcs = vcsManager.getVcsFor(file);
        String vcsName = "<None>";
        if (vcs != null) vcsName = vcs.getName();
        accepted |= acceptedVcsList.contains(vcsName);
        vcsMap.count(vcsName, file);

        Module module = fileIndex.getModuleForFile(file);
        String moduleName = module.getName();
        accepted |= acceptedModuleList.contains(moduleName);

        moduleMap.count(moduleName, file);

        if (fileIndex.isIgnored(file)) {
            ignored.count(file);
            accepted |= acceptIgnored;
        }

        // note that SourceContent is a superset TestSourceContent

        if (fileIndex.isInTestSourceContent(file)) {
            tests.count(file);
            accepted |= acceptTests;
        } else if (fileIndex.isInSourceContent(file)) {
            sources.count(file);
            accepted |= acceptSources;
        }

        if (file.isDirectory()) {
            accepted |= acceptDirectories;
            directories.count(file);
        } else {
            accepted |= acceptFiles;
            files.count(file);
        }

        if (editorManager.isFileOpen(file)) {
            accepted |= acceptOpen;
            open.count(file);
        }

        if (!accepted) {
            String path = file.getPath();
            for (Iterator iterator = acceptedGlobList.iterator(); !accepted && iterator.hasNext();) {
                Pattern pattern = (Pattern) iterator.next();
                accepted |= matcher.contains(path, pattern);
            }
        }

        return accepted;
    }

    public void log() {
        statusMap.log();
        typeMap.log();
        vcsMap.log();
        moduleMap.log();
        files.log();
        directories.log();
        ignored.log();
        sources.log();
        tests.log();
        open.log();
    }

    public String toString() {
        log();

        return files.getCount() + " files; " +
            directories.getCount() + " directories; " +
            ignored.getCount() + " ignored; " +
            sources.getCount() + " sources; " +
            tests.getCount() + " tests; " +
            open.getCount() + " open;";
    }

    private class VirtualFileCounter {

        private String name;

        private List files = new ArrayList();

        public VirtualFileCounter(String name) {
            this.name = name;
        }

        public void count(VirtualFile file) {
            files.add(file);
        }

        public int getCount() {
            return files.size();
        }

        public void log() {
            log.debug(files.size() + " " + name + " files");
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                VirtualFile file = (VirtualFile) iterator.next();
                log.debug(name + " " + file.getPath());
            }
        }
    }

    private class VirtualFileCounterMap {

        private String name;
        private Map map = new HashMap();

        public VirtualFileCounterMap(String name) {
            this.name = name;
        }

        public VirtualFileCounter get(String key) {
            VirtualFileCounter counter = (VirtualFileCounter) map.get(key);
            if (counter == null) {
                counter = new VirtualFileCounter(key);
                map.put(key, counter);
            }
            return counter;
        }

        public void count(String key, VirtualFile file) {
            VirtualFileCounter counter = get(key);
            counter.count(file);
        }

        public void log() {
            log.debug(name);

            for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                VirtualFileCounter counter = (VirtualFileCounter) map.get(key);
                counter.log();
            }

        }
    }

}
