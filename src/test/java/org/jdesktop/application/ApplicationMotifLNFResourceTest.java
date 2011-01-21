/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */

package org.jdesktop.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

/**
 * Checks that by defining the Application.lookAndFeel resource
 * to be "com.sun.java.swing.plaf.motif.MotifLookAndFeel" causes
 * the UIManager.lookAndFeel property to be initialized to the
 * Motif L&F.
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationMotifLNFResourceTest
{

    /* Application.lookAndFeel resource is explicity defined to be "system".
     */
    public static class ApplicationMotifLNF extends WaitForStartupApplication
    {
    }

    @Before
    public void methodSetup()
    {
        ApplicationMotifLNF.launchAndWait(ApplicationMotifLNF.class);
    }

    @Test
    public void testApplicationLookAndFeelResource()
    {
        ApplicationContext ctx = Application.getInstance(ApplicationMotifLNF.class).getContext();
        String lnfResource = ctx.getResourceMap().getString("Application.lookAndFeel");
        assertEquals("Application.lookAndFeel resource", "com.sun.java.swing.plaf.motif.MotifLookAndFeel", lnfResource);
        LookAndFeel lnf = UIManager.getLookAndFeel();
        @SuppressWarnings("all") // ... MotifLookAndFeel is Sun proprietary API and may be removed in a future release
                Class motifLNFClass = com.sun.java.swing.plaf.motif.MotifLookAndFeel.class;
        assertSame("UIManager.getLookAndFeel().getClass", motifLNFClass, lnf.getClass());
    }
}

