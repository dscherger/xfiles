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

    public static final String IGNORED = "ignored";
    public static final String SOURCE= "source";
    public static final String TEST = "test";
    public static final String DIRECTORY = "directory";
    public static final String FILE = "file";
    public static final String OPEN = "open";
    public static final String ACCEPTED = "accepted";
    public static final String REJECTED = "rejected";

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
    private List acceptedOthers;

    private GlobCompiler compiler = new GlobCompiler();
    private Perl5Matcher matcher = new Perl5Matcher();

    private FilterLogger logger;

    public XFilesVirtualFileFilter(Project project) {
        vcsManager = ProjectLevelVcsManager.getInstance(project);

        statusManager = FileStatusManager.getInstance(project);
        editorManager = FileEditorManager.getInstance(project);

        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        fileIndex = rootManager.getFileIndex();
    }

    public void setConfiguration(XFilesFilterConfiguration configuration) {
        name = configuration.NAME;

        acceptedStatusNames = configuration.ACCEPTED_STATUS_NAMES;
        acceptedTypeNames = configuration.ACCEPTED_TYPE_NAMES;
        acceptedVcsNames = configuration.ACCEPTED_VCS_NAMES;
        acceptedModuleNames = configuration.ACCEPTED_MODULE_NAMES;
        acceptedNameGlobs = compileAcceptedNameGlobs(configuration.ACCEPTED_NAME_GLOBS);
        acceptedOthers = configuration.ACCEPTED_OTHERS;
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

        Acceptor acceptor = new AnyAcceptor();

        FileStatus status = statusManager.getStatus(file);
        String statusText = status.getText();
        acceptor.update(acceptedStatusNames.contains(statusText));
        if (logger != null) logger.logStatus(statusText, file);

        FileType type = file.getFileType();
        String typeName = type.getName();
        acceptor.update(acceptedTypeNames.contains(typeName));
        if (logger != null) logger.logType(typeName, file);

        AbstractVcs vcs = vcsManager.getVcsFor(file);
        String vcsName = "<None>";
        if (vcs != null) vcsName = vcs.getName();
        acceptor.update(acceptedVcsNames.contains(vcsName));
        if (logger != null) logger.logVcs(vcsName, file);

        Module module = fileIndex.getModuleForFile(file);
        String moduleName = "<None>";
        if (module != null) moduleName = module.getName();
        acceptor.update(acceptedModuleNames.contains(moduleName));
        if (logger != null) logger.logModule(moduleName, file);

        if (fileIndex.isIgnored(file)) {
            acceptor.update(acceptedOthers.contains(IGNORED));
            if (logger != null) logger.logOther(IGNORED, file);
        }

        // note that SourceContent is a superset TestSourceContent

        if (fileIndex.isInTestSourceContent(file)) {
            acceptor.update(acceptedOthers.contains(TEST));
            if (logger != null) logger.logOther(TEST, file);
        } else if (fileIndex.isInSourceContent(file)) {
            acceptor.update(acceptedOthers.contains(SOURCE));
            if (logger != null) logger.logOther(SOURCE, file);
        }

        if (file.isDirectory()) {
            acceptor.update(acceptedOthers.contains(DIRECTORY));
            if (logger != null) logger.logOther(DIRECTORY, file);
        } else {
            acceptor.update(acceptedOthers.contains(FILE));
            if (logger != null) logger.logOther(FILE, file);
        }

        if (editorManager.isFileOpen(file)) {
            acceptor.update(acceptedOthers.contains(OPEN));
            if (logger != null) logger.logOther(OPEN, file);
        }

        String path = file.getPath();
        for (Iterator iterator = acceptedNameGlobs.iterator(); !acceptor.isAccepted() && iterator.hasNext();) {
            Pattern pattern = (Pattern) iterator.next();
            boolean matched = matcher.contains(path, pattern);
            acceptor.update(matched);
            if (matched) {
                if (logger != null) logger.logMatch(pattern.getPattern(), file);
            } else {
                if (logger != null) logger.logMismatch(pattern.getPattern(), file);
            }
        }

        if (acceptor.isAccepted()) {
            if (logger != null) logger.logOther(ACCEPTED, file);
            return true;
        } else {
            if (logger != null) logger.logOther(REJECTED, file);
            return false;
        }
    }

    private abstract class Acceptor {

        protected boolean accepted;

        public abstract void update(boolean b);

        public boolean isAccepted() {
            return accepted;
        }
    }

    private class AllAcceptor extends Acceptor {

        public AllAcceptor() {
            accepted = true;
        }

        public void update(boolean b) {
            accepted &= b;
        }
    }

    private class AnyAcceptor extends Acceptor {

        public AnyAcceptor() {
            accepted = false;
        }

        public void update(boolean b) {
            accepted |= b;
        }
    }

}
