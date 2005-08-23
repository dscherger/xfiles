/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class FilterLogger {

    private Logger log = Logger.getInstance(getClass().getName());

    private VirtualFileListMap statusMap = new VirtualFileListMap("file statuses");
    private VirtualFileListMap typeMap = new VirtualFileListMap("file types");
    private VirtualFileListMap vcsMap = new VirtualFileListMap("version control systems");
    private VirtualFileListMap moduleMap = new VirtualFileListMap("modules");
    private VirtualFileListMap matchMap = new VirtualFileListMap("matched globs");
    private VirtualFileListMap mismatchMap = new VirtualFileListMap("mismatched globs");
    private VirtualFileListMap otherMap = new VirtualFileListMap("others");

    public void logStatus(String statusText, VirtualFile file) {
        statusMap.put(statusText, file);
    }

    public void logType(String typeName, VirtualFile file) {
        typeMap.put(typeName, file);
    }

    public void logVcs(String vcsName, VirtualFile file) {
        vcsMap.put(vcsName, file);
    }

    public void logModule(String moduleName, VirtualFile file) {
        moduleMap.put(moduleName, file);
    }

    public void logMatch(String pattern, VirtualFile file) {
        matchMap.put(pattern, file);
    }

    public void logMismatch(String pattern, VirtualFile file) {
        mismatchMap.put(pattern, file);
    }

    public void logOther(String type, VirtualFile file) {
        otherMap.put(type, file);
    }

    public void log() {
        /*
        statusMap.log();
        typeMap.log();
        vcsMap.log();
        moduleMap.log();
        matchMap.log();
        mismatchMap.log();
        otherMap.log();
        */
    }

    public String toString() {
        return
            otherMap.getCount(XFilesVirtualFileFilter.FILE) + " files; " +
            otherMap.getCount(XFilesVirtualFileFilter.DIRECTORY) + " directories; " +
            otherMap.getCount(XFilesVirtualFileFilter.IGNORED) + " ignored; " +
            otherMap.getCount(XFilesVirtualFileFilter.SOURCE) + " sources; " +
            otherMap.getCount(XFilesVirtualFileFilter.TEST) + " tests; " +
            otherMap.getCount(XFilesVirtualFileFilter.OPEN) + " open; " +
            otherMap.getCount(XFilesVirtualFileFilter.ACCEPTED) + " accepted; " +
            otherMap.getCount(XFilesVirtualFileFilter.REJECTED) + " rejected;";
    }

    private class VirtualFileList {

        private String name;

        private List files = new ArrayList();

        public VirtualFileList(String name) {
            this.name = name;
        }

        public void add(VirtualFile file) {
            files.add(file);
        }

        public int getCount() {
            return files.size();
        }

        /*
        public void log() {
            log.debug(files.size() + " " + name);
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                VirtualFile file = (VirtualFile) iterator.next();
                log.debug(name + " " + file.getPath());
            }
        }
        */
    }

    private class VirtualFileListMap {

        private String name;
        private Map map = new HashMap();

        public VirtualFileListMap(String name) {
            this.name = name;
        }

        public VirtualFileList get(String key) {
            VirtualFileList list = (VirtualFileList) map.get(key);
            if (list == null) {
                list = new VirtualFileList(key);
                map.put(key, list);
            }
            return list;
        }

        public int getCount(String key) {
            VirtualFileList list = (VirtualFileList) map.get(key);
            if (list == null) {
                return 0;
            } else {
                return list.getCount();
            }
        }

        public void put(String key, VirtualFile file) {
            VirtualFileList list = get(key);
            list.add(file);
        }

        /*
        public void log() {
            log.debug(name);

            for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                VirtualFileList list = (VirtualFileList) map.get(key);
                list.log();
            }

        }
        */
    }


}
