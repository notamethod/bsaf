/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */

package org.jdesktop.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

/**
 * Checks that explicitly defining the Application.lookAndFeel resource
 * to be "system" causes the UIManager.lookAndFeel property to be
 * initialized to the sytem look and feel.
 * This test depends on resources/AppilcationSystemLNF.properties
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationSystemLNFResourceTest
{

    /* Application.lookAndFeel resource is explicity defined to be "system".
     */
    public static class ApplicationSystemLNF extends WaitForStartupApplication
    {
    }


    @Before
    public void methodSetup()
    {
        ApplicationSystemLNF.launchAndWait(ApplicationSystemLNF.class);
    }

    @Test
    public void testApplicationLookAndFeelResource()
    {
        ApplicationContext ctx = Application.getInstance(ApplicationSystemLNF.class).getContext();
        String lnfResource = ctx.getResourceMap().getString("Application.lookAndFeel");
        assertEquals("Application.lookAndFeel resource", "system", lnfResource);
        LookAndFeel lnf = UIManager.getLookAndFeel();
        // On Linux sestemLaF could not be native
        assertTrue("Look and Feel should be native", UIManager.getSystemLookAndFeelClassName().equals(lnf.getClass().getName()));
    }
}


