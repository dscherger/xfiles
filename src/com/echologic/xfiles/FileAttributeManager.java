/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class FileAttributeManager {

    private FileEditorManager editorManager;
    private ProjectFileIndex fileIndex;

    private static final int ignored = 1;
    private static final int test = 2;
    private static final int source = 4;
    private static final int open = 8;

    /**
     * There are 16 possible result arrays given the above attribute values. This is
     * done so that we're not creating new arrays on each call to getAttributes since
     * it will be called for each file in the project.
     */
    private String[][] attributes = {
        {                                     }, // 0000
        {                           "ignored" }, // 0001
        {                   "test"            }, // 0010
        {                   "test", "ignored" }, // 0011
        {         "source"                    }, // 0100
        {         "source",         "ignored" }, // 0101
        {         "source", "test"            }, // 0110 nonsense
        {         "source", "test", "ignored" }, // 0111 nonsense
        { "open"                              }, // 1000
        { "open",                   "ignored" }, // 1001
        { "open",           "test"            }, // 1010
        { "open",           "test", "ignored" }, // 1011
        { "open", "source"                    }, // 1100
        { "open", "source",         "ignored" }, // 1101
        { "open", "source", "test"            }, // 1110 nonsense
        { "open", "source", "test", "ignored" }  // 1111 nonsense
    };

    public FileAttributeManager(Project project) {
        editorManager = FileEditorManager.getInstance(project);

        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        fileIndex = rootManager.getFileIndex();
    }

    public String[] getAttributes(VirtualFile file) {

        int index = 0;

        if (fileIndex.isIgnored(file))
            index += ignored;

        // note that SourceContent is a superset of TestSourceContent
        // so we check for test first

        if (fileIndex.isInTestSourceContent(file))
            index += test;
        else if (fileIndex.isInSourceContent(file))
            index += source;

        if (editorManager.isFileOpen(file))
            index += open;

        return attributes[index];
    }
}
