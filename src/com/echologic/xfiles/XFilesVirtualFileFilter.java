/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.ArrayList;
import java.util.Collections;
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

    private List<Pattern> acceptedPathNames = Collections.emptyList();
    private List<String> acceptedAttributeNames = Collections.emptyList();
    private List<String> acceptedStatusNames = Collections.emptyList();
    private List<String> acceptedTypeNames = Collections.emptyList();
    private List<String> acceptedVcsNames = Collections.emptyList();
    private List<String> acceptedModuleNames = Collections.emptyList();

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
        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (int i=0; i<patterns.size(); i++) {
            if (i > 0) buffer.append(", ");
            Pattern pattern = (Pattern) patterns.get(i);
            buffer.append(pattern.getPattern());
        }
        buffer.append("]");
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

    public List<Pattern> compileAcceptedNameGlobs(List<String> globs) {
        List<Pattern> list = new ArrayList<>();
        for (String glob : globs) {
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

        // TODO: AndCondition doesn't quite work with this as it is...
        // empty lists need to be excluded from condition evaluation or something

        log.debug("match " + file + " starts with " + condition.isTrue());

        FileStatus status = statusManager.getStatus(file);
        String statusText = status.getText();
        listener.status(statusText, file);

        if (!acceptedStatusNames.isEmpty()) {
            condition.evaluate(acceptedStatusNames.contains(statusText));
        }

        FileType type = file.getFileType();
        String typeDescription = type.getDescription();
        listener.type(typeDescription, file);

        if (!acceptedTypeNames.isEmpty()) {
            condition.evaluate(acceptedTypeNames.contains(typeDescription));
        }

        AbstractVcs vcs = vcsManager.getVcsFor(file);
        String vcsName = "<None>";
        if (vcs != null) vcsName = vcs.getName();
        listener.vcs(vcsName, file);

        if (!acceptedVcsNames.isEmpty()) {
            condition.evaluate(acceptedVcsNames.contains(vcsName));
        }

        Module module = moduleManager.getModuleForFile(file);
        String moduleName = "<None>";
        if (module != null) moduleName = module.getName();
        listener.module(moduleName, file);

        if (!acceptedModuleNames.isEmpty()) {
            condition.evaluate(acceptedModuleNames.contains(moduleName));
        }

        String[] attributes = attributeManager.getAttributes(file);
        for (String attribute : attributes) {
            listener.attribute(attribute, file);

            if (!acceptedAttributeNames.isEmpty()) {
                condition.evaluate(acceptedAttributeNames.contains(attribute));
            }
        }

        log.debug("before patterns condition is " + condition.isTrue());

        String path = file.getPath();
        for (Pattern pattern : acceptedPathNames) {
            if (matcher.matches(path, pattern)) {
                condition.evaluate(true);
                listener.path(pattern.getPattern(), file);
                log.debug(pattern.getPattern() + " matched path " + path);
            } else {
                condition.evaluate(false);
                log.debug(pattern.getPattern() + " unmatched path " + path);
            }
        }

        log.debug("after patterns condition is " + condition.isTrue());

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

    public String toString() {
        return name;
    }
}
