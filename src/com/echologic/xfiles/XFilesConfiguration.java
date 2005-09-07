/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.WriteExternalException;

import org.jdom.Element;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesConfiguration implements ProjectComponent, JDOMExternalizable {

    private Logger log = Logger.getInstance(getClass().getName());

    private static final String SCROLL_TO_SOURCE_NODE = "SCROLL_TO_SOURCE";
    private static final String SCROLL_FROM_SOURCE_NODE = "SCROLL_FROM_SOURCE";
    private static final String SELECTED_FILTER_NODE = "SELECTED_FILTER";
    private static final String CONFIGURED_FILTERS_NODE = "CONFIGURED_FILTERS";
    private static final String FILTER_NODE = "FILTER";

    public boolean SCROLL_TO_SOURCE;
    public boolean SCROLL_FROM_SOURCE;
    public String SELECTED_FILTER;

    public List CONFIGURED_FILTERS = new ArrayList();

    // ProjectComponent methods

    public void projectOpened() {}
    public void projectClosed() {}
    public void initComponent() {}
    public void disposeComponent() {}

    public String getComponentName() {
        return "XFilesConfiguration";
    }

    // JDOMExternalizer methods

    /**
     * Read configuration settings from the DOM subtree rooted at the specified element.
     */
    public void readExternal(Element root) throws InvalidDataException {
        log.debug("readExternal");

        SCROLL_TO_SOURCE = JDOMExternalizer.readBoolean(root, SCROLL_TO_SOURCE_NODE);
        SCROLL_FROM_SOURCE = JDOMExternalizer.readBoolean(root, SCROLL_FROM_SOURCE_NODE);
        SELECTED_FILTER = JDOMExternalizer.readString(root, SELECTED_FILTER_NODE);

        Element list = root.getChild(CONFIGURED_FILTERS_NODE);
        List filters = list.getChildren(FILTER_NODE);


        for (Iterator iterator = filters.iterator(); iterator.hasNext();) {
            Element filter = (Element) iterator.next();
            XFilesFilterConfiguration configuration = new XFilesFilterConfiguration();
            configuration.readExternal(filter);
        }

        log.debug("readExternal: " + CONFIGURED_FILTERS.size() + " filters");
    }

    /**
     * Write configuration settings to the DOM subtree rooted at the specified element.
     */
    public void writeExternal(Element root) throws WriteExternalException {
        log.debug("writeExternal");

        JDOMExternalizer.write(root, SCROLL_TO_SOURCE_NODE, SCROLL_TO_SOURCE);
        JDOMExternalizer.write(root, SCROLL_FROM_SOURCE_NODE, SCROLL_FROM_SOURCE);
        JDOMExternalizer.write(root, SELECTED_FILTER_NODE, SELECTED_FILTER);

        Element list = new Element(CONFIGURED_FILTERS_NODE);
        root.addContent(list);

        log.debug("writeExternal: " + CONFIGURED_FILTERS.size() + " filters");

        for (Iterator iterator = CONFIGURED_FILTERS.iterator(); iterator.hasNext();) {
            XFilesFilterConfiguration configuration = (XFilesFilterConfiguration) iterator.next();
            Element filter = new Element(FILTER_NODE);
            list.addContent(filter);
            configuration.writeExternal(filter);
        }
    }

}