
/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */
package org.jdesktop.application;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a list of the proxy action names
 * <p>
 * The proxy actions perform their
 * action not on the component they're bound to (menu items and
 * toolbar buttons), but on the component that currently
 * has the keyboard focus.  Their enabled state tracks the
 * selection value of the component with the keyboard focus,
 * as well as the contents of the system clipboard.
 * The proxy actions work in conjunction with the action
 * map associated with the component that has focus.
 * <p>
 * So you can create an action in any object (whether it is a component or not),
 * and use that action anywhere. But as the focus changes, that one action binds
 * itself to the same named action in the component tree that the focused control has.
 * <p>
 * For example, in the case of Copy, the proxy action can be created anywhere
 * in the application and attached to menus, buttons, or whatever you want.
 * When the focus is on a control that has an ActionMap that contains a 'copy' entry,
 * the proxy action will bind to that action and the menu/button/whatever will invoke
 * the action of the component with focus.
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProxyActions {

    String[] value() default {};
}
