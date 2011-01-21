/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/

package org.jdesktop.application;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.EventObject;

/**
 * Verify that a corrupted session.xml file will not crash
 * a SingleFrameApplication.
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class BadSessionStateTest
{

    public static class BadSessionStateApplication extends WaitForStartupSFA
    {
        private String sessionFile = "mainFrame.session.xml";
        Object sessionObject = null;

        /* An incomplete XMLEncoder/Decoder file. */
        private String badContents =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<java version=\"1.6.0\" class=\"java.beans.XMLDecoder\">";

        @Override
        protected void startup()
        {
            try
            {
                OutputStream ost = getContext().getLocalStorage().openOutputFile(sessionFile);
                PrintStream pst = new PrintStream(ost);
                pst.print(badContents);
                pst.close();
            }
            catch (IOException e)
            {
                throw new Error("unexpected IOException", e);
            }
            show(new JLabel("Hello World"));
            super.startup();
        }

        @Override
        protected void shutdown()
        {
            super.shutdown();
            /* At this point it should be possible to read the
            * session.xml file without problems, since it was
            * just rewritten.
            */
            try
            {
                sessionObject = getContext().getLocalStorage().load(sessionFile);
            }
            catch (IOException e)
            {
                throw new Error("couldn't load " + sessionFile, e);
            }
        }

        // Don't call System.exit(), exitListeners, etc
        @Override
        public void exit(EventObject event)
        {
            shutdown();
        }

    }


    @Before
    public void methodSetup()
    {
        System.err.println("This test generates logger warnings.  Ignore them.");
        BadSessionStateApplication.launchAndWait(BadSessionStateApplication.class);
    }

    @Test
    public void testBadSessionState() throws Exception
    {
        final BadSessionStateApplication app = Application.getInstance(BadSessionStateApplication.class);
        assertTrue("BadSessionStateApplication started", app.isReady());
        Runnable doExit = new Runnable()
        {
            @Override
            public void run() { app.exit(); }  // override doesn't call System.exit
        };
        SwingUtilities.invokeAndWait(doExit);
        assertNotNull("getLocalStorage().load(sessionFile)", app.sessionObject);
    }
}


