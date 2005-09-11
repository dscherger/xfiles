/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class RemoveFilterAction extends EnableableAction {

    private static Icon ICON = new ImageIcon(RemoveFilterAction.class.getResource("/general/remove.png"));

    public RemoveFilterAction() {
        super("Remove", "Remove Filter", ICON);
    }

}
