package fr.viveris.vizada.jnidbus.dispatching.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {
    String path();
    String interfaceName();
}
