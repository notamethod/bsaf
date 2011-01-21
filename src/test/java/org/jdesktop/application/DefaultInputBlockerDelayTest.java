/*
 * Copyright (C) 2010 Eric Heumann
 * Use is subject to license terms.
 */

package org.jdesktop.application;

import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that when an Action is called from a button in a menu of a popup menu,
 * and that Action has a WINDOW or APPLICATION blocking scope, the input is successfully
 * blocked to the window containing the menu.
 * 
 * @author Eric M. Heumann
 * @version April 10, 2010
 */
public class DefaultInputBlockerDelayTest {

	/**
	 * Launch Application.
	 */
	@Before
	public void testSetup() {
		DIBDTestApp.launchAndWait(DIBDTestApp.class);
	}
	
	@Test
	public void testNew() throws Exception {
		final DIBDTestApp app = Application.getInstance(DIBDTestApp.class);
		final javax.swing.Action goAction = app.getContext().getActionMap().get("go");
		
		goAction.actionPerformed(new ActionEvent(app.goItem, ActionEvent.ACTION_PERFORMED, ""));
		Thread.sleep(40);
		
		final boolean menuEnabled = app.getMainView().getMenuBar().isEnabled();
		final boolean normalGlass = app.getMainFrame().getGlassPane() == app.normalGlass;
		final boolean glassVisible = app.getMainFrame().getGlassPane().isVisible();
		
		Assert.assertFalse("main frame menu is not disabled", menuEnabled);
		Assert.assertFalse("main frame doesn't have input blocking glass pane", normalGlass);
		Assert.assertTrue("main frame glass pane is not showing", glassVisible);
	}

	/**
	 * The SingleFrameApplication.
	 * 
	 * @author Eric M. Heumann
	 * @version April 10, 2010
	 *
	 */
	private static class DIBDTestApp extends WaitForStartupSFA {
		
		private JPanel normalGlass;
		private JMenu goMenu;
		private JMenuItem goItem;

		/**
		 * Starts a Task that calls its Thread to sleep for a while.
		 * The waiting dialog of the Tasks's input blocker has a very long delay.
		 * 
		 * @param application
		 * @return
		 */
		@SuppressWarnings("unused")
		@Action (block = Task.BlockingScope.APPLICATION)
		public Task<Void, Void> go(final Application application) {
			return new DelayInputBlockDialogTask(application);
		}

		@Override
		protected void startup() {
			normalGlass = new JPanel();
			goItem = new JMenuItem(getContext().getActionMap().get("go"));
			goMenu = new JMenu("Menu");
			goMenu.add(goItem);
			final JMenuBar bar = new JMenuBar();
			bar.add(goMenu);
			getMainView().setComponent(new JLabel("Input Blocking Ttest with Delayed Wait Dialog"));
			getMainView().setMenuBar(bar);
			getMainView().getFrame().setGlassPane(normalGlass);
			show(getMainView());
		}

		@Override
		public void ready() {
			super.ready();
		}

		/**
		 * @author Eric M. Heumann
		 * @version April 10, 2010
		 *
		 */
		class DelayInputBlockDialogTask extends Task<Void, Void> {

			public DelayInputBlockDialogTask(final Application application) {
				super(application);
			}

			@Override
			protected Void doInBackground() throws Exception {
				try {
					Thread.sleep(5000l);
					return null;
				} catch (final InterruptedException exc) {
					return null;
				}
			}

		}
	}

}
