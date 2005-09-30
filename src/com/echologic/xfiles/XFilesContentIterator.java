/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.ArrayList;
import java.util.List;

import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesContentIterator implements ContentIterator {

    private Logger log = Logger.getInstance(getClass().getName());

    private Project project;

    private XFilesVirtualFileFilter filter;

    private List included = new ArrayList();
    private List excluded = new ArrayList();

    public XFilesContentIterator(Project project) {
        this.project = project;
    }

    public void setFilter(XFilesVirtualFileFilter filter) {
        this.filter = filter;
    }

    public void iterate() {

        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        ProjectFileIndex index = projectRootManager.getFileIndex();

        VirtualFile[] roots = projectRootManager.getContentRoots();

        log.debug("iterating content under roots with filter " + filter.getName());
        filter.logConfiguration();
        
        for (int i = 0; i < roots.length; i++) {
            VirtualFile root = roots[i];
            log.debug("root " + root.getPath());
            index.iterateContentUnderDirectory(root, this);
        }

    }

    public boolean processFile(VirtualFile file) {
        if (filter.accept(file)) {
            included.add(file);
        } else {
            excluded.add(file);
        }

        return true;
    }

    public List getIncluded() {
        return included;
    }

    public List getExcluded() {
        return excluded;
    }

    public String toString() {
        return included.size() + " included; " + excluded.size() + " excluded; ";
    }
}
