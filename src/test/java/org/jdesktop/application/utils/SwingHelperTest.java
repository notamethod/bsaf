package org.jdesktop.application.utils;

import static org.junit.Assert.*;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.RootPaneContainer;

import org.junit.Test;

public class SwingHelperTest {

    @Test
    public void testFindRootPaneContainerNull() {
        JComponent cmp = new JComponent() {
        };
        RootPaneContainer rpc = SwingHelper.findRootPaneContainer(cmp);
        assertNull(rpc);
        
        rpc = SwingHelper.findRootPaneContainer(null);
        assertNull(rpc);
    }

    @Test
    public void testFindRootPaneContainerParent1() {
        JFrame f = new JFrame();
        JButton b = new JButton();
        f.getContentPane().add(b);
        RootPaneContainer rpc = SwingHelper.findRootPaneContainer(b);
        assertEquals(f, rpc);
    }

    @Test
    public void testFindRootPaneContainerParent2() {
        JFrame f = new JFrame();
        JMenuBar mb = new JMenuBar();
        JMenu m = new JMenu();
        JMenuItem i = new JMenuItem();
        
        m.add(i);
        mb.add(m);
        f.setJMenuBar(mb);
        
        RootPaneContainer rpc = SwingHelper.findRootPaneContainer(i);
        assertEquals(f, rpc);
    }
    
    @Test
    public void testFindRootPaneContainerInvoker1() {
        JFrame f = new JFrame();
        JMenuItem i = new JMenuItem();
        JLabel l = new JLabel();
        JPopupMenu pm = new JPopupMenu();
        
        pm.add(i);
        l.setComponentPopupMenu(pm);
        
        f.getContentPane().add(l);pm.setInvoker(l);
        
        RootPaneContainer rpc = SwingHelper.findRootPaneContainer(i);
        assertEquals(f, rpc);
    }   
}
