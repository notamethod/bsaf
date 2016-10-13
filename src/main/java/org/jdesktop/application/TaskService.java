/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved.
 * Copyright (C) 2010 Illya Yalovyy. All rights reserved.
 * Use is subject to license terms.
 */
package org.jdesktop.application;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * The service for executing tasks
 * <p>
 * Methods descriptions are copied from {@link ExecutorService}
 * <p>
 */
public class TaskService extends AbstractBean {
    public static final String DEFAULT_NAME = "default";

    private final String name;
    private final ExecutorService executorService;
    private final List<Task> tasks;
    private final PropertyChangeListener taskPCL;

    /**
     * Creates a new {@code TaskService}
     * @param name the name of the task service
     * @param executorService the executor to be used to run tasks.
     */
    public TaskService(String name, ExecutorService executorService) {
        if (name == null) {
            throw new IllegalArgumentException("null name");
        }
        if (executorService == null) {
            throw new IllegalArgumentException("null executorService");
        }
        this.name = name;
        this.executorService = executorService;
        this.tasks = new ArrayList<Task>();
        this.taskPCL = new TaskPCL();
    }

    /**
     * Creates a new {@code TaskService} with default executor.
     * The default executor is a ThreadPoolExecutor with core pool size = 3,
     * maximum pool size = 10, threads live time = 1 second and queue of type
     * {@link LinkedBlockingQueue}.
     */
    public TaskService(String name) {
        this(name, new ThreadPoolExecutor(
                3, // corePool size
                10, // maximumPool size
                1L, TimeUnit.SECONDS, // non-core threads time to live
                new LinkedBlockingQueue<Runnable>()));
    }

    /**
     * Gets the name of this task service
     * @return this task service's name
     */
    public final String getName() {
        return name;
    }

    private List<Task> copyTasksList() {
        synchronized (tasks) {
            if (tasks.isEmpty()) {
                return Collections.emptyList();
            } else {
                return new ArrayList<Task>(tasks);
            }
        }
    }

    private class TaskPCL implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if ("done".equals(propertyName)) {
                Task task = (Task) (e.getSource());
                if (task.isDone()) {
                    List<Task> oldTaskList, newTaskList;
                    synchronized (tasks) {
                        oldTaskList = copyTasksList();
                        tasks.remove(task);
                        task.removePropertyChangeListener(taskPCL);
                        newTaskList = copyTasksList();
                    }
                    firePropertyChange("tasks", oldTaskList, newTaskList);
                    Task.InputBlocker inputBlocker = task.getInputBlocker();
                    if (inputBlocker != null) {
                        inputBlocker.unblock();
                    }
                }
            }
        }
    }

    private void maybeBlockTask(Task task) {
        final Task.InputBlocker inputBlocker = task.getInputBlocker();
        if (inputBlocker == null) {
            return;
        }
        if (inputBlocker.getScope() != Task.BlockingScope.NONE) {
            if (SwingUtilities.isEventDispatchThread()) {
                inputBlocker.block();
            } else {
                Runnable doBlockTask = new Runnable() {

                    @Override
                    public void run() {
                        inputBlocker.block();
                    }
                };
                SwingUtilities.invokeLater(doBlockTask);
            }
        }
    }

    /**
     * Executes the task.
     *
     * @param task the task to be executed
     */
    public void execute(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("null task");
        }
        if (!task.isPending() || (task.getTaskService() != null)) {
            throw new IllegalArgumentException("task has already been executed");
        }
        task.setTaskService(this);
        // TBD: what if task has already been submitted?
        List<Task> oldTaskList, newTaskList;
        synchronized (tasks) {
            oldTaskList = copyTasksList();
            tasks.add(task);
            newTaskList = copyTasksList();
            task.addPropertyChangeListener(taskPCL);
        }
        firePropertyChange("tasks", oldTaskList, newTaskList);
        maybeBlockTask(task);
        executorService.execute(task);
    }

    /**
     * Returns the list of tasks which are executing by this service
     * @return the list of tasks which are executing by this service
     */
    public List<Task> getTasks() {
        return copyTasksList();
    }

    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted.
     * Invocation has no additional effect if already shut down.
     *
     * @throws SecurityException if a security manager exists and
     *         shutting down this ExecutorService may manipulate
     *         threads that the caller is not permitted to modify
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     *         or the security manager's <tt>checkAccess</tt> method
     *         denies access.
     */
    public final void shutdown() {
        executorService.shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the
     * processing of waiting tasks, and returns a list of the tasks that were
     * awaiting execution.
     *
     * <p>There are no guarantees beyond best-effort attempts to stop
     * processing actively executing tasks.  For example, typical
     * implementations will cancel via {@link Thread#interrupt}, so any
     * task that fails to respond to interrupts may never terminate.
     *
     * @return list of tasks that never commenced execution
     * @throws SecurityException if a security manager exists and
     *         shutting down this ExecutorService may manipulate
     *         threads that the caller is not permitted to modify
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     *         or the security manager's <tt>checkAccess</tt> method
     *         denies access.
     */
    public final List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    /**
     * Returns <tt>true</tt> if this executor has been shut down.
     *
     * @return <tt>true</tt> if this executor has been shut down
     */
    public final boolean isShutdown() {
        return executorService.isShutdown();
    }

    /**
     * Returns <tt>true</tt> if all tasks have completed following shut down.
     * Note that <tt>isTerminated</tt> is never <tt>true</tt> unless
     * either <tt>shutdown</tt> or <tt>shutdownNow</tt> was called first.

     * @return <tt>true</tt> if all tasks have completed following shut down
     */
    public final boolean isTerminated() {
        return executorService.isTerminated();
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown
     * request, or the timeout occurs, or the current thread is
     * interrupted, whichever happens first.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return <tt>true</tt> if this executor terminated and
     *         <tt>false</tt> if the timeout elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    public final boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }
}
