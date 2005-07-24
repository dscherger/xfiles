/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vcs.FileStatus;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesListCellRenderer extends JLabel implements ListCellRenderer {

    public XFilesListCellRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        VirtualFileAdapter adapter = (VirtualFileAdapter) value;
        //VirtualFile file = adapter.getFile();

        // TODO: we might want to contain several icons and a label?
        // in case we want to indicate whether a file is open or not
        // and allow open/close with specific clicks rather than on selection

        //fileEditorManager.isFileOpen(file);
        FileStatus status = adapter.getStatus();
        FileType type = adapter.getType();

        setText(adapter.toString());
        setIcon(type.getIcon());

        if (isSelected)
            setBackground(list.getSelectionBackground());
        else
            setBackground(list.getBackground());

        setForeground(status.getColor());

        return this;
    }
}
