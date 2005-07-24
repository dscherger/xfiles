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
import com.intellij.openapi.vcs.FileStatusManager;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesListCellRenderer extends JLabel implements ListCellRenderer {

    private FileStatusManager fileStatusManager;

    public XFilesListCellRenderer(FileStatusManager fileStatusManager) {
        this.fileStatusManager = fileStatusManager;
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        VirtualFileAdapter adapter = (VirtualFileAdapter) value;
        FileType type = adapter.getType();

        setText(adapter.toString());
        setIcon(type.getIcon());

        FileStatus status = fileStatusManager.getStatus(adapter.getFile());
        setForeground(status.getColor());

        if (isSelected)
            setBackground(list.getSelectionBackground());
        else
            setBackground(list.getBackground());

        return this;
    }
}
