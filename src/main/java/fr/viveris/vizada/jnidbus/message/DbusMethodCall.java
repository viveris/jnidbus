package fr.viveris.vizada.jnidbus.message;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DbusMethodCall {
    String destination();
    String path();
    String interfaceName();
    String member();
}
