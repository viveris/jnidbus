package fr.viveris.vizada.jnidbus.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mandatory annotation for any class extending the Signal class. Contains everything needed to send a dbus signal
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DbusSignal {
    String path();
    String interfaceName();
    String member();
}
