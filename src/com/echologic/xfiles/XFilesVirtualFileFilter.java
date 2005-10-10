/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.intellij.openapi.diagnostic.Logger;
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

    private ProjectLevelVcsManager vcsManager;
    private ProjectFileIndex moduleManager;
    private FileStatusManager statusManager;
    private FileAttributeManager attributeManager;

    private String name;

    private boolean matchAll;

    private List acceptedPathNames = Collections.EMPTY_LIST;
    private List acceptedAttributeNames = Collections.EMPTY_LIST;
    private List acceptedStatusNames = Collections.EMPTY_LIST;
    private List acceptedTypeNames = Collections.EMPTY_LIST;
    private List acceptedVcsNames = Collections.EMPTY_LIST;
    private List acceptedModuleNames = Collections.EMPTY_LIST;

    private GlobCompiler compiler = new GlobCompiler();
    private Perl5Matcher matcher = new Perl5Matcher();

    private FilterListener listener = FilterListener.DEFAULT;

    public XFilesVirtualFileFilter(Project project) {
        vcsManager = ProjectLevelVcsManager.getInstance(project);
        statusManager = FileStatusManager.getInstance(project);
        attributeManager = new FileAttributeManager(project); // TODO: getInstance

        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        moduleManager = rootManager.getFileIndex();
    }

    public void setConfiguration(XFilesFilterConfiguration configuration) {
        name = configuration.NAME;
        matchAll = configuration.MATCH_ALL;

        // TODO: it might be good to use a class that holds an array and matches against it for these

        acceptedPathNames = compileAcceptedNameGlobs(configuration.ACCEPTED_PATH_NAMES);
        acceptedAttributeNames = configuration.ACCEPTED_ATTRIBUTE_NAMES;
        acceptedStatusNames = configuration.ACCEPTED_STATUS_NAMES;
        acceptedTypeNames = configuration.ACCEPTED_TYPE_NAMES;
        acceptedVcsNames = configuration.ACCEPTED_VCS_NAMES;
        acceptedModuleNames = configuration.ACCEPTED_MODULE_NAMES;
    }

    public void logConfiguration() {
        log.debug("configuration " + name);
        log.debug("match " + (matchAll ? "all" : "any"));
        log.debug("path " + getPatternStrings(acceptedPathNames));
        log.debug("attribute " + acceptedAttributeNames);
        log.debug("status " + acceptedStatusNames);
        log.debug("type " + acceptedTypeNames);
        log.debug("vcs " + acceptedVcsNames);
        log.debug("module " + acceptedModuleNames);
    }

    private String getPatternStrings(List patterns) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (int i=0; i<patterns.size(); i++) {
            if (i > 0) buffer.append(", ");
            Pattern pattern = (Pattern) patterns.get(i);
            buffer.append(pattern.getPattern());
        }
        buffer.append("[");
        return buffer.toString();
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

        Condition condition;

        if (matchAll) {
            condition = new AndCondition();
        } else {
            condition = new OrCondition();
        }

        FileStatus status = statusManager.getStatus(file);
        String statusText = status.getText();
        condition.evaluate(acceptedStatusNames.contains(statusText));
        listener.status(statusText, file);

        FileType type = file.getFileType();
        String typeDescription = type.getDescription();
        condition.evaluate(acceptedTypeNames.contains(typeDescription));
        listener.type(typeDescription, file);

        AbstractVcs vcs = vcsManager.getVcsFor(file);
        String vcsName = "<None>";
        if (vcs != null) vcsName = vcs.getName();
        condition.evaluate(acceptedVcsNames.contains(vcsName));
        listener.vcs(vcsName, file);

        Module module = moduleManager.getModuleForFile(file);
        String moduleName = "<None>";
        if (module != null) moduleName = module.getName();
        condition.evaluate(acceptedModuleNames.contains(moduleName));
        listener.module(moduleName, file);

        String[] attributes = attributeManager.getAttributes(file);
        for (int i = 0; i < attributes.length; i++) {
            String attribute = attributes[i];
            condition.evaluate(acceptedAttributeNames.contains(attribute));
            listener.attribute(attribute, file);
        }

        String path = file.getPath();
        for (Iterator iterator = acceptedPathNames.iterator(); iterator.hasNext();) {
            Pattern pattern = (Pattern) iterator.next();
            if (matcher.matches(path, pattern)) {
                condition.evaluate(true);
                listener.path(pattern.getPattern(), file);
                log.debug(pattern.getPattern() + " matched path " + path);
            } else {
                condition.evaluate(false);
                log.debug(pattern.getPattern() + " unmatched path " + path);
            }
        }

        return condition.isTrue();
    }

    private abstract class Condition {

        protected boolean value;

        public abstract void evaluate(boolean b);

        public boolean isTrue() {
            return value;
        }
    }

    private class AndCondition extends Condition {

        public AndCondition() {
            value = true;
        }

        public void evaluate(boolean b) {
            value &= b;
        }
    }

    private class OrCondition extends Condition {

        public OrCondition() {
            value = false;
        }

        public void evaluate(boolean b) {
            value |= b;
        }
    }

}
