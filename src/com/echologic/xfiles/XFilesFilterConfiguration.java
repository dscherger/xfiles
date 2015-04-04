/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import org.jdom.Element;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.JDOMExternalizableStringList;
import com.intellij.openapi.util.WriteExternalException;

/**
 * TODO: consider ExternalizableFilterConfiguration and EditableFilterConfiguration
 * TODO:     also ExternalizablePluginConfiguration and EditablePluginConfiguration
 *
 * or ConfigurableFilterModel/ExternalizableFilterModel
 * 
 * where plugin configuration represents all of the filter configurations
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesFilterConfiguration implements JDOMExternalizable {

    private Logger log = Logger.getInstance(getClass().getName());

    /**
     * Name of the this filter configuration.
     */
    public String NAME;

    /**
     * The MATCH_ALL property controls the AND/OR logic of selections between the various
     * configuration tables.
     *
     * When set to "all" (true)  at least one entry from each table must match.
     * When set to "any" (false) only one entry from any table must match.
     *
     * match ALL implies logical AND between the various tables
     *
     *      any path AND any attribute AND any status AND any type AND any vcs AND any module
     *
     * match ANY implies logical OR between the various tables
     *
     *      any path OR any attribute OR any status OR any type OR any vcs OR any module
     *
     * i.e. match ALL requires one item on all tabs
     *  and match ANY requires one item on any tab
     */
    public boolean MATCH_ALL;

    // TODO: these would be nice in an array which the default externalizer doesn't seem to handle

    public JDOMExternalizableStringList ACCEPTED_PATH_NAMES = new JDOMExternalizableStringList();
    public JDOMExternalizableStringList ACCEPTED_ATTRIBUTE_NAMES = new JDOMExternalizableStringList();
    public JDOMExternalizableStringList ACCEPTED_STATUS_NAMES = new JDOMExternalizableStringList();
    public JDOMExternalizableStringList ACCEPTED_TYPE_NAMES = new JDOMExternalizableStringList();
    public JDOMExternalizableStringList ACCEPTED_VCS_NAMES = new JDOMExternalizableStringList();
    public JDOMExternalizableStringList ACCEPTED_MODULE_NAMES = new JDOMExternalizableStringList();

    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
        //log.debug("readExternal: " + NAME);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        //log.debug("writeExternal: " + NAME);
        DefaultJDOMExternalizer.writeExternal(this, element);
    }

    public void log() {
        log.debug("match " + (MATCH_ALL ? "all" : "any"));
        log.debug("path " + ACCEPTED_PATH_NAMES);
        log.debug("attribute " + ACCEPTED_ATTRIBUTE_NAMES);
        log.debug("status " + ACCEPTED_STATUS_NAMES);
        log.debug("type " + ACCEPTED_TYPE_NAMES);
        log.debug("vcs " + ACCEPTED_VCS_NAMES);
        log.debug("module " + ACCEPTED_MODULE_NAMES);
    }

}
