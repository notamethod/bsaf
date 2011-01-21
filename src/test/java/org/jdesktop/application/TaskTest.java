/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/

package org.jdesktop.application;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the Task classes.  This test depends on the follow resource bundles:
 * <pre>
 * resources/DoNothingTask.properties
 * </pre>
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class TaskTest
{


    public static class SimpleApplication extends WaitForStartupApplication
    {
        public boolean startupOnEDT;
    }

    @Before
    public void methodStartup()
    {
        SimpleApplication.launchAndWait(SimpleApplication.class);
    }

    public static class DoNothingTask extends Task<Void, Void>
    {
        DoNothingTask()
        {
            super(Application.getInstance(SimpleApplication.class), "DoNothingTask");
        }

        protected Void doInBackground()
        {
            return null;
        }
    }

    /* Sanity check: verify that SimpleApp launched */
    private void appSanityCheck()
    {
        Application app = Application.getInstance(SimpleApplication.class);
        boolean isSimpleApp = app instanceof SimpleApplication;
        assertTrue("Application.getInstance() should be a SimpleApp: " + app, isSimpleApp);
    }

    @Test
    public void testResourceLoading()
    {
        appSanityCheck();
        /* Check resource loading basics */
        Task task = new DoNothingTask();
        ResourceMap resourceMap = task.getResourceMap();
        assertNotNull("DoNothingTask.getResourceMap()", resourceMap);
        String resourceName = task.resourceName("justAResource");
        assertEquals("DoNothingTask.justAResource", resourceName);
        assertEquals("just a resource", resourceMap.getString(resourceName));
        assertEquals("the title", task.getTitle());
        assertEquals("the description", task.getDescription());
        assertEquals("the initial message", task.getMessage());
        /* Check the message method */
        task.message("messageParam0");
        assertEquals("message 0", task.getMessage());
        task.message("messageParam1", "foo");
        assertEquals("message 1 foo", task.getMessage());
        task.message("messageParam2", "foo", "bar");
        assertEquals("message 1 foo 2 bar", task.getMessage());
    }

    public static class NoResourcesTask extends Task<Void, Void>
    {
        NoResourcesTask()
        {
            super(Application.getInstance(SimpleApplication.class), null, "");
        }

        protected Void doInBackground()
        {
            return null;
        }
    }

    /* If a resourceClass constructor parameter isn't specified, then 
     * no resourceMap should be created, and title/description/message 
     * should be null.
     */
    @Test
    public void testNoArgsConstructor()
    {
        appSanityCheck();
        Task task = new NoResourcesTask();
        assertNull("NoResourcesTask.getResourceMap()", task.getResourceMap());
        assertEquals("noPrefix", task.resourceName("noPrefix"));
        assertNull(task.getTitle());
        assertNull(task.getDescription());
        assertNull(task.getMessage());
    }

    static int CANCELLED = 1;
    static int INTERRUPTED = 2;
    static int FAILED = 3;
    static int SUCCEEDED = 4;

    public static class SleepTask extends Task<String, Void>
    {
        private int completionMethod = -1;

        synchronized int getCompletionMethod() { return completionMethod; }

        synchronized void setCompletionMethod(int i) { completionMethod = i; }

        protected void succeeded() { setCompletionMethod(SUCCEEDED); }

        protected void cancelled() { setCompletionMethod(CANCELLED); }

        protected void failed() { setCompletionMethod(FAILED); }

        protected void interrupted(InterruptedException e)
        {
            setCompletionMethod(INTERRUPTED);
        }

        private final long sleepTime;

        SleepTask(long sleepTime)
        {
            super(Application.getInstance(SimpleApplication.class));
            this.sleepTime = sleepTime;
        }

        protected String doInBackground() throws InterruptedException
        {
            Thread.sleep(sleepTime);
            return "OK";
        }
    }

    public static class SleepTaskListener extends TaskListener.Adapter<String, Void>
    {
        private int completionMethod = -1;
        private TaskEvent taskEvent = null;

        synchronized int getCompletionMethod() { return completionMethod; }

        synchronized void setCompletionMethod(int i) { completionMethod = i; }

        synchronized TaskEvent getTaskEvent() { return taskEvent; }

        synchronized void setTaskEvent(TaskEvent e) { taskEvent = e; }

        public void succeeded(TaskEvent<String> e)
        {
            setCompletionMethod(SUCCEEDED);
            setTaskEvent(e);
        }

        public void cancelled(TaskEvent<Void> e)
        {
            setCompletionMethod(CANCELLED);
            setTaskEvent(e);
        }

        public void failed(TaskEvent<Throwable> e)
        {
            setCompletionMethod(FAILED);
            setTaskEvent(e);
        }

        public void interrupted(TaskEvent<InterruptedException> e)
        {
            completionMethod = INTERRUPTED;
            setTaskEvent(e);
        }
    }

    private void sleep(long n)
    {
        try
        {
            Thread.sleep(n);
        }
        catch (InterruptedException ignore)
        {
        }
    }


    /**
     * Verify that the Task.cancelled() method is called.
     */
    @Test
    public void testCancelled()
    {
        // cancel before execution
        SleepTask task = new SleepTask(0L);
        SleepTaskListener stl = new SleepTaskListener();
        task.addTaskListener(stl);
        task.cancel(false);
        assertTrue(task.isDone());
        /*
        TBD: todo These checks can't be done until the Task has
        completed, i.e. until the done method and all of the
        listeners have run on the EDT.
      assertEquals(CANCELLED, task.getCompletionMethod());
      assertTrue(task.isCancelled());
      assertEquals(CANCELLED, stl.getCompletionMethod());
      assertNotNull(stl.getTaskEvent());
      assertEquals(stl.getTaskEvent().getSource(), task);
      */

        // cancel after execution; interrupt Thread.sleep()
        task = new SleepTask(100000L);
        stl = new SleepTaskListener();
        task.addTaskListener(stl);
        task.execute();
        while (!task.isStarted())
        {
            sleep(20L);
        }
        sleep(100L); // give task a chance to really start sleeping
        task.cancel(true);
        /*
        TBD: These checks can't be done until the Task has
        completed, i.e. until the done method and all of the
        listeners have run on the EDT.
      assertEquals("task.getCompletionMethod()", task.getCompletionMethod(), CANCELLED);
      assertTrue(task.isCancelled());
      assertEquals(CANCELLED, stl.getCompletionMethod());
      assertNotNull(stl.getTaskEvent());
      assertEquals(task, stl.getTaskEvent().getSource());
      assertNull(stl.getTaskEvent().getValue());
      */
    }
}


