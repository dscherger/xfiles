/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;

/**
 * Configurable ProjectComponent classes appear under Project Settings in the settings panel.
 *
 * Configurable ApplicationComponent classes presumably appear under IDE settings in the settings panel.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFiles implements Configurable, ProjectComponent {

    private static final Logger log = Logger.getInstance(XFiles.class.getName());

    public static final String TOOL_WINDOW_ID = "XFiles";
    public static final Icon TOOL_WINDOW_ICON = new ImageIcon(XFiles.class.getResource("/objectBrowser/visibilitySort.png"));

    private Project project;
    private XFilesConfigurationEditor editor;

    public XFiles(Project project) {
        this.project = project;
    }

    // Configurable methods

    public String getDisplayName() {
        return TOOL_WINDOW_ID;
    }

    public Icon getIcon() {
        return TOOL_WINDOW_ICON;
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        if (editor == null) editor = new XFilesConfigurationEditor();
        return editor;
    }

    /**
     * Compare the configuration values against those in the configuration editor.
     *
     * This method must return true when the configuration has changed to enable the apply button
     * on the configuration panel.
     *
     * @return true if the configuration has changed
     */
    public boolean isModified() {
        // TODO: need methods to compare configuration and editor
        return false;
    }

    /**
     * Apply the current values in the configuration editor to the saved configuration.
     *
     * @throws ConfigurationException
     */
    public void apply() throws ConfigurationException {
        log.debug("apply");
    }

    /**
     * Reset the values in the configuration editor from those in the saved configuration.
     */
    public void reset() {
        // TODO: might want to run a counting filter at this point to get available values
        // after filter has completed use the associated logger to get initial values for
        // the confugration panel then apply current configurations on top of that
        log.debug("reset");
    }

    public void disposeUIResources() {
        editor = null;
    }

    // ProjectComponent methods

    public String getComponentName() {
        return "XFiles";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    public void projectOpened() {
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        ToolWindow window = manager.registerToolWindow(TOOL_WINDOW_ID,
                                                       new XFilesToolWindow(project),
                                                       ToolWindowAnchor.LEFT);
        window.setTitle(project.getName());
        window.setIcon(TOOL_WINDOW_ICON);
    }

    public void projectClosed() {
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        manager.unregisterToolWindow(TOOL_WINDOW_ID);
    }
}
