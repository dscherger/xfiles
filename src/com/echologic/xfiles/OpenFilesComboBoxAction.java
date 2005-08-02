/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;

/**
 * This class is a factory that creates and manages open files combo boxes for various open
 * projects.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class OpenFilesComboBoxAction extends AnAction implements ProjectManagerListener, CustomComponentAction {

    private static Logger log = Logger.getInstance(OpenFilesComboBoxAction.class.getName());

    private Map listeners = new HashMap();
    private OpenFilesComboBoxListener available;

    public OpenFilesComboBoxAction() {
        log.debug("constructed "  + System.identityHashCode(this));
        ProjectManager projectManager = ProjectManager.getInstance();
        projectManager.addProjectManagerListener(this);
    }

    // AnAction methods

    public void actionPerformed(AnActionEvent e) {
    }

    // ProjectManagerListener methods

    /**
     * it appears that calls to createCustomComponent will be preceeded by calls to projectOpened
     * in the cases where the component will have a project. note that there may be calls to
     * createCustomComponent that are not preceeded by projectOpened and these should probably
     * return a null (or simple) component.
     *
     * note also that calls to projectClosed should probably invalidate their associated combo
     * box if we have one registered.
     *
     * looking at this another way, calls to createCustomComponent apparently consume the
     * previously opened project if there is one. calls to projectOpened must save the
     * opened project on the expectation of a later call to createCustomComponent. calls
     * to projectClosed should invalidate their associated compoment.
     *
     *
     * index    project      component
     * -----    -------      ---------
     * 0        null         label
     * 1        null         label
     * 2        foobar       combobox
     *
     * This method now queues up a new listener for the specified project which will be
     * used on the next call to createCustomComponent.
     */
    public void projectOpened(Project project) {
        log.debug("projectOpened: " + project.getName() +"@" + project.hashCode());

        if (available != null)
            log.error("projectOpened: unused " +
                      available.getComboBox().getToolTipText());

        available = new OpenFilesComboBoxListener(project);
        listeners.put(project, available);
        log.debug("projectOpened: available " + available.getComboBox().getToolTipText());
    }

    public boolean canCloseProject(Project project) {
        log.debug("canCloseProject: " + project.getName() +"@" + project.hashCode());
        return true;
    }

    public void projectClosing(Project project) {
        log.debug("projectClosing: " + project.getName() +"@" + project.hashCode());
    }

    public void projectClosed(Project project) {
        log.debug("projectClosed: " + project.getName() +"@" + project.hashCode());
        OpenFilesComboBoxListener listener = (OpenFilesComboBoxListener) listeners.remove(project);
        if (listener != null) {
            log.debug("projectClosed: dispose " + listener.getComboBox().getToolTipText());
            listener.dispose();
        } else {
            log.debug("projectClosed: no listener");
        }
    }

    // CustomComponentAction methods

    /**
     * This method appears to be called once for each project but without any context of which project
     * the compoment is being created for. It seems that we need some way of creating a model and
     * renderer for each project and associating them with the correct combobox.
     *
     * For now we always return the same component but this causes problems when a second project is opened
     * there is only one combobox that has open files from both projects.
     *
     * If we had only one combobox we need a different renderer and model for each project.
     * And when the active project changes we need to flip to the appropriate model and renderer.
     *
     * Consider how all this works when we have two project frames open!
     *
     * @param presentation
     */
    public JComponent createCustomComponent(Presentation presentation) {

        log.debug("createCustomComponent: presentation " + System.identityHashCode(presentation));

        if (available != null) {
            log.debug("createCustomComponent: available " + available.getComboBox().getToolTipText());
            JComponent component = available.getComboBox();
            available = null; // consume this available listener
            return component;
        } else {
            log.debug("createCustomComponent: unavailable ");
            return new JLabel("");
        }
    }

}


