/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/


package org.jdesktop.application;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

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

    @Test
    public void testCustomResourceFolder() {
        TestResourceManager manager = resourceManager();
        final String customFolderName = "customFolderName";
        manager.setResourceFolder(customFolderName);
        List<String> classBundleNames = manager.getClassBundleNames(Object.class);
        assertTrue(classBundleNames.get(0).contains(customFolderName));
    }

    @Test
    public void testCustomResourceFolderFramework() {
        TestResourceManager manager = resourceManager();
        final String customFolderName = "customFolderName";
        final String defaultFolderName = "resources";
        manager.setResourceFolder(customFolderName);
        List<String> classBundleNames = manager.getClassBundleNames(Application.class);
        assertTrue(classBundleNames.get(0).contains(defaultFolderName));
    }

    @Test
    public void testDefaultResourceFolder() {
        TestResourceManager manager = resourceManager();
        final String defaultFolderName = "resources";
        List<String> classBundleNames = manager.getClassBundleNames(Object.class);
        assertTrue(classBundleNames.get(0).contains(defaultFolderName));
    }

    @Test
    public void testNoResourceFolder() {
        TestResourceManager manager = resourceManager();
        manager.setResourceFolder(null);
        List<String> classBundleNames = manager.getClassBundleNames(Object.class);
        assertTrue(classBundleNames.get(0).equals(Object.class.getName()));
    }

}
