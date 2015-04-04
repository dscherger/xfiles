/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.Comparator;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class VirtualFileComparator implements Comparator<VirtualFile> {

    public int compare(VirtualFile file1, VirtualFile file2) {
        FileType type1 = file1.getFileType();
        FileType type2 = file2.getFileType();

        String name1 = file1.getName();
        String name2 = file2.getName();

        int result = type1.getName().compareTo(type2.getName());

        if (result == 0) {
            // TODO: little endian sort on class names
            if (type1 == StdFileTypes.JAVA) {
                result = name1.compareTo(name2);
            } else {
                result = name1.compareTo(name2);
            }
        }

        return result;
    }

}
