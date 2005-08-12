/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * This configuration editor panel is modeled after the JUnit run configuration editor.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesConfigurationEditor extends JPanel {

    private Icon addIcon = new ImageIcon(getClass().getResource("/general/add.png"));
    private Icon removeIcon = new ImageIcon(getClass().getResource("/general/remove.png"));
    private Icon copyIcon = new ImageIcon(getClass().getResource("/general/copy.png"));
    private Icon moveUpIcon = new ImageIcon(getClass().getResource("/actions/moveUp.png"));
    private Icon moveDownIcon = new ImageIcon(getClass().getResource("/actions/moveDown.png"));

    public XFilesConfigurationEditor() {
        ActionManager actionManager = ActionManager.getInstance();

        AnAction add = new IconAction("add", "add", addIcon);
        AnAction remove = new IconAction("remove", "remove", removeIcon);;
        AnAction copy = new IconAction("copy", "copy", copyIcon);;
        AnAction moveUp = new IconAction("move up", "move up", moveUpIcon);;
        AnAction moveDown = new IconAction("move down", "move down", moveDownIcon);;

        DefaultActionGroup group = new DefaultActionGroup("xfiles configuration group", false);
        group.add(add);
        group.add(remove);
        group.add(copy);
        group.add(moveUp);
        group.add(moveDown);

        ActionToolbar toolbar = actionManager.createActionToolbar("XFilesConfigurationToolbar", group, true);
        add(toolbar.getComponent(), BorderLayout.NORTH);

    }

    // toolbar with add/delete/copy/up/down buttons
    // list of configured filters
    // selecting a filter shows the associated filter configuration editor

    // filter configuration editor contains
    // status names
    // type names
    // vcs names
    // module names
    // path globs
    // ignored/source/test/files/directories/open check boxes
    // each item contains a count of matching things from running a blank filter over the current project

    // note that we're editing the settings for a single filter not for all filters
    // however the cancel/apply buttons apply to the entire configuration session
    // and either all changes or no changes are saved

    private class IconAction extends AnAction {
        public IconAction(String text, String description, Icon icon) {
            super(text, description, icon);
        }

        public void actionPerformed(AnActionEvent e) {
        }
    }
}
