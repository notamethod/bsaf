
/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */ 

package org.jdesktop.application;

/**
 * Support for launching an application from a non-EDT thread and
 * waiting until its startup method has finished running on the EDT.  
 */
public class WaitForStartupApplication extends Application {
    private static Object lock = new Object(); // static: Application is a singleton
    private boolean started = false;
    
    /**
     * Unblock the launchAndWait() method.
     */
    protected void startup() { 
	synchronized(lock) {
	    started = true;	
	    lock.notifyAll();
	}
    }

    boolean isStarted() { return started; }

    /**
     * Launch the specified subclsas of WaitForStartupApplication and block
     * (wait) until it's startup() method has run.
     */
    public static void launchAndWait(Class<? extends WaitForStartupApplication> applicationClass) {
	synchronized(lock) {
	    Application.launch(applicationClass, new String[]{});
	    while(true) {
		try {
		    lock.wait();
		}
		catch (InterruptedException e) {
		    System.err.println("launchAndWait interrupted!");
		    break;
		}
		Application app = Application.getInstance(WaitForStartupApplication.class);
		if (app instanceof WaitForStartupApplication) {
		    if (((WaitForStartupApplication)app).isStarted()) {
			break;
		    }
		}
	    }
	}
    }
}
