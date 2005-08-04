/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.ImageIcon;
import javax.swing.Icon;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusListener;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.FileStatusProvider;
import com.intellij.openapi.vcs.VcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFiles implements ProjectComponent {

    private static final Logger log = Logger.getInstance(XFiles.class.getName());

    private static final String TOOL_WINDOW_ID = "XFiles";

    private Project project;
    private String name;
    private FileStatusProvider fileStatusProvider;
    private FileStatusManager fileStatusManager;

    public XFiles(Project project) {
        this.project = project;
	/*
        if (project != null)
            this.name = project.getName();
        else
            this.name = "null";
	*/
	
        log.debug("########################################");
        log.debug(name + " constructed");
        //logStuff();
    }

    public String getComponentName() {
        return "XFiles";
    }

    public void initComponent() {
        log.debug("========================================");
        log.debug(name + " initComponent");
        //logStuff();
    }

    public void disposeComponent() {
        log.debug(name + " disposeComponent");
        //logStuff();
        log.debug("========================================");
    }

    public void projectOpened() {
        log.debug("----------------------------------------");
        log.debug(name + " projectOpened");
        //logStuff();

        registerToolWindow();
    }

    public void projectClosed() {
        log.debug(name + " projectClosed");
        //logStuff();
        log.debug("----------------------------------------");

        unregisterToolWindow();
    }

    private void registerToolWindow() {
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        ToolWindow window = manager.registerToolWindow(TOOL_WINDOW_ID, new XFilesToolWindow(project), ToolWindowAnchor.LEFT);
        window.setTitle("Title");
        Icon icon = new ImageIcon(FilterAction.class.getResource("/objectBrowser/visibilitySort.png"));
        window.setIcon(icon);
        // TODO: assign hot key to activate window if that's even possible
    }

    private void unregisterToolWindow() {
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        manager.unregisterToolWindow(TOOL_WINDOW_ID);
    }

    private void logStuff() {
        ProjectManager projectManager = ProjectManager.getInstance();
        log.debug(name + " project manager is " + projectManager.getClass());

        //ProjectManagerListener - opened, closed, etc.

        // two different ways to get project root manager?
        //ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        ProjectRootManager projectRootManager2 = (ProjectRootManager) project.getComponent(ProjectRootManager.class);

        //log.debug(name + " project root manager #1 is " + identify(projectRootManager));
        log.debug(name + " project root manager #2 is " + identify(projectRootManager2));

        VirtualFile[] roots = projectRootManager2.getContentRoots();
        log.debug(name + " has " + roots.length + " roots");
        for (int i = 0; i < roots.length; i++) {
            VirtualFile root = roots[i];
            log.debug(name + " root " + root.getPath());
        }

        VirtualFile[] sourceRoots = projectRootManager2.getContentSourceRoots();
        log.debug(name + " has " + sourceRoots.length + " source roots");
        for (int i = 0; i < sourceRoots.length; i++) {
            VirtualFile sourceRoot = sourceRoots[i];
            log.debug(name + " source root " + sourceRoot.getPath());
        }

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getModules();

        log.debug(name + " has " + modules.length + " modules");
        for (int i = 0; i < modules.length; i++) {
            Module module = modules[i];
            log.debug(name + " module " + i + " is " + module.getName() + " path " + module.getModuleFilePath());
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            //ModuleFileIndex moduleIndex = moduleRootManager.
            //index.isIgnored();
            //index.isInContent();
            //index.isInSource();;
        }

        VcsManager vcsManager = VcsManager.getInstance(project);
        AbstractVcs vcs = vcsManager.getActiveVcs();
        log.debug(name + " active vcs " + vcs);

        if (vcs != null) {
            log.debug(name + " active vcs " + vcs.getDisplayName() + " " + vcs.getName() + " is " + identify(vcs));
            fileStatusProvider = vcs.getFileStatusProvider();
            log.debug(name + " file status provider is " + identify(fileStatusProvider));

            // presumably we would walk all files under the project roots and
            // get the status of each file?!?
            // fileStatusProvider.getStatus();
        }

        // this can be used to find out when file statuses change
        FileStatusListener fileStatusListener = new FileStatusListener() {
            public void fileStatusesChanged() {
                log.debug(name + " file statuses changed!");
            }

            public void fileStatusChanged(VirtualFile file) {
                getStatus("statusChanged", file);
            }
        };

        fileStatusManager = FileStatusManager.getInstance(project);
        fileStatusManager.addFileStatusListener(fileStatusListener);

        // need to filter files in here so that we only see things under our roots
        // also, these events seem to come in triplicate... :-/

        FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {
            public void fileOpened(FileEditorManager fileEditorManager, VirtualFile file) {
                getStatus("fileOpenened", file);
            }

            public void fileClosed(FileEditorManager fileEditorManager, VirtualFile file) {
                getStatus("fileClosed", file);
            }

            public void selectionChanged(FileEditorManagerEvent event) {
                return;
            }
        };

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.addFileEditorManagerListener(fileEditorManagerListener);

        fileEditorManager.getOpenFiles();

        // various index methods that look interesting and possibly relevant
        ProjectFileIndex index = projectRootManager2.getFileIndex();
        ContentIterator iterator = new ContentIterator() {
            public boolean processFile(VirtualFile fileOrDir) {
                getStatus("processFile", fileOrDir);
                return true;
            }
        };

        log.debug("iterating content under roots");
        for (int i = 0; i < roots.length; i++) {
                VirtualFile root = roots[i];
                log.debug(name + " root " + root.getPath() + " isInContent " + index.isInContent(root));
                index.iterateContentUnderDirectory(root, iterator);
        }
    }

    private String identify(Object object) {
        if (object == null) return "null";
        return object.getClass() + "#" + System.identityHashCode(object);
    }

    private void getStatus(String message, VirtualFile file) {
        // oddly PerforceFileStatusProvider indicates "Up to date" for pretty much everything
        /*
        if (fileStatusProvider != null) {
			FileStatus status = fileStatusProvider.getStatus(file);
            log.debug(message + " " +
                      status.getText() + " " +
                      file.getPath() + " " +
                      status.getClass() + " " +
                      fileStatusProvider.getClass());
        }
        */

        // fortunately FileStatusManagerImpl seems to indicate more reasonable statuses
        if (fileStatusManager != null) {
            FileStatus status = fileStatusManager.getStatus(file);
            log.debug(message + " " +
                      status.getText() + " " +
                      file.getPath() + " " +
                      status.getClass() + " " +
                      fileStatusManager.getClass());
        }
    }
}
