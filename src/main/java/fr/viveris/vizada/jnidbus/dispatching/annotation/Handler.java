package fr.viveris.vizada.jnidbus.dispatching.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation giving basic information about the handler object (path and interface). Multiple handlers can be registered
 * to the same path/interface as long as they implement different methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Handler {
    String path();
    String interfaceName();
}
