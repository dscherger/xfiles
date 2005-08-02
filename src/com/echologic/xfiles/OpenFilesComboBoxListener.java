/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.MutableComboBoxModel;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * This class represents both a container and associated listeners for a
 * project specific combo comboBox of open files.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class OpenFilesComboBoxListener implements FileEditorManagerListener, ActionListener {

    private static Logger log = Logger.getInstance(OpenFilesComboBoxAction.class.getName());

    // TODO: consider another tool bar action to close the currently open file?!?
    // not sure this is useful... really want close on various open tabs themselves

    private JComboBox comboBox;
    private MutableComboBoxModel model;
    private FileEditorManager fileEditorManager;
    private FileStatusManager fileStatusManager;

    public OpenFilesComboBoxListener(Project project) {
        fileEditorManager = FileEditorManager.getInstance(project);
        fileStatusManager = FileStatusManager.getInstance(project);

        model = new OpenFilesComboBoxModel();
        comboBox = new JComboBox(model);
        comboBox.setFocusable(false);
        comboBox.setMaximumRowCount(50);
        comboBox.addActionListener(this);
        comboBox.setRenderer(new XFilesListCellRenderer(fileStatusManager));

        fileEditorManager.addFileEditorManagerListener(this);
    }

    public void dispose() {
        comboBox.removeActionListener(this);
        comboBox = null;

        fileEditorManager.removeFileEditorManagerListener(this);
        fileEditorManager = null;
        fileStatusManager = null;
    }

    public JComboBox getComboBox() {
        return comboBox;
    }

    public void setComboBox(JComboBox comboBox) {
        this.comboBox = comboBox;
    }

    // FileEditorManager methods

    public void fileOpened(FileEditorManager source, VirtualFile file) {
        log.debug("file opened " + file.getName());
        model.addElement(file);
    }

    public void fileClosed(FileEditorManager source, VirtualFile file) {
        log.debug("file closed " + file.getName());
        model.removeElement(file);
    }

    public void selectionChanged(FileEditorManagerEvent event) {
        String oldFile = null;
        String newFile = null;

        if (event.getOldFile() != null)
            oldFile = event.getOldFile().getName();

        if (event.getNewFile() != null)
            newFile = event.getNewFile().getName();

        log.debug("selection changed: old file " + oldFile + " new file " + newFile);

        if (newFile != null) {
            model.setSelectedItem(event.getNewFile());
        }
    }

    // ActionListener methods

    /**
     * This method is called by the open files combobox when a new file is selected. This
     * selection is passed on to the underlying FileEditorManager to select the specified
     * file.
     */
    public void actionPerformed(ActionEvent event) {
        log.debug("actionPerformed");

        VirtualFile file = (VirtualFile) model.getSelectedItem();
        if (file != null) {
            fileEditorManager.openFile(file, true);
            FileStatus status = fileStatusManager.getStatus(file);
            comboBox.setToolTipText("[" + status + "/" + status.getText() + "] " + file.getPath());
        }
    }


}
