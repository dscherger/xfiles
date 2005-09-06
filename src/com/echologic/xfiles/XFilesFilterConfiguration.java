/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.JDOMExternalizableStringList;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.diagnostic.Logger;

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

    // TODO: match any/all conditions

    public JDOMExternalizableStringList ACCEPTED_STATUS_NAMES = new JDOMExternalizableStringList();
    public JDOMExternalizableStringList ACCEPTED_TYPE_NAMES = new JDOMExternalizableStringList();
    public JDOMExternalizableStringList ACCEPTED_VCS_NAMES = new JDOMExternalizableStringList();
    public JDOMExternalizableStringList ACCEPTED_MODULE_NAMES = new JDOMExternalizableStringList();
    public JDOMExternalizableStringList ACCEPTED_NAME_GLOBS = new JDOMExternalizableStringList();
    public JDOMExternalizableStringList ACCEPTED_OTHERS = new JDOMExternalizableStringList();

    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
        log.debug("readExternal: " + NAME);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        log.debug("writeExternal: " + NAME);
        DefaultJDOMExternalizer.writeExternal(this, element);
    }

}
