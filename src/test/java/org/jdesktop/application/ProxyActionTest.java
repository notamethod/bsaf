/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/

package org.jdesktop.application;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.ResourceBundle;

/*
 * This test depends on the resources/ProxyActions.properties ResourceBundle
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ProxyActionTest
{
    @ProxyActions({"simple"})
    public static class SimpleApplication extends WaitForStartupApplication
    {
        public boolean startupOnEDT;

        @Override
        protected void startup()
        {
            super.startup();
            startupOnEDT = SwingUtilities.isEventDispatchThread();
        }

        @Action()
        public void simpleAppAction()
        { }
    }

    private ApplicationContext getContext()
    {
        return Application.getInstance(SimpleApplication.class).getContext();
    }

    @BeforeClass
    public static void unitSetup()
    {
        SimpleApplication.launchAndWait(SimpleApplication.class);
    }

    @ProxyActions({"testAction"})
    public static class ProxyTestActions
    {
    }

    public static class TargetActions
    {
        private boolean actionEnabled = true;
        int testActionCounter = 0;

        @Action(enabledProperty = "testActionEnabled")
        public void testAction()
        {
            testActionCounter += 1;
        }

        public boolean isTestActionEnabled() { return actionEnabled; }

        public void setTestActionEnabled(boolean newValue)
        {
            boolean oldValue = actionEnabled;
            if (oldValue != newValue)
            {
                this.actionEnabled = newValue;
                pcs.firePropertyChange("testActionEnabled", oldValue, newValue);
            }
        }

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public void addPropertyChangeListener(PropertyChangeListener listener)
        {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener)
        {
            pcs.removePropertyChangeListener(listener);
        }
    }

    private ResourceMap resourceMap()
    {
        String bundleBaseName = getClass().getPackage().getName() + ".resources.ProxyTestActions";
        /* If the ResourceBundle can't be found, getBundle() will throw an exception.
       * ResourceMap isn't supposed to complain if it can't find a
       * ResourceBundle however the tests that follow expect
       * Basic ResourceBundle to exist.
       */
        ResourceBundle.getBundle(bundleBaseName);
        ClassLoader classLoader = getClass().getClassLoader();
        ResourceMap resourceMap = new ResourceMap(null, classLoader, bundleBaseName);
        return resourceMap;
    }

    private ApplicationActionMap proxyActionMap()
    {
        Object actionsObject = new ProxyTestActions();
        return new ApplicationActionMap(getContext(), ProxyTestActions.class, actionsObject, resourceMap());
    }

    private ApplicationActionMap targetActionMap()
    {
        Object actionsObject = new TargetActions();
        return new ApplicationActionMap(getContext(), TargetActions.class, actionsObject, resourceMap());
    }

    private void checkUnboundProxyAMAction(ApplicationActionMap appAM, String actionKey, String actionName)
    {
        javax.swing.Action action = appAM.get(actionKey);
        String pamG = "proxyActionMap().get(\"" + actionKey + "\")";
        assertTrue(pamG, action instanceof ApplicationAction);
        assertFalse(pamG + ".isEnabled()", action.isEnabled());
        ApplicationAction appAction = (ApplicationAction) action;
        assertEquals(actionName, appAction.getValue(javax.swing.Action.NAME));
        assertNull(pamG + ".getProxyBinding()", appAction.getProxy());
    }

    // see resources/ProxyAction.properties
    @Test
    public void testBasics()
    {
        ApplicationActionMap appAM = proxyActionMap();
        assertEquals("appAM.getProxyActions.size()", 1, appAM.getProxyActions().size());
        checkUnboundProxyAMAction(appAM, "testAction", "testAction");
        assertTrue(targetActionMap().get("testAction") instanceof ApplicationAction);
    }

    private static String getShortDescription(javax.swing.Action a)
    {
        return (String) (a.getValue(javax.swing.Action.SHORT_DESCRIPTION));
    }

    private static void setShortDescription(javax.swing.Action a, String s)
    {
        a.putValue(javax.swing.Action.SHORT_DESCRIPTION, s);
    }

    private static String getLongDescription(javax.swing.Action a)
    {
        return (String) (a.getValue(javax.swing.Action.LONG_DESCRIPTION));
    }

    private static void setLongDescription(javax.swing.Action a, String s)
    {
        a.putValue(javax.swing.Action.LONG_DESCRIPTION, s);
    }

    private void checkActionProperties(String msg, javax.swing.Action action, boolean enabled, String shortD, String longD)
    {
        assertEquals(msg + " isEnabled()", enabled, action.isEnabled());
        assertEquals(msg + " shortDescription", shortD, getShortDescription(action));
        assertEquals(msg + " longDescription", longD, getLongDescription(action));
    }

    @Test
    public void testBinding()
    {
        ApplicationActionMap proxyAM = proxyActionMap();
        ApplicationActionMap targetAM = targetActionMap();
        ApplicationAction targetAction = (ApplicationAction) (targetAM.get("testAction"));
        targetAction.setEnabled(false);
        setShortDescription(targetAction, "shortDescription");
        setLongDescription(targetAction, "longDescription");
        String[] proxyActionKeys = {"testAction"};

        /* Bind each proxyAction to targetAction and verify that proxyAction's
       * enabled, long/shortDescription properties change
       */
        checkActionProperties("testAction", targetAction, false, "shortDescription", "longDescription");
        for (String proxyActionKey : proxyActionKeys)
        {
            ApplicationAction proxyAction = (ApplicationAction) (proxyAM.get(proxyActionKey));
            proxyAction.setProxy(targetAction);
            checkActionProperties("ProxyAction " + proxyActionKey, proxyAction, false, "shortDescription", "longDescription");
        }
        // Check for "How could this happen?" failure mode:
        checkActionProperties("testAction", targetAction, false, "shortDescription", "longDescription");

        /* Change the target action's properties and verify that the
       * proxyActions change too.
       */
        targetAction.setEnabled(true);
        setShortDescription(targetAction, "shortD");
        setLongDescription(targetAction, "longD");
        checkActionProperties("testAction", targetAction, true, "shortD", "longD");
        for (String proxyActionKey : proxyActionKeys)
        {
            ApplicationAction proxyAction = (ApplicationAction) (proxyAM.get(proxyActionKey));
            checkActionProperties("ProxyAction " + proxyActionKey, proxyAction, true, "shortD", "longD");
        }
        // Check for "How could this happen?" failure mode:
        checkActionProperties("testAction", targetAction, true, "shortD", "longD");

        /* Verify that calling proxyAction.actionPerformed() calls the targetAction's
       * @Action method: TargetActions.testAction();
       */
        TargetActions targetActions = (TargetActions) (targetAM.getActionsObject());
        targetActions.testActionCounter = 0;
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "not used");
        for (String proxyActionKey : proxyActionKeys)
        {
            ApplicationAction proxyAction = (ApplicationAction) (proxyAM.get(proxyActionKey));
            proxyAction.actionPerformed(event);
        }
        assertEquals(proxyActionKeys.length, targetActions.testActionCounter);

        /* Unbind the proxyActions and verify that they're disabled, and that they
       * no longer track the targetAction.
       */
        for (String proxyActionKey : proxyActionKeys)
        {
            ApplicationAction proxyAction = (ApplicationAction) (proxyAM.get(proxyActionKey));
            proxyAction.setProxy(null);
            assertFalse(proxyAction.isEnabled());
        }
        targetAction.setEnabled(true);  // shouldn't affect - now unbound - proxyActions
        targetActions.testActionCounter = 0;
        assertTrue(targetAction.isEnabled());
        for (String proxyActionKey : proxyActionKeys)
        {
            ApplicationAction proxyAction = (ApplicationAction) (proxyAM.get(proxyActionKey));
            proxyAction.actionPerformed(event);
            assertFalse(proxyAction.isEnabled());
        }
        assertEquals(0, targetActions.testActionCounter);
    }

    private void checkProxyAction(String name, List<ApplicationAction> proxyActions)
    {
        boolean matchFound = false;
        for (ApplicationAction pa : proxyActions)
        {
            if (pa.getName().equals(name))
            {
                matchFound = true;
                break;
            }
        }
        assertTrue("expected proxyAction named \"" + name + "\"", matchFound);
    }

    /**
     * Verify that ApplicationActionMap#getProxyActions() returns
     * a list of proxyActions that include the cut/copy/paste/delete
     * proxy actions defined in the Application class, as well as the
     * ones added by our app subclass and by ProxyTestActions.
     */
    @Test
    public void testGetProxyActions()
    {
        ApplicationContext ac = Application.getInstance(SimpleApplication.class).getContext();
        // check the actions in the global SimpleApplication action map
        List<ApplicationAction> globalProxyActions = ac.getActionMap().getProxyActions();
        String[] globalProxyActionNames = {
                "simple", "cut", "copy", "paste", "delete"
        };
        for (String proxyActionName : globalProxyActionNames)
        {
            checkProxyAction(proxyActionName, globalProxyActions);
        }
        // check the actions in SimpleApplication's ProxyTestActions action map
        Object pta = new ProxyTestActions();
        List<ApplicationAction> ptaProxyActions = ac.getActionMap(pta.getClass(), pta).getProxyActions();
        String[] ptaProxyActionNames = {
                "testAction", "simple", "cut", "copy", "paste", "delete"
        };
        for (String proxyActionName : ptaProxyActionNames)
        {
            checkProxyAction(proxyActionName, ptaProxyActions);
        }

    }

}
