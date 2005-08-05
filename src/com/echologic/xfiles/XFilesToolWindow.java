/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.BorderLayout;
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
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesToolWindow extends JPanel {

    private Logger log = Logger.getInstance(getClass().getName());

    // TODO: need a SortedListModel that re-orders things as they are added
    //private DefaultListModel model = new DefaultListModel();
    private OpenFilesComboBoxModel model = new OpenFilesComboBoxModel();
    private JList list = new JList(model);

    private FilterAction refresh = new FilterAction(model);
    private ToggleAction scrollToSource = new ScrollToSourceAction();
    private ToggleAction scrollFromSource = new ScrollFromSourceAction();


    public XFilesToolWindow(Project project) {
        super(new BorderLayout());

        AnAction selections = new FilterSelectionComboBoxAction(project, refresh);

        DefaultActionGroup group = new DefaultActionGroup("xfiles group", false);
        group.add(refresh);
        group.add(selections);
        group.add(scrollToSource);
        group.add(scrollFromSource);

        ActionManager actionManager = ActionManager.getInstance();

        ActionToolbar toolbar = actionManager.createActionToolbar("XFilesActionToolbar", group, true);
        add(toolbar.getComponent(), BorderLayout.NORTH);

        // TODO: consider re-ordering editor tabs to match selected files here?
        // TODO: consider a sync option to sync editors with our list


        FileStatusManager fileStatusManager = FileStatusManager.getInstance(project);
        ListCellRenderer renderer = new XFilesListCellRenderer(fileStatusManager);
        list.setCellRenderer(renderer);

        // TODO: might want some icons in a gutter to indicate things?
        // for example a way to open/close selected files

        FileEditorManagerListener editorListener = new FileEditorManagerListener() {

            private void addAndScroll(VirtualFile file) {
                if (!model.contains(file) && refresh.getFilter().accept(file)) {
                    model.addElement(file);
                    log.debug("added " + file);
                }

                int index = model.indexOf(file);
                if (index >= 0 && scrollFromSource.isSelected(null)) {
                    list.setSelectedIndex(index);
                    list.ensureIndexIsVisible(index);
                    log.debug("selected " + file);

                }
            }

            public void fileOpened(FileEditorManager source, VirtualFile file) {
                log.debug("file opened " + file.getName() + " scroll " + scrollFromSource.isSelected(null));

                addAndScroll(file);
            }

            public void fileClosed(FileEditorManager source, VirtualFile file) {
                log.debug("file closed " + file.getName());
                int index = model.indexOf(file);
                if (index >= 0)
                    model.removeElementAt(index);
            }

            public void selectionChanged(FileEditorManagerEvent event) {
                String oldName = "";
                String newName = "";
                VirtualFile file = null;

                if (event.getOldFile() != null)
                    oldName = event.getOldFile().getName();

                if (event.getNewFile() != null) {
                    newName = event.getNewFile().getName();
                    file = event.getNewFile();
                }

                log.debug("selection changed: old file " + oldName + " new file " + newName + " scroll " + scrollFromSource.isSelected(null));

                if (file != null) addAndScroll(file);
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
                            file = (VirtualFile) model.getElementAt(first);
                        } else {
                            log.debug("both selected " + first + ", " + last);
                            return;
                        }
                    } else if (list.isSelectedIndex(first)) {
                        log.debug("selected " + first);
                        log.debug("unselected " + last);
                        file = (VirtualFile) model.getElementAt(first);
                    } else if (list.isSelectedIndex(last)) {
                        log.debug("unselected " + first);
                        log.debug("selected " + last);
                        file = (VirtualFile) model.getElementAt(last);
                    } else {
                        log.debug("none selected " + first + ", " + last);
                        return;
                    }

                    if (scrollToSource.isSelected(null)) {
                        editor.openFile(file, true);
                        log.debug("selected " + file);
                    }
                }
            }
        };

        // TODO: need to also listen for file created/renamed/deleted events and add/remove from list
        
        ListSelectionModel selection = list.getSelectionModel();
        selection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selection.addListSelectionListener(listener);

        JScrollPane scroller = new JScrollPane();
        scroller.getViewport().setView(list);

        add(scroller, BorderLayout.CENTER);
    }

}
