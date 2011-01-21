/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/

package org.jdesktop.application;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;

/**
 * Test the Actions that are defined by the Application class:
 * quit, cut, copy, paste, delete.  Depends on ResourceBundle
 * resources/DefaultActionsApplication.properties
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationActionsTest
{

    public static class DefaultActionsApplication extends WaitForStartupApplication
    {
        boolean deleteCalled = false;
        boolean selected = true;

        @Action
        public void delete()
        { deleteCalled = true; }

        @Action(selectedProperty="selected")
        public void selectableAction() {

        }

        public boolean isSelected() {
            return selected;
        }

    }

    @BeforeClass
    public static void unitSetup()
    {
        DefaultActionsApplication.launchAndWait(DefaultActionsApplication.class);
    }


    private String actionText(ApplicationAction action)
    {
        return (String) action.getValue(ApplicationAction.NAME);
    }

    private String actionShortDescription(ApplicationAction action)
    {
        return (String) action.getValue(ApplicationAction.SHORT_DESCRIPTION);
    }

    private void checkDefaultAction(String actionName, ApplicationAction action)
    {
        assertNotNull(actionName, action);
        assertNotNull(actionName + ".Action.text", actionText(action));
        assertNotNull(actionName + ".Action.shortDescription", actionShortDescription(action));
    }

    private ApplicationActionMap actionMap()
    {
        return Application.getInstance(DefaultActionsApplication.class).getContext().getActionMap();
    }

    /**
     * Verify that the quit, cut, copy, paste, and delete actions exist,
     * and that they all text, shortDescription properties.
     */
    @Test
    public void testBasics()
    {
        String[] actionNames = {"quit", "cut", "copy", "paste", "delete"};
        ApplicationActionMap appAM = actionMap();
        assertNotNull("Global ActionMap", appAM);
        for (String actionName : actionNames)
        {
            ApplicationAction action = (ApplicationAction) (appAM.get(actionName));
            checkDefaultAction(actionName, action);
        }
    }

    /**
     * Verify that the quit action resources defined in
     * resources/DefaultActionsApplication.properties override
     * the defaults inherited from the Application ResourceBundle.
     */
    @Test
    public void testApplicationResourceOverrides()
    {
        ApplicationActionMap appAM = actionMap();
        assertSame("global ActionMap.actionsClass", DefaultActionsApplication.class, appAM.getActionsClass());
        ApplicationAction action = (ApplicationAction) (appAM.get("quit"));
        assertEquals("quit.Action.text", "Q", actionText(action));
        assertEquals("quit.Action.shortDescription", "Q", actionShortDescription(action));
    }

    private DefaultActionsApplication application()
    {
        return Application.getInstance(DefaultActionsApplication.class);
    }

    /**
     * Verify that the DefaultActionsApplication.delete @Action
     * shadows (but doesn't replace) the ProxyAction defined by
     * the Application class.
     */
    @Test
    public void testApplicationActionOverrides()
    {
        ApplicationActionMap appAM = actionMap();
        ApplicationAction action = (ApplicationAction) (appAM.get("delete"));
        assertEquals("delete.Action.text", "D", actionText(action));
        assertEquals("delete.Action.shortDescription", "D", actionShortDescription(action));
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "not used");
        action.actionPerformed(event);
        assertTrue("DefaultActionsApplication.deleteCalled", application().deleteCalled);

        /* Looking up the "delete" action in this ActionMap should produce
       * the default one, i.e. the one defined by Application. Its resources
       * should have been overidden as above, but it should be a separate
       * Action object.
       */
        ApplicationActionMap parentAM = (ApplicationActionMap) actionMap().getParent();
        ApplicationAction parentAction = (ApplicationAction) (parentAM.get("delete"));
        assertEquals("delete.Action.text", "D", actionText(parentAction));
        assertEquals("delete.Action.shortDescription", "D", actionShortDescription(parentAction));
        assertNotSame(action, parentAction);
    }

    @Test
    public void testSelectableAction() {
        ApplicationActionMap actionMap = actionMap();
        JCheckBox checkBox = new JCheckBox(actionMap.get("selectableAction"));
        assertTrue(checkBox.isSelected());
    }

}
