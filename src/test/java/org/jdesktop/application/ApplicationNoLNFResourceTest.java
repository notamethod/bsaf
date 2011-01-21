/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/

package org.jdesktop.application;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;


/**
 * No Application.lookAndFeel resource defined, so we default to
 * "system" (unlike the JVM itself) which means to use the
 * system (native) look and feel.
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationNoLNFResourceTest
{

    public static class ApplicationNoLNF extends WaitForStartupApplication
    {
    }

    @Before
    public void methodSetup()
    {
        ApplicationNoLNF.launchAndWait(ApplicationNoLNF.class);
    }

    @Test
    public void testApplicationLookAndFeelResource()
    {
        LookAndFeel lnf = UIManager.getLookAndFeel();
        // On Linux sestemLaF could not be native
        assertTrue("Look and Feel should be native", UIManager.getSystemLookAndFeelClassName().equals(lnf.getClass().getName()));
    }
}
