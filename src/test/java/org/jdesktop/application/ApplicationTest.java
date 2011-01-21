/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/

package org.jdesktop.application;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.jdesktop.application.utils.PlatformType;

import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.jdesktop.application.utils.PlatformType;

/**
 * ApplicationTest.java
 *
 * This test depends on ResourceBundles and an image file:
 * <pre>
 * resources/
 * resources/black1x1.png
 * </pre>
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationTest
{

    public static class SimpleApplication extends WaitForStartupApplication
    {
        public boolean startupOnEDT;

        @Override
        protected void startup()
        {
            startupOnEDT = SwingUtilities.isEventDispatchThread();
            super.startup(); 
        }

        @Action()
        public void simpleAppAction()
        { }
    }

    @Before
    public void methodSetup()
    {
        SimpleApplication.launchAndWait(SimpleApplication.class);
    }


    private ApplicationContext getApplicationContext()
    {
        return Application.getInstance(SimpleApplication.class).getContext();
    }

    @Test
    public void testLaunch()
    {
        ApplicationContext ac = getApplicationContext();
        Application app = ac.getApplication();
        boolean isSimpleApp = app instanceof SimpleApplication;
        assertTrue("ApplicationContext.getApplication()", isSimpleApp);
        Class appClass = ac.getApplicationClass();
        assertSame("ApplicationContext.getApplicationClass()", SimpleApplication.class, appClass);
        assertTrue("SimpleApplication.startup() ran to completion", ((SimpleApplication) app).isStarted());
        assertTrue("SimpleApplication.startup() ran on the EDT", ((SimpleApplication) app).startupOnEDT);
    }

    @Test
    public void testGetResourceMap()
    {
        ApplicationContext ac = getApplicationContext();
        String bundleBaseName = getClass().getPackage().getName() + ".resources.";

        /* Check the Application ResourceMap chain */
        {
            ResourceMap appRM = ac.getResourceMap();
            assertNotNull("Application ResourceMap", appRM);
            /* Application ResourceMap rm should have a null parent
            * and three bundles:
            */
            String[] expectedBundleNames = {
                    bundleBaseName + "SimpleApplication",
                    bundleBaseName + "WaitForStartupApplication",
                    bundleBaseName + "Application"
            };
            String[] actualBundleNames = appRM.getBundleNames().toArray(new String[0]);
            assertArrayEquals(expectedBundleNames, actualBundleNames);
        }
        /* Check the ResourceMap for getClass() */
        {
            ResourceMap rm = ac.getResourceMap(getClass());
            assertNotNull(rm);
            assertEquals(bundleBaseName + "ApplicationTest", rm.getBundleNames().get(0));
        }
    }

    /**
     * Verify that the platform resource was initialized to "osx" or "default"
     * and that it can be reset.
     */
    @Test
    public void testPlatformResource()
    {
        ApplicationContext ctx = getApplicationContext();
        ResourceManager rm = ctx.getResourceManager();
        PlatformType platform = rm.getPlatform();
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isPlatformSet = false;
        for (String ptr : platform.getPatterns()) {
            if (osName.startsWith(ptr)) {
                isPlatformSet = true;
                break;
            }
        }
        assertTrue(isPlatformSet);
        
        try {
            ctx.getResourceManager().setPlatform(PlatformType.FREE_BSD);
            fail("It is forbidden to change the platform of a resource map.");
        } catch (Exception ignore) {
        }

        String currentPlatform = rm.getResourceMap().getString("currentPlatform");
        assertEquals(platform.getName(), currentPlatform);
    }

    private void checkActionName(String msg, javax.swing.Action action, String expectedValue)
    {
        String value = (String) (action.getValue(javax.swing.Action.NAME));
        assertEquals(msg + ".getValue(javax.swing.Action.NAME)", expectedValue, value);
    }

    public static class SimpleController
    {
        @Action()
        public void simpleControllerAction()
        { }
    }

    /**
     * Verify that the ActionMap for SimplController.class contains an Action
     * for "simpleControllerAction" and a parent ActionMap, defined by
     * SimpleControllerApp, that contains "simpleControllerAppAction".
     */
    @Test
    public void testGetActionMap()
    {
        SimpleController sc = new SimpleController();
        /* There should be four ActionMaps in the parent chain for sc, based on:
       * 0 - SimpleController.class
       * 1 - SimpleApplication.class
       * 1 - WaitForStartupApplication.class
       * 2 - Application.class  // parent of this one should be null
       */
        Class[] actionsClasses = {
                SimpleController.class,
                SimpleApplication.class,
                WaitForStartupApplication.class,
                Application.class
        };
        ApplicationActionMap actionMap = getApplicationContext().getActionMap(sc);
        int n = 0;
        for (Class actionsClass : actionsClasses)
        {
            assertNotNull("ActionMap " + actionsClass + " " + n, actionMap);
            assertSame("ActionMap actionsClass " + n, actionsClass, actionMap.getActionsClass());
            actionMap = (ApplicationActionMap) actionMap.getParent();
        }
        assertNull("Application actionMap parent", actionMap);

        actionMap = getApplicationContext().getActionMap(sc);
        String simpleControllerAction = "simpleControllerAction";
        String gamString = "Application.getActionMap(simpleController)";
        String gscaString = gamString + ".get(\"" + simpleControllerAction + "\")";
        javax.swing.Action scAction = actionMap.get(simpleControllerAction);
        assertNotNull(gscaString, scAction);
        checkActionName(gscaString, scAction, simpleControllerAction);

        String simpleAppAction = "simpleAppAction";
        String gsaaString = gamString + ".get(\"" + simpleAppAction + "\")";
        javax.swing.Action saAction = actionMap.get(simpleAppAction);
        assertNotNull(gsaaString, saAction);
        checkActionName(gsaaString, saAction, simpleAppAction);

        String noSuchAction = "noSuchAction";
        String gnsaString = gamString + ".get(\"" + noSuchAction + "\")";
        javax.swing.Action nsAction = actionMap.get(noSuchAction);
        assertNull(gnsaString, nsAction);
    }

    /**
     * Check the ActionMap returned by ApplicationContext.getActionMap(),
     * i.e. the global ApplicationMap.
     */
    @Test
    public void testGetAppActionMap()
    {
        /* In this case there should be just three ActionMaps in the
       * parent chain:
       * 0 - SimpleApplication.class
       * 1 - WaitForStartupApplication.class
       * 2 - Application.class  // parent of this one should be null
       */
        Class[] actionsClasses = {
                SimpleApplication.class,
                WaitForStartupApplication.class,
                Application.class
        };
        ApplicationActionMap actionMap = getApplicationContext().getActionMap();
        int n = 0;
        for (Class actionsClass : actionsClasses)
        {
            assertNotNull("ActionMap " + actionsClass + " " + n, actionMap);
            assertSame("ActionMap actionsClass " + n, actionsClass, actionMap.getActionsClass());
            actionMap = (ApplicationActionMap) actionMap.getParent();
        }
        assertNull("Application actionMap parent", actionMap);

        actionMap = getApplicationContext().getActionMap();
        String simpleControllerAction = "simpleControllerAction";
        String gamString = "Application.getActionMap()";
        String gscaString = gamString + ".get(\"" + simpleControllerAction + "\")";
        javax.swing.Action scAction = actionMap.get(simpleControllerAction);
        assertNull(gscaString, scAction);

        String simpleAppAction = "simpleAppAction";
        String gsaaString = gamString + ".get(\"" + simpleAppAction + "\")";
        javax.swing.Action saAction = actionMap.get(simpleAppAction);
        assertNotNull(gsaaString, saAction);
        checkActionName(gsaaString, saAction, simpleAppAction);
    }

    public static class StatefulController
    {
        private int n = 0;

        @Action()
        public void one()
        { n = 1; }

        @Action()
        public void two()
        { n = 2; }

        public int getN() { return n; }
    }

    private void checkSCActionMap(ApplicationActionMap appAM, Object actionsObject, Class actionsClass)
    {
        String msg = "ActionMap for " + actionsClass;
        assertNotNull(msg, appAM);
        assertSame(msg + ".getActionsObject()", actionsObject, appAM.getActionsObject());
        assertSame(msg + ".getActionsClass()", actionsClass, appAM.getActionsClass());
        assertNotNull(msg + ".getAction(\"one\")", appAM.get("one"));
        assertNotNull(msg + ".getAction(\"two\")", appAM.get("two"));
    }

    /**
     * Verify that getActionMap() caches per target actionsObject, not per @Actions
     * class.
     */
    @Test
    public void testGetActionMapPerObject()
    {
        StatefulController sc1 = new StatefulController();
        StatefulController sc2 = new StatefulController();
        StatefulController sc3 = new StatefulController();
        ApplicationContext ac = getApplicationContext();
        ApplicationActionMap am1 = ac.getActionMap(sc1);
        ApplicationActionMap am2 = ac.getActionMap(sc2);
        ApplicationActionMap am3 = ac.getActionMap(sc3);
        checkSCActionMap(am1, sc1, StatefulController.class);
        checkSCActionMap(am2, sc2, StatefulController.class);
        checkSCActionMap(am3, sc3, StatefulController.class);
        String oneActionMapPerObject = "one ActionMap per actionsObject";
        assertTrue(oneActionMapPerObject, am1 != am2);
        assertTrue(oneActionMapPerObject, am1 != am3);
        assertTrue(oneActionMapPerObject, am2 != am3);
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "not used");
        am1.get("one").actionPerformed(event);
        am2.get("two").actionPerformed(event);
        assertEquals("StatefulController.getN(), after calling @Action sc1.one()", 1, sc1.getN());
        assertEquals("StatefulController.getN(), after calling @Action sc2.two()", 2, sc2.getN());
        assertEquals("StatefulController.getN(), no @Actions called", 0, sc3.getN());
    }


    public static class ActionMapObject
    {
        int n;

        ActionMapObject(int n) { this.n = n; }

        @Action()
        public void anAction()
        { n = -1; }
    }

    /**
     * Verify that ActionMaps are GC'd when their target actionsObject is
     * no longer referred to and their actions are no longer in use (no
     * longer referred to).
     */
    @Test
    public void testActionMapGC()
    {
        ApplicationContext ac = getApplicationContext();
        List<Reference<ActionMapObject>> refs = new ArrayList<Reference<ActionMapObject>>();
        for (int i = 0; i < 256; i++)
        {
            ActionMapObject amo = new ActionMapObject(i);
            refs.add(new WeakReference<ActionMapObject>(amo));
            ApplicationActionMap appAM = ac.getActionMap(amo);
            assertNotNull(appAM);
            assertNotNull(appAM.get("anAction"));
            assertSame(amo, appAM.getActionsObject());
            assertEquals(i, amo.n);
        }
        /* GC should clear all of the references to ActionMapObjects because
       * they're no longer strongly reachable, i.e. the framework isn't
       * hanging on to them.
       */
        System.gc();
        for (Reference ref : refs)
        {
            assertNull("Reference to ApplictionActionMap actionsObject", ref.get());
        }
    }

    /**
     * Verify that an Action's target is -not- GC'd if a reference to the
     * Action persists even after no direct references to the
     * actionsObject target exist.
     */
    @Test
    public void testActionMapNoGC()
    {
        ApplicationContext ac = getApplicationContext();
        List<Reference<ActionMapObject>> refs = new ArrayList<Reference<ActionMapObject>>();
        List<ApplicationAction> actions = new ArrayList<ApplicationAction>();
        for (int i = 0; i < 256; i++)
        {
            ActionMapObject amo = new ActionMapObject(i);
            refs.add(new WeakReference<ActionMapObject>(amo));
            ApplicationActionMap appAM = ac.getActionMap(amo);
            assertNotNull(appAM);
            actions.add((ApplicationAction) (appAM.get("anAction")));
            assertSame(amo, appAM.getActionsObject());
            assertEquals(i, amo.n);
        }
        /* GC should -not- clear all of the references to ActionMapObjects because
       * the ApplicationAction objects still refer (indirectly and strongy)
       * to them.
       */
        System.gc();
        for (Reference ref : refs)
        {
            assertNotNull("Reference to ApplictionActionMap actionsObject", ref.get());
        }
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "not used");
        int i = 0;
        for (ApplicationAction action : actions)
        {
            action.actionPerformed(event);
            ActionMapObject amo = refs.get(i).get();
            assertEquals("after calling ActionMapObject.anAction()", -1, amo.n);
        }
    }
}
