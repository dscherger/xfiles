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

    public VirtualFileAdapter(VirtualFile file, FileStatus status) {
        this.file = file;
        this.status = status;
    }

    public VirtualFile getFile() {
        return file;
    }

    public FileStatus getStatus() {
        return status;
    }

    public String toString() {
        return status.toString() + " " + file.getName();
    }

}
