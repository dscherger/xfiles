/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    private ProjectLevelVcsManager vcsManager;
    private ProjectFileIndex fileIndex;
    private FileStatusManager statusManager;
    private FileEditorManager editorManager;

    // each of the following need to be listed in their
    // own table under a heading describing the table contents
    // selected, name, count

    private List acceptedStatusNames;
    private List acceptedTypeNames;
    private List acceptedVcsNames;
    private List acceptedModuleNames;
    private List acceptedNameGlobs;

    private GlobCompiler compiler = new GlobCompiler();
    private Perl5Matcher matcher = new Perl5Matcher();

    // all of these could be listed in one table under the
    // "other" heading again with selected, name, count
    
    private boolean acceptIgnoredFiles;
    private boolean acceptSourceFiles;
    private boolean acceptTestFiles;
    private boolean acceptFiles;
    private boolean acceptDirectories;
    private boolean acceptOpenFiles;

    private FilterLogger logger;

    public XFilesVirtualFileFilter(Project project) {
        vcsManager = ProjectLevelVcsManager.getInstance(project);

        statusManager = FileStatusManager.getInstance(project);
        editorManager = FileEditorManager.getInstance(project);

        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
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

    public void setLogger(FilterLogger logger) {
        this.logger = logger;
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
        if (logger != null) logger.logStatus(statusText, file);

        FileType type = file.getFileType();
        String typeName = type.getName();
        accepted |= acceptedTypeNames.contains(typeName);
        if (logger != null) logger.logType(typeName, file);

        AbstractVcs vcs = vcsManager.getVcsFor(file);
        String vcsName = "<None>";
        if (vcs != null) vcsName = vcs.getName();
        accepted |= acceptedVcsNames.contains(vcsName);
        if (logger != null) logger.logVcs(vcsName, file);

        Module module = fileIndex.getModuleForFile(file);
        String moduleName = "<None>";
        if (module != null) moduleName = module.getName();
        accepted |= acceptedModuleNames.contains(moduleName);
        if (logger != null) logger.logModule(moduleName, file);

        if (fileIndex.isIgnored(file)) {
            accepted |= acceptIgnoredFiles;
            if (logger != null) logger.logIgnored(file);
        }

        // note that SourceContent is a superset TestSourceContent

        if (fileIndex.isInTestSourceContent(file)) {
            accepted |= acceptTestFiles;
            if (logger != null) logger.logTest(file);
        } else if (fileIndex.isInSourceContent(file)) {
            accepted |= acceptSourceFiles;
            if (logger != null) logger.logSource(file);
        }

        if (file.isDirectory()) {
            accepted |= acceptDirectories;
            if (logger != null) logger.logDirectory(file);
        } else {
            accepted |= acceptFiles;
            if (logger != null) logger.logFile(file);
        }

        if (editorManager.isFileOpen(file)) {
            accepted |= acceptOpenFiles;
            if (logger != null) logger.logOpen(file);
        }

        String path = file.getPath();
        for (Iterator iterator = acceptedNameGlobs.iterator(); !accepted && iterator.hasNext();) {
            Pattern pattern = (Pattern) iterator.next();
            boolean matched = matcher.contains(path, pattern);
            accepted |= matched;
            if (matched) {
                if (logger != null) logger.logMatch(pattern.getPattern(), file);
            } else {
                if (logger != null) logger.logMismatch(pattern.getPattern(), file);
            }
        }

        if (accepted) {
            if (logger != null) logger.logAccepted(file);
        } else {
            if (logger != null) logger.logRejected(file);
        }
        
        return accepted;
    }

}
