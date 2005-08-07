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

import org.jdom.Element;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesConfiguration implements JDOMExternalizable {

    // TODO: scroll to/from settings
    // TODO: currently selected filter
    // TODO: ordered list of configured filters

    // then for each filter in the list of filters

    // list of accepted statuses
    // list of accepted types
    // list of accepted vcses
    // list of accepted modules
    // list of accepted globs
    // ignored, sources, tests, directories, open

    // TODO: status, type, vcs, module must all be strings!

    // layer up classes of externalizable objects

    private JDOMExternalizableStringList list = new JDOMExternalizableStringList();

    public void testing() {
        //JDOMExternalizer.readBoolean();
        //JDOMExternalizer.readInteger();
        //JDOMExternalizer.readString();
        //JDOMExternalizer.write();
    }
    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }
}
