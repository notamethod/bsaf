
/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */
package org.jdesktop.application;

/**
 * Identical to WaitForStartupApplication.
 * 
 * Support for launching a SingleFrameApplication from a non-EDT thread and
 * waiting until its startup method has finished running on the EDT.  
 */
public class WaitForStartupSFA extends SingleFrameApplication {

    private static final Object lock = new Object(); // static: Application is a singleton

    @Override
    protected void startup() {
        // do nothing
    }

    /**
     * Unblock the launchAndWait() method
     * when the application is ready
     */
    @Override
    protected void ready() {
        super.ready();
        synchronized (lock) {
            lock.notifyAll();
        }
    }


    /**
     * Launch the specified subclsas of WaitForStartupApplication and block
     * (wait) until it's startup() method has run.
     */
    public static void launchAndWait(Class<? extends WaitForStartupSFA> applicationClass) {
        synchronized (lock) {
            Application.launch(applicationClass, new String[]{});
            while (true) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    System.err.println("launchAndWait interrupted!");
                    break;
                }
                Application app = Application.getInstance(WaitForStartupSFA.class);
                if (app instanceof WaitForStartupSFA) {
                    if (((WaitForStartupSFA) app).isReady()) {
                        break;
                    }
                }
            }
        }
    }
}
