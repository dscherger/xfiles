/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class VirtualFileAdapter implements Comparable {

    private VirtualFile file;
    private FileType type;
    private String name;
    private String path;

    public VirtualFileAdapter(VirtualFile file) {
        this.file = file;
        this.type = file.getFileType();
        this.name = file.getName();
        this.path = file.getPath();
    }

    public VirtualFile getFile() {
        return file;
    }

    public FileType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public int compareTo(Object o) {
        VirtualFileAdapter that = (VirtualFileAdapter) o;

        int result = this.type.getName().compareTo(that.type.getName());

        if (result == 0) {
            // TODO: little endian sort on class names
            if (this.type == StdFileTypes.JAVA) {
                result = this.name.compareTo(that.name);
            } else {
                result = this.name.compareTo(that.name);
            }
        }

        return result;
    }

    public String toString() {
        return file.getNameWithoutExtension();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final VirtualFileAdapter that = (VirtualFileAdapter) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;

        return true;
    }

    public int hashCode() {
        return (path != null ? path.hashCode() : 0);
    }
}
