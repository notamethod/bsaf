/*
 * Copyright (C) 2009 Illya Yalovyy
 * Use is subject to license terms.
 */
package org.jdesktop.application;

import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Illya Yalovyy
 */
public class TaskMonitorTest{

    public static final String MESSAGE_TASK0 = "* Task 0";
    public static final String MESSAGE_TASK1 = "* Task 1";    
    
    public static class SimpleApplication extends WaitForStartupApplication {

        public boolean startupOnEDT;
    }

    public static class LatchTask extends Task<Void, Void> {
        final CountDownLatch allTasksDoneLatch;
        final CountDownLatch startLatch;
        final LatchTask other;
        private final String message;
        volatile boolean fired = false;
        
        public LatchTask(String message, CountDownLatch startLatch, CountDownLatch allTasksDoneLatch, LatchTask other) {
            super(Application.getInstance(SimpleApplication.class));
            this.message = message;
            this.startLatch = startLatch;
            this.allTasksDoneLatch = allTasksDoneLatch;
            this.other = other;
        }

        @Override
        protected Void doInBackground() throws Exception {
            
            if (startLatch!=null) startLatch.await(500, TimeUnit.MILLISECONDS);
            
            setMessage(message);
            
            fired = true;
            
            if (other != null) {
                Application.getInstance().getContext().getTaskService().execute(other);
            }
            
            return null;
        }        
        
        @Override
        protected void finished() {
            if (other != null) {
                other.startLatch.countDown();
            }
            allTasksDoneLatch.countDown();
        }        
    }

    @Before
    public void methodSetup()
    {
        SimpleApplication.launchAndWait(SimpleApplication.class);
    }

    CountDownLatch allTasksDoneLatch = new CountDownLatch(2);
    CountDownLatch startLatch = new CountDownLatch(1);

    @Test
    public void testSucceeded() throws InterruptedException {
        
        TaskMonitor mon = Application.getInstance().getContext().getTaskMonitor();
        RecordingPropertyChangeListener pcl = new RecordingPropertyChangeListener();
        mon.addPropertyChangeListener(pcl);
        LatchTask t0 = new LatchTask(MESSAGE_TASK0, new CountDownLatch(1), allTasksDoneLatch, null);
        LatchTask t1 = new LatchTask(MESSAGE_TASK1, startLatch, allTasksDoneLatch, t0);
        Application.getInstance().getContext().getTaskService().execute(t1);

        allTasksDoneLatch.await(1000, TimeUnit.MILLISECONDS);
        
        assertTrue(t0.fired);
        assertTrue(t1.fired);
        assertTrue(pcl.messages.contains(MESSAGE_TASK0));
        assertTrue(pcl.messages.contains(MESSAGE_TASK1));
    }

    @Test
    public void testMessageSucceeded()  throws InterruptedException {
        TaskMonitor mon = Application.getInstance().getContext().getTaskMonitor();

        TaskService srv = Application.getInstance().getContext().getTaskService();

        final CountDownLatch cdl = new CountDownLatch(1);

        final String MESSAGE_0 = "doInBackground";
        final String MESSAGE_1 = "succeeded";
        final String MESSAGE_2 = "finished";

        final Set<String> messages = Collections.synchronizedSet(new HashSet<String>(10));

        mon.addPropertyChangeListener(Task.PROP_MESSAGE, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String message = (String) evt.getNewValue();
                messages.add(message);
            }
        });

        srv.execute(new Task(Application.getInstance()) {

            @Override
            protected Object doInBackground() throws Exception {
                setMessage("doInBackground");
                return null;
            }

            @Override
            protected void succeeded(Object result) {
                super.succeeded(result);
                setMessage("succeeded");
            }

            @Override
            protected void finished() {
                super.finished();
                setMessage("finished");
                cdl.countDown();
            }

        });

        cdl.await();

        assertTrue(MESSAGE_0, messages.contains(MESSAGE_0));
        assertTrue(MESSAGE_1, messages.contains(MESSAGE_1));
        assertTrue(MESSAGE_2, messages.contains(MESSAGE_2));

    }

    @Test
    public void testMessageFailed()  throws InterruptedException {
        TaskMonitor mon = Application.getInstance().getContext().getTaskMonitor();

        TaskService srv = Application.getInstance().getContext().getTaskService();

        final CountDownLatch cdl = new CountDownLatch(1);

        final String MESSAGE_0 = "doInBackground";
        final String MESSAGE_1 = "failed";
        final String MESSAGE_2 = "finished";

        final Set<String> messages = Collections.synchronizedSet(new HashSet<String>(10));

        mon.addPropertyChangeListener(Task.PROP_MESSAGE, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String message = (String) evt.getNewValue();
                messages.add(message);
            }
        });

        srv.execute(new Task(Application.getInstance()) {

            @Override
            protected Object doInBackground() throws Exception {
                setMessage("doInBackground");
                Thread.sleep(100);
                throw new Exception("Test Exception");
            }

            @Override
            protected void failed(Throwable cause) {
                setMessage("failed");
            }

            @Override
            protected void finished() {
                setMessage("finished");
                cdl.countDown();
            }

        });

        cdl.await();
        assertTrue(MESSAGE_0, messages.contains(MESSAGE_0));
        assertTrue(MESSAGE_1, messages.contains(MESSAGE_1));
        assertTrue(MESSAGE_2, messages.contains(MESSAGE_2));

    }

    private class RecordingPropertyChangeListener implements PropertyChangeListener{
        List<String> messages = new ArrayList<String>();
        
        public void propertyChange(PropertyChangeEvent evt) {
            
            if (TaskMonitor.PROP_FOREGROUND_TASK.equals(evt.getPropertyName())) {
                // Wait until TaskMonitor insert listeners. may be this process should be synchronized?
                startLatch.countDown();
            }
            
            if (Task.PROP_MESSAGE.equals(evt.getPropertyName())){
                messages.add((String) evt.getNewValue());
            }
        }
    }
}
