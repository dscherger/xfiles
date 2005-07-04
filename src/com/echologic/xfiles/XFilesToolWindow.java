/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesToolWindow extends JPanel {

    private Logger log = Logger.getInstance(getClass().getName());

    private JLabel xfiles = new JLabel("XFiles");

    public XFilesToolWindow() {
        super(new BorderLayout());

        AnAction action = new AnAction() {
            public void actionPerformed(AnActionEvent event) {
                log.debug("actionPerformed");

                Project project = (Project) event.getDataContext().getData(DataConstants.PROJECT);

                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                VirtualFile[] files = fileEditorManager.getOpenFiles();
                for (int i = 0; i < files.length; i++) {
                    VirtualFile file = files[i];
                    log.debug("open file " + file.getPath());
                }

                // various index methods that look interesting and possibly relevant
                ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
                ProjectFileIndex index = projectRootManager.getFileIndex();

                final FileStatusManager fileStatusManager = FileStatusManager.getInstance(project);

                ContentIterator iterator = new ContentIterator() {
                    public boolean processFile(VirtualFile fileOrDir) {
                        if (fileStatusManager != null) {
                            String type = fileOrDir.isDirectory() ? "directory " : "file ";

                            FileStatus status = fileStatusManager.getStatus(fileOrDir);
                            log.debug(status.getText() + " " +
                                      type +
                                      fileOrDir.getPath());
                        }
                        return true;
                    }
                };

                VirtualFile[] roots = projectRootManager.getContentRoots();

                log.debug("iterating content under roots");
                for (int i = 0; i < roots.length; i++) {
                        VirtualFile root = roots[i];
                        log.debug("root " + root.getPath() + " isInContent " + index.isInContent(root));
                        index.iterateContentUnderDirectory(root, iterator);
                }

            }
        };

        DefaultActionGroup group = new DefaultActionGroup("xfiles group", false);
        group.addSeparator();
        group.add(action);
        group.addSeparator();

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("here", group, true);

        add(toolbar.getComponent(), BorderLayout.NORTH);
    }
}
