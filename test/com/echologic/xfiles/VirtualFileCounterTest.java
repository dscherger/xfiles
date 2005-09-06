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
public class VirtualFileCounterTest extends TestCase {

    /**
     * this test relies on the fact that the current counter implementation doesn't
     * actuall use the file in its public api.
     */
    public void testCounter() {
        VirtualFileCounter counter = new VirtualFileCounter("test");
        assertEquals("test", counter.getName());
        assertEquals(0, counter.getCount());
        counter.count(null);
        assertEquals(1, counter.getCount());
        counter.count(null);
        assertEquals(2, counter.getCount());
    }
}
