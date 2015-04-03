/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class EditConfigurationsAction extends AnAction {

    public EditConfigurationsAction() {
        super("Edit Configurations", "Edit Configurations", XFilesIcons.PROPERTIES_ICON);
    }

    public void actionPerformed(AnActionEvent event) {
        DataContext context = event.getDataContext();
        Project project = PlatformDataKeys.PROJECT.getData(context);
        if (project != null) {
            XFilesConfigurable configurable = project.getComponent(XFilesConfigurable.class);

            ShowSettingsUtil util = ShowSettingsUtil.getInstance();
            util.editConfigurable(project, configurable);
        }
    }
}
