/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class CountingFilterListener implements FilterListener {

    private Logger log = Logger.getInstance(getClass().getName());

    private VirtualFileCounterMap statusMap = new VirtualFileCounterMap("file status");
    private VirtualFileCounterMap typeMap = new VirtualFileCounterMap("file type");
    private VirtualFileCounterMap vcsMap = new VirtualFileCounterMap("version control system");
    private VirtualFileCounterMap moduleMap = new VirtualFileCounterMap("module");
    //private VirtualFileCounterMap matchMap = new VirtualFileCounterMap("matched pattern");
    private VirtualFileCounterMap otherMap = new VirtualFileCounterMap("miscellaneous");

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

    /*
    public VirtualFileCounterMap getMatchMap() {
        return matchMap;
    }
    */

    public VirtualFileCounterMap getOtherMap() {
        return otherMap;
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

    /*
    public void match(String pattern, VirtualFile file) {
        matchMap.count(pattern, file);
    }
    */

    public void other(String type, VirtualFile file) {
        otherMap.count(type, file);
    }

    // TODO: this indicates the other constants should live here or in FilterListener

    public String toString() {
        return
            otherMap.getCount(XFilesVirtualFileFilter.IGNORED) + " ignored; " +
            otherMap.getCount(XFilesVirtualFileFilter.SOURCE) + " sources; " +
            otherMap.getCount(XFilesVirtualFileFilter.TEST) + " tests; " +
            otherMap.getCount(XFilesVirtualFileFilter.OPEN) + " open; ";
    }

    public void log() {
        statusMap.log();
        typeMap.log();
        vcsMap.log();
        moduleMap.log();
        //matchMap.log();
        otherMap.log();
    }

}
