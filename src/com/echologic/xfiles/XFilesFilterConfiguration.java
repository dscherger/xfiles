/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.JDOMExternalizableStringList;
import com.intellij.openapi.util.WriteExternalException;

import org.jdom.Element;

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

    public String NAME;

    /**
     * TODO: this isn't quite correct
     * there *must* be OR logic between selections of a single type (i.e. if several
     * version control statuses are selected a logical AND between them would never
     * match). this setting really controls the AND/OR logic BETWEEN the different
     * types. i.e. the following file statuses AND/OR the folling file types.
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
