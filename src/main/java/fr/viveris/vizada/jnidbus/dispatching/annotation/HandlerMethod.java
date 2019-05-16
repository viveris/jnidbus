package fr.viveris.vizada.jnidbus.dispatching.annotation;

import fr.viveris.vizada.jnidbus.dispatching.HandlerType;

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
    HandlerType type();
}
