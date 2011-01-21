/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/


package org.jdesktop.application;

import org.junit.Test;

/**
 * [TBD]
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */

public class ResourceManagerTest
{


    class TestResourceManager extends ResourceManager
    {
        TestResourceManager()
        {
            super(new ApplicationContext());
        }
    }

    TestResourceManager resourceManager()
    {
        return new TestResourceManager();
    }

    @Test
    public void testBasics()
    {
        TestResourceManager manager = resourceManager();
        ResourceMap rm = manager.getResourceMap(getClass());
        // [TBD]
    }
}
