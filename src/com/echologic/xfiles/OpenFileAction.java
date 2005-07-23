/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.FileType;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class OpenFileAction extends AnAction implements Comparable {

    private static Logger log = Logger.getInstance(OpenFileAction.class.getName());

    private String path;
    private String name;
    private FileType type;

    public OpenFileAction(VirtualFile file, FileStatus status) {
        super(file.getName(), status.getText() + "/" + status, file.getFileType().getIcon());
        this.path = file.getPath();
        this.name = file.getName();
        this.type = file.getFileType();

        // TODO: use status to colour displayed name if possible
    }

    public String getName() {
        return name;
    }

    public FileType getType() {
        return type;
    }

    public void actionPerformed(AnActionEvent e) {
        log.debug("open file selected " + name);
        // presumably we'll flip the editor to this file
    }

    public int compareTo(Object o) {
        OpenFileAction that = (OpenFileAction) o;

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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final OpenFileAction that = (OpenFileAction) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;

        return true;
    }

    public int hashCode() {
        return (path != null ? path.hashCode() : 0);
    }
}
