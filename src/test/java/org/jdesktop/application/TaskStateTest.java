/*
 * Copyright (C) 2009 Illya Yalovyy
 * Use is subject to license terms.
 */
package org.jdesktop.application;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Illya Yalovyy
 */
public class TaskStateTest{

    private enum State {
        PCL_STARTED,
        PCL_COMPLETED,
        PCL_DONE,
        SUCCEEDED,
        FAILED,
        FINISHED,
        CANCELLED,
        TL_DOINBACKGROUND,
        TL_SUCCEEDED,
        TL_FAILED,
        TL_CANCALLED,
        TL_FINISHED,
        TL_INTERRUPTED
    }

    public static class SimpleApplication extends WaitForStartupApplication {

        public boolean startupOnEDT;
    }

    public static class DoNothingTask extends Task<Void, Void> {

        private final Exception ex;
        private final Queue<State> queue;


        DoNothingTask(Exception ex, Queue<State> queue) {
            super(Application.getInstance(SimpleApplication.class), "DoNothingTask");
            this.ex = ex;
            this.queue = queue;
        }

        @Override
        protected Void doInBackground() throws Exception {
            
            if (ex!=null)
                throw ex;
            return null;
        }

        @Override
        protected void failed(Throwable cause) {
            queue.offer(State.FAILED);
        }

        @Override
        protected void succeeded(Void result) {
            queue.offer(State.SUCCEEDED);
        }

        @Override
        protected void finished() {
            queue.offer(State.FINISHED);
        }
    }

    private static class PropertyChangeListenerImpl implements PropertyChangeListener {
        private final BlockingQueue<State> queue;

        public PropertyChangeListenerImpl(BlockingQueue<State> queue) {
            this.queue = queue;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Task.PROP_DONE.equals(evt.getPropertyName())) {
                queue.offer(State.PCL_DONE);
            } else if (Task.PROP_STARTED.equals(evt.getPropertyName())) {
                queue.offer(State.PCL_STARTED);
            } else if (Task.PROP_COMPLETED.equals(evt.getPropertyName())) {
                queue.offer(State.PCL_COMPLETED);
            }
        }
    }

    private static class TaslListenerImpl
            extends TaskListener.Adapter<Void, Void> {

        private final BlockingQueue<State> queue;

        public TaslListenerImpl(BlockingQueue<State> queue) {
            this.queue = queue;
        }

        @Override
        public void cancelled(TaskEvent<Void> event) {
            queue.offer(State.TL_CANCALLED);
        }

        @Override
        public void doInBackground(TaskEvent<Void> event) {
            queue.offer(State.TL_DOINBACKGROUND);
        }

        @Override
        public void failed(TaskEvent<Throwable> event) {
            queue.offer(State.TL_FAILED);
        }

        @Override
        public void finished(TaskEvent<Void> event) {
            queue.offer(State.TL_FINISHED);
        }

        @Override
        public void interrupted(TaskEvent<InterruptedException> event) {
            queue.offer(State.TL_INTERRUPTED);
        }

        @Override
        public void succeeded(TaskEvent<Void> event) {
            queue.offer(State.TL_SUCCEEDED);
        }

    }

    @Before
    public void methodSetup()
    {
        SimpleApplication.launchAndWait(SimpleApplication.class);
    }

    @Test
    public void testSucceeded() throws InterruptedException {
        List<State> result = runTask(null);
        assertSequence(result, State.SUCCEEDED);
    }

    @Test
    public void testFailed() throws InterruptedException {
        List<State> result = runTask(new Exception("Test Exception"));
        assertSequence(result, State.FAILED);
    }

    private void assertSequence(List<State> result, State expected) {
        assertTrue(isAfter(State.PCL_DONE, State.PCL_STARTED, result));
        assertTrue(isAfter(State.PCL_DONE, State.TL_DOINBACKGROUND, result));

        assertTrue(isAfter(State.PCL_COMPLETED, State.PCL_DONE, result));
        assertTrue(isAfter(State.PCL_COMPLETED, State.FINISHED, result));
        assertTrue(isAfter(State.PCL_COMPLETED, expected, result));
        assertTrue(isAfter(State.PCL_COMPLETED, State.TL_FINISHED, result));

        assertTrue(isAfter(State.FINISHED, expected, result));
        assertTrue(isAfter(expected, State.PCL_DONE, result));

    }

    private List<State> runTask(Exception ex) throws InterruptedException {
        List<State> result = new ArrayList<State>();
        BlockingQueue<State> queue = new ArrayBlockingQueue<State>(20);
        DoNothingTask task = new DoNothingTask(ex, queue);
        task.addPropertyChangeListener(new PropertyChangeListenerImpl(queue));
        task.addTaskListener(new TaslListenerImpl(queue));
        task.execute();
        boolean timeout=false;
        while (!timeout) {
            State s = queue.poll(1, TimeUnit.SECONDS);
            if (!(timeout = s == null)) {
                result.add(s);
            }
        }
        return result;
    }

    private boolean isAfter(State st1, State st2, List<State> list) {
        int idx1 = list.indexOf(st1);
        int idx2 = list.indexOf(st2);
        return idx1 > idx2;
    }
}
