/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class CountingFilterListener implements FilterListener {

    private VirtualFileCounterMap pathMap = new VirtualFileCounterMap("path pattern");
    private VirtualFileCounterMap attributeMap = new VirtualFileCounterMap("file attribute");
    private VirtualFileCounterMap statusMap = new VirtualFileCounterMap("file status");
    private VirtualFileCounterMap typeMap = new VirtualFileCounterMap("file type");
    private VirtualFileCounterMap vcsMap = new VirtualFileCounterMap("version control system");
    private VirtualFileCounterMap moduleMap = new VirtualFileCounterMap("module");

    public VirtualFileCounterMap getPathMap() {
        return pathMap;
    }

    public VirtualFileCounterMap getAttributeMap() {
        return attributeMap;
    }

    public VirtualFileCounterMap getStatusMap() {
        return statusMap;
    }

    public VirtualFileCounterMap getTypeMap() {
        return typeMap;
    }

    public VirtualFileCounterMap getVcsMap() {
        return vcsMap;
    }

    public VirtualFileCounterMap getModuleMap() {
        return moduleMap;
    }

    public void path(String pattern, VirtualFile file) {
        pathMap.count(pattern, file);
    }

    public void attribute(String type, VirtualFile file) {
        attributeMap.count(type, file);
    }

    public void status(String statusText, VirtualFile file) {
        statusMap.count(statusText, file);
    }

    public void type(String typeName, VirtualFile file) {
        typeMap.count(typeName, file);
    }

    public void vcs(String vcsName, VirtualFile file) {
        vcsMap.count(vcsName, file);
    }

    public void module(String moduleName, VirtualFile file) {
        moduleMap.count(moduleName, file);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (VirtualFileCounter counter : attributeMap.getCounters()) {
            buffer.append(counter.getCount()).append(" ");
            buffer.append(counter.getName()).append("; ");
        }
        return buffer.toString();
    }

    public void log() {
        pathMap.log();
        attributeMap.log();
        statusMap.log();
        typeMap.log();
        vcsMap.log();
        moduleMap.log();
    }

}
