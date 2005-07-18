/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class FilterActionGroup extends DefaultActionGroup {

    private Logger log = Logger.getInstance(getClass().getName());

    public FilterActionGroup() {
        super("xfiles filter", true);
        //add(new ConfigurableFilterAction("xxx", "XXX"));
        //add(new ConfigurableFilterAction("yyy", "YYY"));
        //add(new ConfigurableFilterAction("zzz", "ZZZ"));
    }

    public void update(AnActionEvent e) {
        //Presentation presentation = e.getPresentation();
        //presentation.setIcon(ConfigurableFilterAction.ICON);
        //presentation.setText("text");
        //presentation.setDescription("description");
        //presentation.setEnabled(true);
        //presentation.setVisible(true);
        //log.debug("updated from place " + e.getPlace());
    }

    public boolean displayTextInToolbar() {
        return true;
    }


}
