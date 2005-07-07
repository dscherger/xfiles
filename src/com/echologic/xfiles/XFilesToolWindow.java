/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.BorderLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesToolWindow extends JPanel {

    private Logger log = Logger.getInstance(getClass().getName());
    private Project project;
    private DefaultListModel model = new DefaultListModel();

    public XFilesToolWindow(Project project) {
        super(new BorderLayout());
        this.project = project;

        AnAction action = new FilterAction(model);

        DefaultActionGroup group = new DefaultActionGroup("xfiles group", false);
        group.addSeparator();
        group.add(action);
        group.addSeparator();

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("here", group, true);

        add(toolbar.getComponent(), BorderLayout.NORTH);

        // TODO: need a proper renderer here to render in terms of the right status colour
        // TODO: add a selection listener to open selected files
        // TODO: consider scroll to/from source options
        // TODO: consider re-ordering editor tabs to match selected files here?

        JScrollPane scroller = new JScrollPane();
        final JList list = new JList();

        FileEditorManagerListener editorListener = new FileEditorManagerListener() {
            public void fileOpened(FileEditorManager source, VirtualFile file) {
                log.debug("file opened " + file.getName());
            }

            public void fileClosed(FileEditorManager source, VirtualFile file) {
                log.debug("file closed " + file.getName());
            }

            public void selectionChanged(FileEditorManagerEvent event) {
                log.debug("selection changed: old file " + event.getOldFile().getName() + "; new file " + event.getNewFile().getName());
            }
        };

        FileEditorManager editor = FileEditorManager.getInstance(project);
        editor.addFileEditorManagerListener(editorListener);

        ListSelectionListener listener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int first = e.getFirstIndex();
                    int last = e.getLastIndex();

                    log.debug("selected index " + first + " " + list.isSelectedIndex(first));
                    log.debug("selected index " + last + " " + list.isSelectedIndex(last));
                }
            }
        };

        ListSelectionModel selection = list.getSelectionModel();
        selection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selection.addListSelectionListener(listener);

        list.setModel(model);

        scroller.getViewport().setView(list);

        add(scroller, BorderLayout.CENTER);
    }


}
