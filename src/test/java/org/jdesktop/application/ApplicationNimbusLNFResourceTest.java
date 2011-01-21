/*
 * Copyright (C) 2010 Illya Yalovyy
 * Use is subject to license terms.
 */

package org.jdesktop.application;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

/**
 * Checks special support for Nimbus LnF
 *
 * @author Illya Yalovyy
 */
public class ApplicationNimbusLNFResourceTest
{

    /* Application.lookAndFeel resource is explicitly defined to be "nimbus".
     */
    public static class ApplicationNimbusLNF extends WaitForStartupApplication
    {
    }

    @Before
    public void methodSetup()
    {
    	ApplicationNimbusLNF.launchAndWait(ApplicationNimbusLNF.class);
    }

    @Test
    public void testApplicationLookAndFeelResource()
    {
        ApplicationContext ctx = Application.getInstance(ApplicationNimbusLNF.class).getContext();
        String lnfResource = ctx.getResourceMap().getString("Application.lookAndFeel");
        assertNotNull(lnfResource);
        assertEquals("Application.lookAndFeel resource", "nimbus", lnfResource);
        
        LookAndFeel lnf = UIManager.getLookAndFeel();
        assertEquals("UIManager.getLookAndFeel", "Nimbus", lnf.getName());
    }
}

