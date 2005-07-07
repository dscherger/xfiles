/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vcs.FileStatus;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class VirtualFileAdapter {

    VirtualFile file;
    FileStatus status;
    boolean open;

    public VirtualFileAdapter(VirtualFile file, FileStatus status, boolean open) {
        this.file = file;
        this.status = status;
        this.open = open;
    }

    public String toString() {
        return file.getName();
    }

}
