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
public interface FilterListener {

    public static FilterListener DEFAULT = new FilterListener() {
        public void status(String statusText, VirtualFile file) {}
        public void type(String typeName, VirtualFile file) {}
        public void vcs(String vcsName, VirtualFile file) {}
        public void module(String moduleName, VirtualFile file) {}
        public void other(String type, VirtualFile file) {}
    };

    public void status(String statusText, VirtualFile file);
    public void type(String typeName, VirtualFile file);
    public void vcs(String vcsName, VirtualFile file);
    public void module(String moduleName, VirtualFile file);
    public void other(String type, VirtualFile file);

}
