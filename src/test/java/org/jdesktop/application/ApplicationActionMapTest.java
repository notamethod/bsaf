/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/

package org.jdesktop.application;

import static org.junit.Assert.*;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

/*
 * This test depends on ResourceBundles and an image file:
 * <pre>
 * resources/Controller.properties
 * resources/black1x1.png
 * </pre>
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationActionMapTest
{


    public static class SimpleActions
    {
        @Action
        public void trivialAction()
        { }  // no properties in Controller.properties

        @Action
        public void allActionProperties()
        { }  // all possible properties in Controller.properties

        @Action(name = "alternateActionName")
        public void notTheActionName()
        { }

        // See testActionMnemonicProperties
        @Action
        public void checkActionMnemonics0()
        { }

        @Action
        public void checkActionMnemonics1()
        { }

        @Action
        public void checkActionMnemonics5()
        { }

        // See testActionPerformed()
        public int actionCounter = 0;

        @Action
        public void incrementActionCounter()
        {
            actionCounter += 1;
        }

        // See testActionEnabled()
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public void addPropertyChangeListener(PropertyChangeListener listener)
        {
            pcs.addPropertyChangeListener(listener);
        }

        private boolean actionEnabled = true;

        public boolean isActionEnabled() { return actionEnabled; }

        public void setActionEnabled(boolean newValue)
        {
            boolean oldValue = actionEnabled;
            if (oldValue != newValue)
            {
                this.actionEnabled = newValue;
                pcs.firePropertyChange("actionEnabled", oldValue, newValue);
            }
        }

        @Action(enabledProperty = "actionEnabled")
        public void testActionEnabled()
        { }

        // see testActionParametersA()
        public ActionEvent lastActionEvent = null;

        @Action
        void actionParametersA(ActionEvent e)
        {
            lastActionEvent = e;
        }

        @Action
        void actionParametersB(javax.swing.Action a)
        {
            a.putValue("testKey", "testValue");
        }

        @Action
        void actionParametersAB(ActionEvent e, javax.swing.Action a)
        {
            lastActionEvent = e;
            a.putValue("testKey", "testValue");
        }

        @Action
        void actionParametersZ(
                ActionEvent actionEvent, javax.swing.Action action, ActionMap actionMap, ResourceMap resourceMap)
        {
            lastActionEvent = actionEvent;
            action.putValue("testKey", "testValue");
            actionMap.put("testKey", action);
            assertNotNull("actionParametersZ resourceMap parameter", resourceMap);
            assertEquals("actionParametersZ", resourceMap.getString("actionParametersZ.text"));
        }

        // see testJavaDocExample
        PrintWriter helloWorldWriter = null;

        @Action
        void Hello()
        { helloWorldWriter.print("Hello "); }

        @Action
        void World()
        { helloWorldWriter.print("World"); }

    }

    private ResourceMap emptyResourceMap()
    {
        ClassLoader classLoader = getClass().getClassLoader();
        ResourceMap resourceMap = new ResourceMap(null, classLoader, "noSuchBundle");
        return resourceMap;
    }

    private ResourceMap resourceMap()
    {
        String bundleBaseName = getClass().getPackage().getName() + ".resources.Controller";
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

    private ApplicationActionMap simpleActionMap(ResourceMap resourceMap)
    {
        SimpleActions actionsObject = new SimpleActions();
        ApplicationContext ctx = new ApplicationContext();
        return new ApplicationActionMap(ctx, SimpleActions.class, actionsObject, resourceMap);
    }

    private ApplicationActionMap simpleActionMap()
    {
        return simpleActionMap(emptyResourceMap());
    }

    @Test
    public void testGetActionsObject()
    {
        ApplicationActionMap appAM = simpleActionMap();
        boolean isSimpleActionsObject = appAM.getActionsObject() instanceof SimpleActions;
        assertTrue("appAM.getActionsObject() instanceof SimpleActionsObject", isSimpleActionsObject);
        boolean isSingleton = appAM.getActionsObject() == appAM.getActionsObject();
        assertTrue("appAM.getActionsObject() returns singleton", isSingleton);
    }

    public static class NoActions
    {
        NoActions(String s) {} // ApplicationActionMap can't auto-construct this one
    }

    @Test
    public void testTrivialAction()
    {
        ApplicationActionMap appAM = simpleActionMap();
        checkName(appAM, "trivialAction", "trivialAction");
    }

    private String appAMGetString(String actionKey)
    {
        return "ApplicationActionMap.get(\"" + actionKey + "\")";
    }

    private javax.swing.Action checkAction(ApplicationActionMap appAM, String actionKey)
    {
        javax.swing.Action action = appAM.get(actionKey);
        assertNotNull(appAMGetString(actionKey), action);
        return action;
    }

    private void checkName(ApplicationActionMap appAM, String actionKey, String expectedValue)
    {
        javax.swing.Action action = checkAction(appAM, actionKey);
        String value = (String) (action.getValue(javax.swing.Action.NAME));
        assertEquals(appAMGetString(actionKey) + ".getValue(javax.swing.Action.NAME)", expectedValue, value);
    }

    private void checkShortDescription(ApplicationActionMap appAM, String actionKey, String expectedValue)
    {
        javax.swing.Action action = checkAction(appAM, actionKey);
        String value = (String) (action.getValue(javax.swing.Action.SHORT_DESCRIPTION));
        assertEquals(appAMGetString(actionKey) + ".getValue(javax.swing.Action.SHORT_DESCRIPTION)", expectedValue, value);
    }

    private void checkLongDescription(ApplicationActionMap appAM, String actionKey, String expectedValue)
    {
        javax.swing.Action action = checkAction(appAM, actionKey);
        String value = (String) (action.getValue(javax.swing.Action.LONG_DESCRIPTION));
        assertEquals(appAMGetString(actionKey) + ".getValue(javax.swing.Action.LONG_DESCRIPTION)", expectedValue, value);
    }

    private void checkIcon(ApplicationActionMap appAM, String actionKey)
    {
        javax.swing.Action action = checkAction(appAM, actionKey);
        Icon icon = (Icon) (action.getValue(javax.swing.Action.SMALL_ICON));
        String msg = appAMGetString(actionKey) + ".getValue(javax.swing.Action.SMALL_ICON)";
        assertNotNull(msg, icon);
        assertEquals(msg + ".getIconWidth()", 1, icon.getIconWidth());
        assertEquals(msg + ".getIconHeight()", 1, icon.getIconHeight());
    }

    private void checkNullIcon(ApplicationActionMap appAM, String actionKey)
    {
        javax.swing.Action action = checkAction(appAM, actionKey);
        Icon icon = (Icon) (action.getValue(javax.swing.Action.SMALL_ICON));
        String msg = appAMGetString(actionKey) + ".getValue(javax.swing.Action.SMALL_ICON)";
        assertNull(msg, icon);
    }

    private void checkCommandKey(ApplicationActionMap appAM, String actionKey, String expectedValue)
    {
        javax.swing.Action action = checkAction(appAM, actionKey);
        String value = (String) (action.getValue(javax.swing.Action.ACTION_COMMAND_KEY));
        assertEquals(appAMGetString(actionKey) + ".getValue(javax.swing.Action.ACTION_COMMAND_KEY)", expectedValue, value);
    }

    private void checkAcceleratorKey(ApplicationActionMap appAM, String actionKey, KeyStroke expectedValue)
    {
        javax.swing.Action action = checkAction(appAM, actionKey);
        KeyStroke value = (KeyStroke) (action.getValue(javax.swing.Action.ACCELERATOR_KEY));
        assertEquals(appAMGetString(actionKey) + ".getValue(javax.swing.Action.ACCELERATOR_KEY)", expectedValue, value);
    }

    private void checkMnemonicKey(ApplicationActionMap appAM, String actionKey, Integer expectedValue)
    {
        javax.swing.Action action = checkAction(appAM, actionKey);
        Integer value = (Integer) (action.getValue(javax.swing.Action.MNEMONIC_KEY));
        assertEquals(appAMGetString(actionKey) + ".getValue(javax.swing.Action.MNEMONIC_KEY)", expectedValue, value);
    }

    /* This javax.swing.Action constants is only 
     * defined in Mustang (1.6), see 
     * http://download.java.net/jdk6/docs/api/javax/swing/Action.html
     */
    private static final String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey";

    private void checkMnemonicIndex(ApplicationActionMap appAM, String actionKey, Integer expectedValue)
    {
        javax.swing.Action action = checkAction(appAM, actionKey);
        Integer value = (Integer) (action.getValue(/*javax.swing.Action.*/DISPLAYED_MNEMONIC_INDEX_KEY));
        assertEquals(appAMGetString(actionKey) + ".getValue(/*javax.swing.Action.*/DISPLAYED_MNEMONIC_INDEX_KEY)", expectedValue, value);
    }

    @Test
    public void testActionProperties()
    {
        ApplicationActionMap appAM = simpleActionMap(resourceMap());
        String allActionProperties = "allActionProperties";
        checkName(appAM, allActionProperties, "All");
        checkShortDescription(appAM, allActionProperties, "short");
        checkLongDescription(appAM, allActionProperties, "long");
        checkIcon(appAM, allActionProperties);
        checkCommandKey(appAM, allActionProperties, "AllCommand");
        KeyStroke controlA = KeyStroke.getKeyStroke("control A");
        checkAcceleratorKey(appAM, allActionProperties, controlA);
        checkMnemonicKey(appAM, allActionProperties, new Integer(controlA.getKeyCode()));
        String checkActionMnemonics0 = "checkActionMnemonics0";
    }

    @Test
    public void testActionMnemonicProperties()
    {
        ApplicationActionMap appAM = simpleActionMap(resourceMap());
        String[] actionMapKeys = {"checkActionMnemonics0", "checkActionMnemonics1", "checkActionMnemonics5"};
        String[] actionNames = {"File", "Exit", "Save As"};
        int[] mnemonicKeys = {KeyEvent.VK_F, KeyEvent.VK_X, KeyEvent.VK_A};
        int[] mnemonicIndices = {0, 1, 5};
        for (int i = 0; i < actionMapKeys.length; i++)
        {
            String actionMapKey = actionMapKeys[i];
            checkName(appAM, actionMapKey, actionNames[i]);
            checkMnemonicKey(appAM, actionMapKey, mnemonicKeys[i]);
            checkMnemonicIndex(appAM, actionMapKey, mnemonicIndices[i]);
            checkShortDescription(appAM, actionMapKey, null);
            checkLongDescription(appAM, actionMapKey, null);
            checkCommandKey(appAM, actionMapKey, null);
        }
    }

    @Test
    public void testActionAnnotationKeyProperty()
    {
        ApplicationActionMap appAM = simpleActionMap(resourceMap());
        String alternateActionName = "alternateActionName";
        checkName(appAM, alternateActionName, "All");
        checkShortDescription(appAM, alternateActionName, "short");
        checkLongDescription(appAM, alternateActionName, "long");
        checkIcon(appAM, alternateActionName);
        checkCommandKey(appAM, alternateActionName, "AllCommand");
        KeyStroke controlA = KeyStroke.getKeyStroke("control A");
        checkAcceleratorKey(appAM, alternateActionName, controlA);
        KeyStroke A = KeyStroke.getKeyStroke("A");
        checkMnemonicKey(appAM, alternateActionName, new Integer(A.getKeyCode()));
    }

    @Test
    public void testActionPerformed()
    {
        ApplicationActionMap appAM = simpleActionMap(resourceMap());
        SimpleActions actionsObject = (SimpleActions) (appAM.getActionsObject());
        javax.swing.Action action = checkAction(appAM, "incrementActionCounter");
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "not used");
        actionsObject.actionCounter = 0;
        action.actionPerformed(event);
        action.actionPerformed(event);
        action.actionPerformed(event);
        assertEquals("Called SimpleActionsObject.incrementActionCounter() 3X", 3, actionsObject.actionCounter);
        // TBD - verify that the event gets passed along if the @Action method
        // has an event parameter
    }


    /**
     * Verify that setting the "enabled" property of the
     * javax.swing.Action created for @Action SimpleActions.testActionEnabled,
     * calls SimpleActions.setActionEnabled().
     */
    @Test
    public void testActionEnabledBasics()
    {
        ApplicationActionMap appAM = simpleActionMap();
        SimpleActions actionsObject = (SimpleActions) (appAM.getActionsObject());
        javax.swing.Action action = checkAction(appAM, "testActionEnabled");
        action.setEnabled(false);
        assertFalse(action.isEnabled());
        assertFalse(actionsObject.isActionEnabled());
        action.setEnabled(true);
        assertTrue(action.isEnabled());
        assertTrue(actionsObject.isActionEnabled());
    }

    /**
     * Verify that setting the SimpleActions.actionEnabled property
     * changes the action object's enabled property and fires its
     * PropertyChangeListener.
     */
    @Test
    public void testActionEnabled()
    {
        ApplicationActionMap appAM = simpleActionMap();
        SimpleActions actionsObject = (SimpleActions) (appAM.getActionsObject());
        javax.swing.Action action = checkAction(appAM, "testActionEnabled");
        PropertyChangeListener actionEnabledChangesToFalse = new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                assertEquals("PropertyChangeEvent.propertyName", "enabled", event.getPropertyName());
                assertTrue("PropertyChangeEvent.oldValue", (Boolean) (event.getOldValue()));
                assertFalse("PropertyChangeEvent.newValue", (Boolean) (event.getNewValue()));
            }
        };
        action.addPropertyChangeListener(actionEnabledChangesToFalse);
        String msg = "SimpleActionsObject.testActionEnabled Action - action.isEnabled() ";
        assertTrue(msg + "before calling actionsObject.setActionEnabled(false)", action.isEnabled());
        actionsObject.setActionEnabled(false);
        assertFalse(msg + "after calling actionsObject.setActionEnabled(false)", action.isEnabled());
    }

    // TBD check the ApplicationActionMap constructor that takes an actionsObject
    // TBD check the ApplicationActionMap constructor with null ResourceMap


    private ActionEvent createActionEvent()
    {
        return new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "testActionParameters");
    }

    /* @Action void actionParametersA(ActionEvent e) // see SimpleActions
     * 
     * Verify that the Action created for SimpleActions.actionParametersA()
     * passes the ActionEvent parameter along.
     */
    @Test
    public void testActionParametersA()
    {
        ApplicationActionMap appAM = simpleActionMap(resourceMap());
        // verify that the Action is in the SimpleActions ActionMap
        javax.swing.Action action = appAM.get("actionParametersA");
        assertNotNull("appAM.get(actionParametersA)", action);
        // call the actionPerformedMethod and check the parameters
        SimpleActions simpleActions = (SimpleActions) (appAM.getActionsObject());
        simpleActions.lastActionEvent = null;
        ActionEvent actionEventA = createActionEvent();
        action.actionPerformed(actionEventA);
        assertEquals(actionEventA, simpleActions.lastActionEvent);
    }

    /* @Action void actionParametersB(javax.swing.Action a) {
     * 
     * Verify that the Action created for SimpleActions.actionParametersB()
     * passes the Action parameter along.
     */
    @Test
    public void testActionParametersB()
    {
        ApplicationActionMap appAM = simpleActionMap(resourceMap());
        // verify that the Action is in the SimpleActions ActionMap
        javax.swing.Action action = appAM.get("actionParametersB");
        assertNotNull("appAM.get(actionParametersB)", action);
        // call the actionPerformedMethod and check the parameters
        action.putValue("testKey", null);
        ActionEvent actionEventB = createActionEvent();
        action.actionPerformed(actionEventB);
        assertEquals("testValue", action.getValue("testKey"));
    }

    /* @Action void actionParametersAB(ActionEvent e, Action a)
     * 
     * Verify that the Action created for SimpleActions.actionParametersAB()
     * passes the ActionEvent and Action parameters along.
     */
    @Test
    public void testActionParametersAB()
    {
        ApplicationActionMap appAM = simpleActionMap(resourceMap());
        // verify that the Action is in the SimpleActions ActionMap
        javax.swing.Action action = appAM.get("actionParametersAB");
        assertNotNull("appAM.get(actionParametersAB)", action);
        // call the actionPerformedMethod and check the parameters
        ActionEvent actionEventAB = createActionEvent();
        action.putValue("testKey", null);
        SimpleActions simpleActions = (SimpleActions) (appAM.getActionsObject());
        simpleActions.lastActionEvent = null;
        action.actionPerformed(actionEventAB);
        assertEquals(actionEventAB, simpleActions.lastActionEvent);
        assertEquals("testValue", action.getValue("testKey"));
    }

    /*
     * @Action void actionParametersZ(
     *     ActionEvent actionEvent, javax.swing.Action action, ActionMap actionMap, resourceMap resourceMap)
     * 
     * Verify that the Action created for SimpleActions.actionParametersZ()
     * passes all of the specified parameters along.
     */
    @Test
    public void testActionParametersZ()
    {
        ApplicationActionMap appAM = simpleActionMap(resourceMap());
        // verify that the Action is in the SimpleActions ActionMap
        javax.swing.Action action = appAM.get("actionParametersZ");
        assertNotNull("appAM.get(actionParametersZ)", action);
        // call the actionPerformedMethod and check the parameters
        ActionEvent actionEventZ = createActionEvent();
        action.putValue("testKey", null);
        SimpleActions simpleActions = (SimpleActions) (appAM.getActionsObject());
        simpleActions.lastActionEvent = null;
        action.actionPerformed(actionEventZ);
        assertEquals(actionEventZ, simpleActions.lastActionEvent);
        assertEquals("testValue", action.getValue("testKey"));
        assertEquals(action, appAM.get("testKey"));
    }

    /* Check an example from the ApplicationActionMap class javadoc.
     */
    @Test
    public void testJavaDocExample1()
    {
        ApplicationActionMap appAM = simpleActionMap(resourceMap());
        SimpleActions simpleActions = (SimpleActions) (appAM.getActionsObject());
        StringWriter sw = new StringWriter();
        simpleActions.helloWorldWriter = new PrintWriter(sw);
        ActionEvent actionEvent =
                new ActionEvent("no source", ActionEvent.ACTION_PERFORMED, "");
        appAM.get("Hello").actionPerformed(actionEvent);
        appAM.get("World").actionPerformed(actionEvent);
        assertEquals("Hello World", sw.toString());
    }
}

