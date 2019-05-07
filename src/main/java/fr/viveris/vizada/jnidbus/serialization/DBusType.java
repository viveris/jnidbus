package fr.viveris.vizada.jnidbus.serialization;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DBusType {
    String value();
    String[] fields();
}
