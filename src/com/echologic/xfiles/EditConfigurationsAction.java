/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

/**
 * We could significantly simplify the filter configuration problem by allowing configuration
 * of only a single filter. The tool window would then need to allow for add/edit/delete of
 * the filter and also for changing the order of the filters.
 *
 * This means +5 buttons on the tool window (somewhat bad)
 * but also quicker access to editing the selected filter (somewhat good)
 * but also requires selection of some filter before allowing it to be configured
 *
 * This may not actually be the case. The apply/reset/isModified methods of Configurable
 * imply the entire configuration so we're really going to be editing the whole thing
 * either way. 
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class EditConfigurationsAction extends AnAction {

    public EditConfigurationsAction() {
        super("Edit Configurations", "Edit Configurations", XFilesIcons.PROPERTIES_ICON);
    }

    public void actionPerformed(AnActionEvent event) {
        DataContext context = event.getDataContext();
        Project project = (Project) context.getData(DataConstants.PROJECT);
        XFiles xfiles = (XFiles) project.getComponent(XFiles.class);

        ShowSettingsUtil util = ShowSettingsUtil.getInstance();
        util.editConfigurable(project, xfiles);
    }
}
