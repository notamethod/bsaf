/*
 * Copyright (C) 2011 Illya Yalovyy
 * Use is subject to license terms.
 */
package org.jdesktop.application;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Illya Yalovyy
 */
public class TaskServiceTest {

    public static final String CUSTOM_TASK_SERVICE_NAME = "TaskServiceWithHistory";
    public static final String ACTION1 = "actionWithTask1";
    public static final String ACTION2 = "actionWithTask2";
    private ApplicationWithActions application;
    private ApplicationContext context;
    private ApplicationActionMap actionMap;

    public static final class ApplicationWithActions extends WaitForStartupApplication {

        @Override
        protected void initialize(String[] args) {
            super.initialize(args);
            this.getContext().addTaskService(new TaskService(CUSTOM_TASK_SERVICE_NAME));
        }

        @Action(name = ACTION1)
        public Task<Void, Void> actionWithTask1() {
            return new WaitingTask(this);
        }

        @Action(name = ACTION2, taskService = CUSTOM_TASK_SERVICE_NAME)
        public Task<Void, Void> actionWithTask2() {
            return new WaitingTask(this);
        }
    }

    public static final class WaitingTask extends Task<Void, Void> {

        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(1);

        public WaitingTask(Application application) {
            super(application);
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            // DO nothing
            // just wait for the signal
            startSignal.await();
            return null;
        }

        @Override
        protected void finished() {
            doneSignal.countDown();
        }
    }

    @BeforeClass
    public static void unitSetup() {
        ApplicationWithActions.launchAndWait(ApplicationWithActions.class);

    }

    @Before
    public void initVariables() {

        application = Application.getInstance(ApplicationWithActions.class);
        context = application.getContext();
        actionMap = context.getActionMap();

    }

    @Test
    public void testTaskServicesList() {
        List<TaskService> taskServices = context.getTaskServices();

        assertEquals(2, taskServices.size());

        assertNotNull(context.getTaskService());
        assertNotNull(context.getTaskService(CUSTOM_TASK_SERVICE_NAME));
        assertNotNull(context.getTaskService(TaskService.DEFAULT_NAME));

        assertEquals(TaskService.DEFAULT_NAME, context.getTaskService().getName());
    }

    @Test
    public void testDefaultTaskService() throws InterruptedException {

        javax.swing.Action action1 = actionMap.get(ACTION1);
        action1.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        TaskService taskService = context.getTaskService(TaskService.DEFAULT_NAME);
        assertEquals(1, taskService.getTasks().size());
        final WaitingTask task = (WaitingTask) taskService.getTasks().get(0);

        task.startSignal.countDown();
        boolean await = task.doneSignal.await(5, TimeUnit.SECONDS);
        assertTrue(await);
    }

    @Test
    public void testCustomTaskService() throws InterruptedException {

        javax.swing.Action action2 = actionMap.get(ACTION2);
        action2.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        TaskService taskService = context.getTaskService(CUSTOM_TASK_SERVICE_NAME);
        assertEquals(1, taskService.getTasks().size());
        final WaitingTask task = (WaitingTask) taskService.getTasks().get(0);

        task.startSignal.countDown();
        boolean await = task.doneSignal.await(5, TimeUnit.SECONDS);
        assertTrue(await);
    }
}
