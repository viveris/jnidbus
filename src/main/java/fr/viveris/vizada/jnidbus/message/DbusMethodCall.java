package fr.viveris.vizada.jnidbus.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mandatory annotation for any class extending the Call class. Contains everything needed to perform a dbus call
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DbusMethodCall {
    String destination();
    String path();
    String interfaceName();
    String member();
}
