/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
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
import com.intellij.openapi.vcs.FileStatus;
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


        ListCellRenderer renderer = new XFilesRenderer();
        list.setCellRenderer(renderer);
        
        // TODO: need a custom list renderer that renders text according to file status colour
        // might also want some icons in a gutter to indicate things?
        // for example a way to open or close selected files

        FileEditorManagerListener editorListener = new FileEditorManagerListener() {
            public void fileOpened(FileEditorManager source, VirtualFile file) {
                log.debug("file opened " + file.getName());
            }

            public void fileClosed(FileEditorManager source, VirtualFile file) {
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
            }
        };

        final FileEditorManager editor = FileEditorManager.getInstance(project);
        editor.addFileEditorManagerListener(editorListener);

        //editor.closeFile();

        ListSelectionListener listener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int first = e.getFirstIndex();
                    int last = e.getLastIndex();

                    VirtualFileAdapter adapter = null;
                    if (list.isSelectedIndex(first) && list.isSelectedIndex(last)) {
                        if (first == last) {
                            log.debug("selected " + first);
                            adapter = (VirtualFileAdapter) model.get(first);
                        } else {
                            log.debug("both selected " + first + ", " + last);
                            return;
                        }
                    } else if (list.isSelectedIndex(first)) {
                        log.debug("selected " + first);
                        log.debug("unselected " + last);
                        adapter = (VirtualFileAdapter) model.get(first);
                    } else if (list.isSelectedIndex(last)) {
                        log.debug("unselected " + first);
                        log.debug("selected " + last);
                        adapter = (VirtualFileAdapter) model.get(last);
                    } else {
                        log.debug("none selected " + first + ", " + last);
                        return;
                    }

                    VirtualFile file = adapter.getFile();
                    FileStatus status = adapter.getStatus();

                    log.debug("selected " + status + " file " + file);

                    // TODO: really we should open the file if it's closed or else select it if its already open
                    // except that the way to do this via the openapi is non-obvious
                    // just opening the file seems to work ok

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

    class XFilesRenderer extends JLabel implements ListCellRenderer {
        public XFilesRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            VirtualFileAdapter adapter = (VirtualFileAdapter) value;
            FileStatus status = adapter.getStatus();
            setText(value.toString());

            if (isSelected)
                setBackground(list.getSelectionBackground());
            else
                setBackground(list.getBackground());

            setForeground(status.getColor());

            return this;
        }
    }

}
