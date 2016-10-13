/*
 * Copyright (C) 2009 Illya Yalovyy
 * Use is subject to license terms.
 */

package org.jdesktop.application;

import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Locale;

/**
 * @author Illya Yalovyy
 */
public class EnabledPropertyInGermanLocaleTest
{

    public static class ActionMapObject
    {

        protected boolean interestingActionFlag = false;
        public static final String PROP_INTERESTINGACTIONFLAG = "interestingActionFlag";

        public boolean isInterestingActionFlag()
        {
            return interestingActionFlag;
        }

        public void setInterestingActionFlag(boolean interestingActionFlag)
        {
            boolean oldInterestingActionFlag = this.interestingActionFlag;
            this.interestingActionFlag = interestingActionFlag;
            propertyChangeSupport.firePropertyChange(PROP_INTERESTINGACTIONFLAG, oldInterestingActionFlag, interestingActionFlag);
        }

        private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public void addPropertyChangeListener(PropertyChangeListener listener)
        {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener)
        {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        @Action(enabledProperty = "interestingActionFlag")
        public void interestingAction()
        {
            System.out.println("Should do something useful!");
        }
    }

    @Before
    public void methodSetup()
    {
        WaitForStartupApplication.launchAndWait(WaitForStartupApplication.class);
    }

    @Test
    public void testActionInGermanLocale()
    {
        Locale.setDefault(Locale.GERMAN);
        ApplicationContext ac = Application.getInstance().getContext();
        ActionMapObject amo = new ActionMapObject();
        assertNotNull(ac.getActionMap(amo).get("interestingAction"));
    }

    @Test
    public void testActionInTurkishLocale()
    {
        Locale.setDefault(new Locale("tr"));
        ApplicationContext ac = Application.getInstance().getContext();
        ActionMapObject amo = new ActionMapObject();
        assertNotNull(ac.getActionMap(amo).get("interestingAction"));
    }


}
