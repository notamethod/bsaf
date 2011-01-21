
/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */
package org.jdesktop.application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the field as a resource to be injected.
 * <p>
 * In order to inject the resources for this class,
 * the resources must be defined in a class resource file.
 * You should name field resources in the resource file using the class name followed by a period (.) and the key:
 * <pre>
 *  &lt;classname&gt;.&lt;fieldname&gt;
 * </pre>
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Resource {
    /**
     * Key for resource injection. If not specified the name of the field will be used.
     */
    String key() default "";
}
