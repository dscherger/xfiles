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

    private String name;

    private ProjectRootManager rootManager;
    private ProjectLevelVcsManager vcsManager;
    private ProjectFileIndex fileIndex;
    private FileStatusManager statusManager;
    private FileEditorManager editorManager;

    private List acceptedStatusNames;
    private List acceptedTypeNames;
    private List acceptedVcsNames;
    private List acceptedModuleNames;
    private List acceptedNameGlobs;

    private GlobCompiler compiler = new GlobCompiler();
    private Perl5Matcher matcher = new Perl5Matcher();

    private boolean acceptIgnoredFiles;
    private boolean acceptSourceFiles;
    private boolean acceptTestFiles;
    private boolean acceptFiles;
    private boolean acceptDirectories;
    private boolean acceptOpenFiles;

    // TODO: move these to a listener class that the filter can update as it runs
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

    public void setConfiguration(XFilesFilterConfiguration configuration) {
        name = configuration.FILTER_NAME;

        acceptedStatusNames = configuration.ACCEPTED_STATUS_NAMES;
        acceptedTypeNames = configuration.ACCEPTED_TYPE_NAMES;
        acceptedVcsNames = configuration.ACCEPTED_VCS_NAMES;
        acceptedModuleNames = configuration.ACCEPTED_MODULE_NAMES;
        acceptedNameGlobs = compileAcceptedNameGlobs(configuration.ACCEPTED_NAME_GLOBS);

        acceptIgnoredFiles = configuration.ACCEPT_IGNORED_FILES;
        acceptSourceFiles = configuration.ACCEPT_SOURCE_FILES;
        acceptTestFiles = configuration.ACCEPT_TEST_FILES;
        acceptFiles = configuration.ACCEPT_FILES;
        acceptDirectories = configuration.ACCEPT_DIRECTORIES;
        acceptOpenFiles = configuration.ACCEPT_OPEN_FILES;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List compileAcceptedNameGlobs(List globs) {
        ArrayList list = new ArrayList();
        for (Iterator iterator = globs.iterator(); iterator.hasNext();) {
            String glob = (String) iterator.next();
            try {
                    Pattern pattern = compiler.compile(glob);
                    log.debug("compiled glob " + glob + " to pattern " + pattern.getPattern());
                    list.add(pattern);
            } catch (MalformedPatternException e) {
                throw new RuntimeException("bad glob " + glob, e);
            }
        }
        return list;
    }

    public boolean accept(VirtualFile file) {

        boolean accepted = false;

        FileStatus status = statusManager.getStatus(file);
        String statusText = status.getText();
        accepted |= acceptedStatusNames.contains(statusText);
        statusMap.count(statusText, file);

        FileType type = file.getFileType();
        String typeName = type.getName();
        accepted |= acceptedTypeNames.contains(typeName);
        typeMap.count(typeName, file);

        AbstractVcs vcs = vcsManager.getVcsFor(file);
        String vcsName = "<None>";
        if (vcs != null) vcsName = vcs.getName();
        accepted |= acceptedVcsNames.contains(vcsName);
        vcsMap.count(vcsName, file);

        Module module = fileIndex.getModuleForFile(file);
        String moduleName = "<None>";
        if (module != null) moduleName = module.getName();
        accepted |= acceptedModuleNames.contains(moduleName);

        moduleMap.count(moduleName, file);

        if (fileIndex.isIgnored(file)) {
            ignored.count(file);
            accepted |= acceptIgnoredFiles;
        }

        // note that SourceContent is a superset TestSourceContent

        if (fileIndex.isInTestSourceContent(file)) {
            tests.count(file);
            accepted |= acceptTestFiles;
        } else if (fileIndex.isInSourceContent(file)) {
            sources.count(file);
            accepted |= acceptSourceFiles;
        }

        if (file.isDirectory()) {
            accepted |= acceptDirectories;
            directories.count(file);
        } else {
            accepted |= acceptFiles;
            files.count(file);
        }

        if (editorManager.isFileOpen(file)) {
            accepted |= acceptOpenFiles;
            open.count(file);
        }

        if (!accepted) {
            String path = file.getPath();
            for (Iterator iterator = acceptedNameGlobs.iterator(); !accepted && iterator.hasNext();) {
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
