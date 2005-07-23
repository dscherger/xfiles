/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
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
public class OpenFilesComboBoxAction extends ComboBoxAction
    implements ProjectManagerListener, FileEditorManagerListener
{

    private static Logger log = Logger.getInstance(OpenFilesComboBoxAction.class.getName());

    private FileStatusManager fileStatusManager;
    private FileEditorManager fileEditorManager;

    private ComboBoxButton button;
    private Presentation presentation;

    private SortedSet openFileSet = new TreeSet();
    private DefaultActionGroup group;

    public OpenFilesComboBoxAction() {
        ProjectManager projectManager = ProjectManager.getInstance();
        projectManager.addProjectManagerListener(this);
        log.debug("constructed");
    }

    public void projectOpened(Project project) {
        log.debug("projectOpened " + project.getName());

        fileEditorManager = FileEditorManager.getInstance(project);
        fileStatusManager = FileStatusManager.getInstance(project);
        fileEditorManager.addFileEditorManagerListener(this);

        group = new DefaultActionGroup();

        VirtualFile[] files = fileEditorManager.getOpenFiles();

        for (int i = 0; i < files.length; i++) {
            VirtualFile file = files[i];
            FileStatus status = fileStatusManager.getStatus(file);
            OpenFileAction action = new OpenFileAction(file, status);
            openFileSet.add(action);
            log.debug("open file " + file.getName() + " " + status.getText() + "/" + status);
        }
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

    public JComponent createCustomComponent(Presentation presentation) {
        log.debug("createCustomComponent");

        this.presentation = presentation;
        JPanel panel = new JPanel(new GridBagLayout());
        button = new ComboBoxButton(presentation);
        panel.add(button,
                  new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 3, 0, 3), 0, 0)
        );
        return panel;
    }

    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        log.debug("createPopupActionGroup: "  + group.getChildrenCount());

        VirtualFile[] files = fileEditorManager.getSelectedFiles();

        // TODO: not sure what to do with multiple files open?!?
        if (files.length > 0) this.button.setText(files[0].getName());

        return group;
    }

    public void fileOpened(FileEditorManager source, VirtualFile file) {
        log.debug("file opened " + file.getName());
        if (fileStatusManager == null) return;

        FileStatus status = fileStatusManager.getStatus(file);
        OpenFileAction action = new OpenFileAction(file, status);
        openFileSet.add(action);
        presentation.setText(file.getName());
        update();
    }

    public void fileClosed(FileEditorManager source, VirtualFile file) {
        log.debug("file closed " + file.getName());
        if (fileStatusManager == null) return;

        FileStatus status = fileStatusManager.getStatus(file);
        OpenFileAction action = new OpenFileAction(file, status);
        openFileSet.remove(action);
        presentation.setText(file.getName());
        update();
    }

    public void selectionChanged(FileEditorManagerEvent event) {
        String oldFile = "";
        String newFile = "";

        if (event.getOldFile() != null)
            oldFile = event.getOldFile().getName();

        if (event.getNewFile() != null)
            newFile = event.getNewFile().getName();

        log.debug("selection changed: old file " + oldFile + " new file " + newFile);
        presentation.setText(newFile);
        update();
    }

    private void update() {
        group.removeAll();

        if (!openFileSet.isEmpty()) {
            OpenFileAction last = (OpenFileAction) openFileSet.first();

            for (Iterator iterator = openFileSet.iterator(); iterator.hasNext();) {
                OpenFileAction action = (OpenFileAction) iterator.next();
                if (last.getType() != action.getType()) group.addSeparator();
                last = action;
                group.add(action);
                log.debug("refresh " + action.getName());
            }
        }
    }

}


