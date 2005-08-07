/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.List;

import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.JDOMExternalizableStringList;
import com.intellij.openapi.util.WriteExternalException;

import org.jdom.Element;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesFilterConfiguration implements JDOMExternalizable {

    public String FILTER_NAME;

    public List ACCEPTED_STATUS_NAMES = new JDOMExternalizableStringList();
    public List ACCEPTED_TYPE_NAMES = new JDOMExternalizableStringList();
    public List ACCEPTED_VCS_NAMES = new JDOMExternalizableStringList();
    public List ACCEPTED_MODULE_NAMES = new JDOMExternalizableStringList();
    public List ACCEPTED_NAME_GLOBS = new JDOMExternalizableStringList();

    public boolean ACCEPT_IGNORED_FILES;
    public boolean ACCEPT_SOURCE_FILES;
    public boolean ACCEPT_TEST_FILES;
    public boolean ACCEPT_FILES;
    public boolean ACCEPT_DIRECTORIES;
    public boolean ACCEPT_OPEN_FILES;

    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }

}
