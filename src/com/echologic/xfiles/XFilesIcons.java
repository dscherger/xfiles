/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.util.Icons;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public abstract class XFilesIcons extends Icons {

    public static final Icon SCROLL_TO_ICON = load("/general/autoscrollToSource.png");
    public static final Icon SCROLL_FROM_ICON = load("/general/autoscrollFromSource.png");
    public static final Icon REMOVE_ICON = load("/general/remove.png");
    public static final Icon COPY_ICON = load("/general/copy.png");

    public static final Icon DOWN_ICON = load("/actions/moveDown.png");
    public static final Icon UP_ICON = load("/actions/moveUp.png");
    public static final Icon SYNC_ICON = load("/actions/sync.png");
    public static final Icon PROPERTIES_ICON = load("/actions/properties.png");

    public static final Icon FILTER_ICON = load("/debugger/class_filter.png");

    public static final Icon XFILES_ICON = load("/objectBrowser/visibilitySort.png");

    private static Icon load(String path) {
        return new ImageIcon(XFilesIcons.class.getResource(path));
    }
}
