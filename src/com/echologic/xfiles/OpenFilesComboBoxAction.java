/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class OpenFilesComboBoxAction extends AnAction
    implements ProjectManagerListener, CustomComponentAction, FileEditorManagerListener, ActionListener
{

    private static Logger log = Logger.getInstance(OpenFilesComboBoxAction.class.getName());

    private FileStatusManager fileStatusManager;
    private FileEditorManager fileEditorManager;

    private JComboBox comboBox;
    private OpenFilesComboBoxModel model;

    public OpenFilesComboBoxAction() {
        log.debug("constructed");
        ProjectManager projectManager = ProjectManager.getInstance();
        projectManager.addProjectManagerListener(this);
    }

    /**
     * This method is not expected to be called.
     */
    public void actionPerformed(AnActionEvent e) {
        log.debug("actionPerformed from " + e.getPlace());
    }

    // ProjectManagerListener methods

    public void projectOpened(Project project) {
        log.debug("projectOpened " + project.getName());

        fileEditorManager = FileEditorManager.getInstance(project);
        fileStatusManager = FileStatusManager.getInstance(project);

        fileEditorManager.addFileEditorManagerListener(this);
    }

    public boolean canCloseProject(Project project) {
        log.debug("canCloseProject: true");
        return true;
    }

    public void projectClosing(Project project) {
        log.debug("projectClosing " + project.getName());
    }

    public void projectClosed(Project project) {
        fileEditorManager.removeFileEditorManagerListener(this);

        fileEditorManager = null;
        fileStatusManager = null;

        log.debug("projectClosed " + project.getName());
    }

    // CustomComponentAction methods

    public JComponent createCustomComponent(Presentation presentation) {
        log.debug("createCustomComponent");

        model = new OpenFilesComboBoxModel();
        comboBox = new JComboBox(model);
        comboBox.setFocusable(false);
        comboBox.setMaximumRowCount(50);
        comboBox.setRenderer(new XFilesListCellRenderer());
        comboBox.addActionListener(this);
        return comboBox;
    }

    // FileEditorManager methods

    public void fileOpened(FileEditorManager source, VirtualFile file) {
        log.debug("file opened " + file.getName());

        if (fileStatusManager == null) return;

        FileStatus status = fileStatusManager.getStatus(file);
        VirtualFileAdapter adapter = new VirtualFileAdapter(file, status);
        model.addElement(adapter);
    }

    public void fileClosed(FileEditorManager source, VirtualFile file) {
        log.debug("file closed " + file.getName());

        if (fileStatusManager == null) return;

        FileStatus status = fileStatusManager.getStatus(file);
        VirtualFileAdapter adapter = new VirtualFileAdapter(file, status);
        model.removeElement(adapter);
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
            FileStatus status = fileStatusManager.getStatus(event.getNewFile());
            VirtualFileAdapter adapter = new VirtualFileAdapter(event.getNewFile(), status);
            model.setSelectedItem(adapter);
        }
    }

    // ActionListener methods

    /**
     * This method is called by the open files combobox when a new file is selected. This
     * selection is passed on to the underlying FileEditorManager to select the specified
     * file.
     */
    public void actionPerformed(ActionEvent event) {
        VirtualFileAdapter adapter = (VirtualFileAdapter) model.getSelectedItem();
        if (adapter != null) {
            log.debug("actionPerformed: event=" + event + " file=" + adapter.getName());
            fileEditorManager.openFile(adapter.getFile(), true);
        } else {
            log.debug("actionPerformed: event=" + event);
        }
    }
}


