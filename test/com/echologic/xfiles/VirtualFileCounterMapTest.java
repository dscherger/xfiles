/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class VirtualFileCounterMapTest extends TestCase {

    private VirtualFileCounterMap map = new VirtualFileCounterMap("test");

    public void testCounters() {
        assertTrue(map.getCounters().isEmpty());

        map.count("foo", null);
        map.count("foo", null);
        map.count("foo", null);
        map.count("bar", null);
        map.count("bar", null);

        assertEquals("test", map.getName());
        assertEquals(3, map.getCount("foo"));
        assertEquals(2, map.getCount("bar"));

        assertEquals(2, map.getCounters().size());
    }
}
