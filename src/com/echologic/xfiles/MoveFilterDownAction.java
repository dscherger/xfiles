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
public class MoveFilterDownAction extends EnableableAction {

    private static Icon ICON = new ImageIcon(MoveFilterDownAction.class.getResource("/actions/moveDown.png"));

    public MoveFilterDownAction() {
        super("Move Down", "Move Filter Down", ICON);
    }

}
