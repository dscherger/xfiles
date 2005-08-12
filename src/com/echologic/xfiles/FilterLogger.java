/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;

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

    private VirtualFileList files = new VirtualFileList("files");
    private VirtualFileList directories = new VirtualFileList("directories");
    private VirtualFileList ignored = new VirtualFileList("ignored");
    private VirtualFileList sources = new VirtualFileList("sources");
    private VirtualFileList tests = new VirtualFileList("tests");
    private VirtualFileList open = new VirtualFileList("open");
    private VirtualFileList accepted = new VirtualFileList("accepted");
    private VirtualFileList rejected = new VirtualFileList("rejected");


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

    public void logIgnored(VirtualFile file) {
        ignored.add(file);
    }

    public void logTest(VirtualFile file) {
        tests.add(file);
    }

    public void logSource(VirtualFile file) {
        sources.add(file);
    }

    public void logDirectory(VirtualFile file) {
        directories.add(file);
    }

    public void logFile(VirtualFile file) {
        files.add(file);
    }

    public void logOpen(VirtualFile file) {
        open.add(file);
    }

    public void logAccepted(VirtualFile file) {
        accepted.add(file);
    }

    public void logRejected(VirtualFile file) {
        rejected.add(file);
    }

    public void log() {
        statusMap.log();
        typeMap.log();
        vcsMap.log();
        moduleMap.log();
        matchMap.log();
        mismatchMap.log();

        files.log();
        directories.log();
        ignored.log();
        sources.log();
        tests.log();
        open.log();
        accepted.log();
        rejected.log();
    }

    public String toString() {
        return files.getCount() + " files; " +
            directories.getCount() + " directories; " +
            ignored.getCount() + " ignored; " +
            sources.getCount() + " sources; " +
            tests.getCount() + " tests; " +
            open.getCount() + " open; " +
            accepted.getCount() + " accepted; " +
            rejected.getCount() + " rejected;";
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

        public void log() {
            log.debug(files.size() + " " + name);
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                VirtualFile file = (VirtualFile) iterator.next();
                log.debug(name + " " + file.getPath());
            }
        }
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

        public void put(String key, VirtualFile file) {
            VirtualFileList list = get(key);
            list.add(file);
        }

        public void log() {
            log.debug(name);

            for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                VirtualFileList list = (VirtualFileList) map.get(key);
                list.log();
            }

        }
    }


}
