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
public class CopyFilterAction extends EnableableAction {

    private static Icon ICON = new ImageIcon(CopyFilterAction.class.getResource("/general/copy.png"));

    public CopyFilterAction() {
        super("Copy", "Copy Filter", ICON);
    }

}
