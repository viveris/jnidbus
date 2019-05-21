package fr.viveris.jnidbus.remote;

import fr.viveris.jnidbus.dispatching.MemberType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to define the name and type of a remote object method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface RemoteMember {
    String value();
}
