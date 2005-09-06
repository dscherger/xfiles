/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class VirtualFileCounterMap {

    private Logger log = Logger.getInstance(getClass().getName());

    private String name;
    private Map map = new HashMap();

    public VirtualFileCounterMap(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public VirtualFileCounter getCounter(String key) {
        VirtualFileCounter counter = (VirtualFileCounter) map.get(key);
        if (counter == null) {
            counter = new VirtualFileCounter(key);
            map.put(key, counter);
        }
        return counter;
    }

    public Collection getCounters() {
        return map.values();
    }

    public int getCount(String key) {
        VirtualFileCounter counter = getCounter(key);
        return counter.getCount();
    }

    public void count(String key, VirtualFile file) {
        VirtualFileCounter counter = getCounter(key);
        counter.count(file);
    }

    public void log() {
        log.debug(name + " map has " + map.size() + " entries");
        for (Iterator iterator = map.values().iterator(); iterator.hasNext();) {
            VirtualFileCounter counter = (VirtualFileCounter) iterator.next();
            log.debug(counter.getName() + " " + counter.getCount());
        }
    }
}

