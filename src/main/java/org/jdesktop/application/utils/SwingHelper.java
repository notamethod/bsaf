/*
 * Copyright (C) 2009 Illya Yalovyy
 * Use is subject to license terms.
 */

package org.jdesktop.application.utils;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.RootPaneContainer;

/**
 * Utility class for Swing Application Framework (BSAF)
 *
 * @author Illya Yalovyy
 * @author Eric Heumann
 *
 * @since 1.9
 */
public final class SwingHelper {
	private static final String WINDOW_STATE_NORMAL_BOUNDS = "WindowState.normalBounds";

    private SwingHelper() {
    }

    /**
     * Calculates virtual graphic bounds.
     * On multiscreen systems all screens are united into one virtual screen.
     * @return the graphic bounds
     */
    public static Rectangle computeVirtualGraphicsBounds() {
        Rectangle virtualBounds = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (GraphicsDevice gd : gs) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            virtualBounds = virtualBounds.union(gc.getBounds());
        }
        return virtualBounds;
    }

    /**
     * Checks whether the window supports resizing
     * @param window the {@code Window} to be checked
     * @return true if the window supports resizing
     */
    public static boolean isResizable(Window window) {
        boolean resizable = true;
        if (window instanceof Frame) {
            resizable = ((Frame) window).isResizable();
        } else if (window instanceof Dialog) {
            resizable = ((Dialog) window).isResizable();
        }
        return resizable;
    }

    /**
     * Calculates default location for the specified window.
     * @return default location for the window
     * @param window the window location is calculated for.
     *               It should not be null.
     */
    public static Point defaultLocation(Window window) {
        GraphicsConfiguration gc = window.getGraphicsConfiguration();
        Rectangle bounds = gc.getBounds();
        Insets insets = window.getToolkit().getScreenInsets(gc);
        int x = bounds.x + insets.left;
        int y = bounds.y + insets.top;
        return new Point(x, y);
    }

    /**
     * Finds the nearest RootPaneContainer of the provided Component. 
     * Primarily, if a JPopupMenu (such as used by JMenus when they are visible) has no parent,
     * the search continues with the JPopupMenu's invoker instead. Fixes BSAF-77
     * 
     * @return a RootPaneContainer for the provided component
     * @param root the Component
     */
    public static RootPaneContainer findRootPaneContainer(Component root) {
        while (root != null) {
            if (root instanceof RootPaneContainer) {
                return (RootPaneContainer) root;
            } else if (root instanceof JPopupMenu && root.getParent() == null) {
                root = ((JPopupMenu) root).getInvoker();
            } else {
                root = root.getParent();
            }
        }
        return null;
    }
    
    /**
     * Gets {@code Window} bounds from the client property
     * @param window the source {@code Window}
     * @return bounds from the client property
     */
    public static Rectangle getWindowNormalBounds(Window window) {
        if (window instanceof JFrame) {
            Object res = ((JFrame) window).getRootPane().getClientProperty(WINDOW_STATE_NORMAL_BOUNDS);
            if (res instanceof Rectangle) {
                return (Rectangle) res;
            }
        }
        return null;
    }
    
    /**
     * Puts {@code Window} bounds to client property.
     * @param window the target {@code Window}
     * @param bounds bounds
     */
    public static void putWindowNormalBounds(Window window, Rectangle bounds) {
        if (window instanceof JFrame) {
            ((JFrame) window).getRootPane().putClientProperty(WINDOW_STATE_NORMAL_BOUNDS, bounds);
        }
    }
}
