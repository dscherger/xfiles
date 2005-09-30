/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

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

    // TODO: are these better in FilterListener?!?
    // hmm... it seems not, the listener simply has a generic other category
    // which can be used by clients for anything they like

    public static final String IGNORED = "ignored";
    public static final String SOURCE= "source";
    public static final String TEST = "test";
    public static final String OPEN = "open";

    private Logger log = Logger.getInstance(getClass().getName());

    private String name;

    private ProjectLevelVcsManager vcsManager;
    private ProjectFileIndex fileIndex;
    private FileStatusManager statusManager;
    private FileEditorManager editorManager;

    private List acceptedStatusNames = Collections.EMPTY_LIST;
    private List acceptedTypeNames = Collections.EMPTY_LIST;
    private List acceptedVcsNames = Collections.EMPTY_LIST;
    private List acceptedModuleNames = Collections.EMPTY_LIST;
    private List acceptedNameGlobs = Collections.EMPTY_LIST;
    private List acceptedOthers = Collections.EMPTY_LIST;

    private GlobCompiler compiler = new GlobCompiler();
    private Perl5Matcher matcher = new Perl5Matcher();

    private FilterListener listener = FilterListener.DEFAULT;

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

    public void logConfiguration() {
        log.debug("status " + acceptedStatusNames);
        log.debug("type " + acceptedTypeNames);
        log.debug("vcs " + acceptedVcsNames);
        log.debug("module " + acceptedModuleNames);
        //log.debug("glob " + acceptedNameGlobs);
        log.debug("other " + acceptedOthers);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setListener(CountingFilterListener listener) {
        this.listener = listener;
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

        if (file.isDirectory()) return false;

        Acceptor acceptor = new AnyAcceptor();

        FileStatus status = statusManager.getStatus(file);
        String statusText = status.getText();
        acceptor.update(acceptedStatusNames.contains(statusText));
        listener.status(statusText, file);

        FileType type = file.getFileType();
        String typeDescription = type.getDescription();
        acceptor.update(acceptedTypeNames.contains(typeDescription));
        listener.type(typeDescription, file);

        AbstractVcs vcs = vcsManager.getVcsFor(file);
        String vcsName = "<None>";
        if (vcs != null) vcsName = vcs.getName();
        acceptor.update(acceptedVcsNames.contains(vcsName));
        listener.vcs(vcsName, file);

        Module module = fileIndex.getModuleForFile(file);
        String moduleName = "<None>";
        if (module != null) moduleName = module.getName();
        acceptor.update(acceptedModuleNames.contains(moduleName));
        listener.module(moduleName, file);

        if (fileIndex.isIgnored(file)) {
            acceptor.update(acceptedOthers.contains(IGNORED));
            listener.other(IGNORED, file);
        }

        // note that SourceContent is a superset TestSourceContent

        if (fileIndex.isInTestSourceContent(file)) {
            acceptor.update(acceptedOthers.contains(TEST));
            listener.other(TEST, file);
        } else if (fileIndex.isInSourceContent(file)) {
            acceptor.update(acceptedOthers.contains(SOURCE));
            listener.other(SOURCE, file);
        }

        if (editorManager.isFileOpen(file)) {
            acceptor.update(acceptedOthers.contains(OPEN));
            listener.other(OPEN, file);
        }

        String path = file.getPath();
        for (Iterator iterator = acceptedNameGlobs.iterator(); !acceptor.isAccepted() && iterator.hasNext();) {
            Pattern pattern = (Pattern) iterator.next();
            boolean matched = matcher.contains(path, pattern);
            acceptor.update(matched);
            /*
            if (matched) {
                listener.match(pattern.getPattern(), file);
            }
            */
        }

        return acceptor.isAccepted();
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
