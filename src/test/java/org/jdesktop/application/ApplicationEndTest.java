/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */

package org.jdesktop.application;

import javax.swing.SwingUtilities;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;


/**
 * Verify overriding Application#end() defeats the default call to
 * System.exit().
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationEndTest
{
    private static boolean isAppLaunched = false;

    /* If the JVM were to actually shutdown during the Application#exit call, 
     * we'll throw an Error here.
     */
    private static class ShutdownHookError extends Thread
    {
        public void run()
        {
            throw new Error("JVM shutdown unexpectedly");
        }
    }

    public static class EndApplication extends WaitForStartupApplication
    {
        boolean endCalled = false;
        boolean shutdownRanOnEDT;

        @Override
        protected void end()
        {
            endCalled = true;  // default was System.exit(0);
        }

        //Application.shutdown() javadoc says: "This method runs on the event dispatching thread."
        @Override
        protected void shutdown() {
            shutdownRanOnEDT = SwingUtilities.isEventDispatchThread();
            super.shutdown();
        }
    }

    private EndApplication application()
    {
        return Application.getInstance(EndApplication.class);
    }


    @Before
    public void methodSetup()
    {
        EndApplication.launchAndWait(EndApplication.class);
        isAppLaunched = true;
        Runtime rt = Runtime.getRuntime();
        Thread hook = new ShutdownHookError();
        rt.addShutdownHook(hook);
        application().exit();
        rt.removeShutdownHook(hook);
    }

    @Test
    public void testEndCalled()
    {
        assertTrue("shutdown() ran on the EDT", ((EndApplication) Application.getInstance()).shutdownRanOnEDT);
        assertTrue(application().endCalled);
    }
}


