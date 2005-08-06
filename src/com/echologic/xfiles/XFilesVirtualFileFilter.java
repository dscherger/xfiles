/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;

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
    private boolean acceptDirectories;
    private boolean acceptOpen;

    // available statuses, types, vcs's and modules

    private ListMap statusMap = new ListMap();
    private ListMap typeMap = new ListMap();
    private ListMap vcsMap = new ListMap();
    private ListMap moduleMap = new ListMap();

    private List files = new ArrayList();
    private List directories = new ArrayList();
    private List ignored = new ArrayList();
    private List sources = new ArrayList();
    private List tests = new ArrayList();
    private List open = new ArrayList();

    public XFilesVirtualFileFilter(Project project) {
        rootManager = ProjectRootManager.getInstance(project);
        vcsManager = ProjectLevelVcsManager.getInstance(project);

        statusManager = FileStatusManager.getInstance(project);
        editorManager = FileEditorManager.getInstance(project);

        fileIndex = rootManager.getFileIndex();
    }

    public void addAcceptedStatus(FileStatus status) {
        acceptedStatusList.add(status);
    }

    public void addAcceptedType(FileType type) {
        acceptedTypeList.add(type);
    }

    public void addAcceptedVcs(AbstractVcs vcs) {
        acceptedVcsList.add(vcs);
    }

    public void addAcceptedModule(Module module) {
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

    public void setAcceptDirectories(boolean b) {
        acceptDirectories = b;
    }

    public void setAcceptOpen(boolean b) {
        acceptOpen = b;
    }

    public boolean accept(VirtualFile file) {

        boolean accepted = false;

        FileStatus status = statusManager.getStatus(file);
        accepted |= acceptedStatusList.contains(status);
        statusMap.add(status, file);

        FileType type = file.getFileType();
        accepted |= acceptedTypeList.contains(type);
        typeMap.add(type, file);

        AbstractVcs vcs = vcsManager.getVcsFor(file);
        accepted |= acceptedVcsList.contains(vcs);

        String vcsName = "<None>";
        if (vcs != null) vcsName = vcs.getName();
        vcsMap.add(vcsName, file);

        Module module = fileIndex.getModuleForFile(file);
        accepted |= acceptedModuleList.contains(module);

        moduleMap.add(module, file);

        if (fileIndex.isIgnored(file)) {
            ignored.add(file);
            accepted |= acceptIgnored;
        }

        // note that SourceContent is a superset TestSourceContent

        if (fileIndex.isInTestSourceContent(file)) {
            tests.add(file);
            accepted |= acceptTests;
        } else if (fileIndex.isInSourceContent(file)) {
            sources.add(file);
            accepted |= acceptSources;
        }

        if (file.isDirectory()) {
            accepted |= acceptDirectories;
            directories.add(file);
        } else {
            //accepted |= acceptFiles;
            files.add(file);
        }

        if (editorManager.isFileOpen(file)) {
            accepted |= acceptOpen;
            open.add(file);
        }

        if (!accepted) {
            String path = file.getPath();
            for (Iterator iterator = acceptedGlobList.iterator(); !accepted && iterator.hasNext();) {
                Pattern pattern = (Pattern) iterator.next();
                accepted |= matcher.contains(path, pattern);
            }
        }

        return accepted;

        // for name matches we can use reges or globs (allowing only * chars)
        // where foo*a*b*bar is evaluated with
        // startsWith("foo")
        // indexOf("a") > 0
        // indexOf("b") > indexOf("a")
        // endsWith("bar")
    }

    public void log() {
        log.debug("file statuses");

        for (Iterator iterator = statusMap.keySet().iterator(); iterator.hasNext();) {
            FileStatus status = (FileStatus) iterator.next();
            List list = statusMap.get(status);
            log(list, status.getText());
        }

        log.debug("file types");

        for (Iterator iterator = typeMap.keySet().iterator(); iterator.hasNext();) {
            FileType type = (FileType) iterator.next();
            List list = typeMap.get(type);
            log(list, type.getDescription());
        }

        log.debug("version control systems");

        for (Iterator iterator = vcsMap.keySet().iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            List list = vcsMap.get(name);
            log(list, name);
        }

        log.debug("modules");

        for (Iterator iterator = moduleMap.keySet().iterator(); iterator.hasNext();) {
            Module module = (Module) iterator.next();
            List list = moduleMap.get(module);
            log(list, module.getName());
        }

        log(ignored, "ignored");
        log(sources, "source");
        log(tests, "test");

    }

    public void log(List files, String type) {
        log.debug(files.size() + " " + type + " files");
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            VirtualFile file = (VirtualFile) iterator.next();
            log.debug(type + " " + file.getPath());
        }
    }

    public String toString() {
        log();

        return files.size() + " files; " +
            directories.size() + " directories; " +
            ignored.size() + " ignored; " +
            sources.size() + " sources; " +
            tests.size() + " tests; " +
            open.size() + " open;";
    }

    private class ListMap {

        private Map map = new HashMap();

        public List get(Object key) {
            List list = (List) map.get(key);
            if (list == null) {
                list = new ArrayList();
                map.put(key, list);
            }
            return list;
        }

        public void add(Object key, VirtualFile file) {
            List list = get(key);
            list.add(file);
        }

        public Set keySet() {
            return map.keySet();
        }
    }
}
