/*
 * Copyright (C) 2010 Illya Yalovyy
 * Use is subject to license terms.
 */
package org.jdesktop.application;

import org.jdesktop.application.session.PropertySupport;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.EventObject;
import java.util.Map;

/**
 * @author Illya Yalovyy
 */
public class CustomPropertySupportTest
{
    private static final String LABEL_NAME = "testLabel";
    private static final String FIRST_VALUE = "1";
    private static final String SECOND_VALUE = "2";

    public static class CustomSessionStateApplication extends WaitForStartupSFA
    {
        final JLabel label = new JLabel();
        private final String sessionFile = "mainFrame.session.xml";
        Object sessionObject = null;

        private String sessionContents =
                "<?xml version='1.0' encoding='UTF-8'?> " +
                        "<java version='1.6.0_10' class='java.beans.XMLDecoder'> " +
                        " <object class='java.util.HashMap'> " +
                        "  <void method='put'> " +
                        "   <string>mainFrame</string> " +
                        "   <object class='org.jdesktop.application.session.WindowState'> " +
                        "    <void property='bounds'> " +
                        "     <object class='java.awt.Rectangle'> " +
                        "      <int>0</int> " +
                        "      <int>28</int> " +
                        "      <int>112</int> " +
                        "      <int>43</int> " +
                        "     </object> " +
                        "    </void> " +
                        "    <void property='graphicsConfigurationBounds'> " +
                        "     <object class='java.awt.Rectangle'> " +
                        "      <int>0</int> " +
                        "      <int>0</int> " +
                        "      <int>1680</int> " +
                        "      <int>1050</int> " +
                        "     </object> " +
                        "    </void> " +
                        "    <void property='screenCount'> " +
                        "     <int>1</int> " +
                        "    </void> " +
                        "   </object> " +
                        "  </void> " +
                        "  <void method='put'> " +
                        "   <string>" + LABEL_NAME + "/null.contentPane/null.layeredPane/JRootPane0/mainFrame</string> " +
                        "   <object class='org.jdesktop.application.CustomPropertySupportTest$LabelState'> " +
                        "    <void property='text'> " +
                        "     <string>" + FIRST_VALUE + "</string> " +
                        "    </void> " +
                        "   </object> " +
                        "  </void> " +
                        " </object> " +
                        "</java> ";

        @Override
        protected void startup()
        {
            label.setName(LABEL_NAME);
            getContext().getSessionStorage().putProperty(JLabel.class, new LabelProperty());

            try
            {
                OutputStream ost = getContext().getLocalStorage().openOutputFile(sessionFile);
                PrintStream pst = new PrintStream(ost);
                pst.print(sessionContents);
                pst.close();
            }
            catch (IOException e)
            {
                throw new Error("unexpected IOException", e);
            }
            show(label);
            super.startup();
        }

        @Override
        protected void shutdown()
        {
            super.shutdown();
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

    public static class LabelProperty implements PropertySupport
    {

        @Override
        public Object getSessionState(Component c)
        {
            if (c instanceof JLabel)
            {
                JLabel jLabel = (JLabel) c;
                return new LabelState(jLabel.getText());
            }
            else
            {
                throw new IllegalArgumentException("invalid component");
            }
        }

        @Override
        public void setSessionState(Component c, Object state)
        {
            if (c instanceof JLabel && state instanceof LabelState)
            {
                LabelState labelState = (LabelState) state;
                JLabel jLabel = (JLabel) c;

                jLabel.setText(labelState.getText());
            }
            else
            {
                throw new IllegalArgumentException("invalid component");
            }
        }
    }

    public static class LabelState
    {
        private String text;

        public LabelState()
        {
        }

        public LabelState(String text)
        {
            this.text = text;
        }

        public String getText()
        {
            return text;
        }

        public void setText(String text)
        {
            this.text = text;
        }
    }

    @Before
    public void methodSetup()
    {
        System.err.println("This test generates logger warnings.  Ignore them.");
        CustomSessionStateApplication.launchAndWait(CustomSessionStateApplication.class);
    }

    @Test
    public void testCustomSessionState() throws Exception
    {
        final CustomSessionStateApplication app = Application.getInstance(CustomSessionStateApplication.class);
        assertTrue("CustomSessionStateApplication started", app.isReady());
        assertTrue(FIRST_VALUE.equals(app.label.getText()));

        Runnable doExit = new Runnable()
        {

            @Override
            public void run()
            {
                app.label.setText(SECOND_VALUE);
                app.exit();
            }  // override doesn't call System.exit
        };
        SwingUtilities.invokeAndWait(doExit);
        assertNotNull("getLocalStorage().load(sessionFile)", app.sessionObject);
        Map<String, Object> storage = (Map<String, Object>) app.sessionObject;

        String value = null;

        for (Map.Entry<String, Object> e : storage.entrySet())
        {
            if (e.getKey().contains(LABEL_NAME))
            {
                value = ((LabelState) e.getValue()).getText();
            }
        }

        assertTrue(SECOND_VALUE.equals(value));
    }
}
