/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.dispatching.annotation;

import fr.viveris.jnidbus.dispatching.MemberType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation exposing a @Handler class to DBus. The signature of the method will be inferred from its arguments and return type, which should only be Message classes or void
 * for the return type of a signal handler. A method can handle either calls or signals, depending upon the value of the field "type"
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HandlerMethod {
    String member();
    MemberType type();
}
