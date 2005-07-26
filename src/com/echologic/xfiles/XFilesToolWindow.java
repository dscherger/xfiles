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
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vcs.FileStatusManager;

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

        AnAction filter = new FilterAction(model);
        AnAction selections = new FilterSelectionComboBoxAction();
        AnAction scrollToSource = new ScrollToSourceAction();
        AnAction scrollFromSource = new ScrollFromSourceAction();

        DefaultActionGroup group = new DefaultActionGroup("xfiles group", false);
        group.add(filter);
        group.add(selections);
        group.add(scrollToSource);
        group.add(scrollFromSource);

        ActionManager actionManager = ActionManager.getInstance();

        ActionToolbar toolbar = actionManager.createActionToolbar("XFilesActionToolbar", group, true);
        add(toolbar.getComponent(), BorderLayout.NORTH);

        // TODO: consider re-ordering editor tabs to match selected files here?
        // TODO: consider a sync option to sync editors with our list

        JScrollPane scroller = new JScrollPane();
        final JList list = new JList();

        FileStatusManager fileStatusManager = FileStatusManager.getInstance(project);
        ListCellRenderer renderer = new XFilesListCellRenderer(fileStatusManager);
        list.setCellRenderer(renderer);

        // TODO: might want some icons in a gutter to indicate things?
        // for example a way to open or close selected files

        FileEditorManagerListener editorListener = new FileEditorManagerListener() {
            public void fileOpened(FileEditorManager source, VirtualFile file) {
                // TODO: select this file if it's in our list
                log.debug("file opened " + file.getName());
            }

            public void fileClosed(FileEditorManager source, VirtualFile file) {
                // TODO: unselect this file if it's in our list
                log.debug("file closed " + file.getName());
            }

            public void selectionChanged(FileEditorManagerEvent event) {
                String oldFile = "";
                String newFile = "";

                if (event.getOldFile() != null)
                    oldFile = event.getOldFile().getName();

                if (event.getNewFile() != null)
                    newFile = event.getNewFile().getName();

                log.debug("selection changed: old file " + oldFile + " new file " + newFile);
                // TODO: change the corresponding selections in our list
            }
        };

        final FileEditorManager editor = FileEditorManager.getInstance(project);
        editor.addFileEditorManagerListener(editorListener);

        ListSelectionListener listener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int first = e.getFirstIndex();
                    int last = e.getLastIndex();

                    VirtualFile file = null;
                    if (list.isSelectedIndex(first) && list.isSelectedIndex(last)) {
                        if (first == last) {
                            log.debug("selected " + first);
                            file = (VirtualFile) model.get(first);
                        } else {
                            log.debug("both selected " + first + ", " + last);
                            return;
                        }
                    } else if (list.isSelectedIndex(first)) {
                        log.debug("selected " + first);
                        log.debug("unselected " + last);
                        file = (VirtualFile) model.get(first);
                    } else if (list.isSelectedIndex(last)) {
                        log.debug("unselected " + first);
                        log.debug("selected " + last);
                        file = (VirtualFile) model.get(last);
                    } else {
                        log.debug("none selected " + first + ", " + last);
                        return;
                    }

                    log.debug("selected " + file);

                    // TODO: really we should open the file if it's closed or else select it if its already open
                    // except that the way to do this via the openapi is non-obvious
                    // just opening the file seems to work ok

                    // TODO: only do this if scroll to source is on

                    editor.openFile(file, true);
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
