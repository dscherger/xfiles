/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.fileTypes.FileType;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class VirtualFileAdapter implements Comparable {

    VirtualFile file;
    FileStatus status;
    FileType type;

    public VirtualFileAdapter(VirtualFile file, FileStatus status, FileType type) {
        this.file = file;
        this.status = status;
        this.type = type;
    }

    public VirtualFile getFile() {
        return file;
    }

    public FileStatus getStatus() {
        return status;
    }

    public FileType getType() {
        return type;
    }

    /**
     * Order first by file type and then by file name.
     *
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        VirtualFileAdapter that = (VirtualFileAdapter) o;
        int result = 0;
        if (result == 0) result = this.type.getName().compareTo(that.type.getName());
        if (result == 0) result = this.file.getPath().compareTo(that.file.getPath());
        return result;
    }

    public String toString() {
        return file.getName() + " [" + status.getText() + "]";
    }

}
