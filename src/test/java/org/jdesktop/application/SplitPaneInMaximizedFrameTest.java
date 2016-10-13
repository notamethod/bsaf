/*
 * Copyright (C) 2011 Illya Yalovyy
 * Use is subject to license terms.
 */
package org.jdesktop.application;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.EventObject;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for the ticket 
 * http://kenai.com/jira/browse/BSAF-112
 * @author Illya Yalovyy
 */
public class SplitPaneInMaximizedFrameTest {
    private static final int DIVIDER_LOCATION = 200;
    
    public static class SplitPaneTest extends WaitForStartupSFA
    {
        private static final String SESSION = "SplitPaneTest.session.xml";
        Object sessionObject = null;

        /* An incomplete XMLEncoder/Decoder file. */
        private String sessionContent =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
"<java version=\"1.6.0_25\" class=\"java.beans.XMLDecoder\"> " +
" <object class=\"java.util.HashMap\"> " +
"  <void method=\"put\"> " +
"   <string>Split/null.contentPane/null.layeredPane/JRootPane0/SplitPaneTest</string> " +
"   <object class=\"org.jdesktop.application.session.SplitPaneState\"> " +
"    <void property=\"dividerLocation\"> " +
"     <int>"+DIVIDER_LOCATION+"</int> " +
"    </void> " +
"   </object> " +
"  </void> " +
"  <void method=\"put\"> " +
"   <string>SplitPaneTest</string> " +
"   <object class=\"org.jdesktop.application.session.WindowState\"> " +
"    <void property=\"bounds\"> " +
"     <object class=\"java.awt.Rectangle\"> " +
"      <int>50</int> " +
"      <int>50</int> " +
"      <int>150</int> " +
"      <int>150</int> " +
"     </object> " +
"    </void> " +
"    <void property=\"frameState\"> " +
"     <int>6</int> " +
"    </void> " +
"    <void property=\"graphicsConfigurationBounds\"> " +
"     <object class=\"java.awt.Rectangle\"> " +
"      <int>0</int> " +
"      <int>0</int> " +
"      <int>1024</int> " +
"      <int>768</int> " +
"     </object> " +
"    </void> " +
"    <void property=\"screenCount\"> " +
"     <int>1</int> " +
"    </void> " +
"   </object> " +
"  </void> " +
" </object> " +
"</java> ";


        public JSplitPane split;
        
        @Override
        protected void startup()
        {
            try
            {
                OutputStream ost = getContext().getLocalStorage().openOutputFile(SESSION);
                PrintStream pst = new PrintStream(ost);
                pst.print(sessionContent);
                pst.close();
            }
            catch (IOException e)
            {
                throw new Error("unexpected IOException", e);
            }
            
            JPanel right = new JPanel();
            right.setBackground(Color.RED);
            JPanel left = new JPanel();
            left.setBackground(Color.BLUE);
            split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            split.setName("Split");
            getMainFrame().setName("SplitPaneTest");


            split.setLeftComponent(left);
            split.setRightComponent(right);
            FrameView view = getMainView();

            view.setComponent(split);


            show(view);            
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
        SplitPaneTest.launchAndWait(SplitPaneTest.class);
    }

    @Test
    public void testBadSessionState() throws Exception
    {
        final SplitPaneTest app = Application.getInstance(SplitPaneTest.class);
        assertEquals(DIVIDER_LOCATION, app.split.getDividerLocation());
        Runnable doExit = new Runnable()
        {
            @Override
            public void run() { app.exit(); }  // override doesn't call System.exit
        };
        SwingUtilities.invokeAndWait(doExit);
    }
}
