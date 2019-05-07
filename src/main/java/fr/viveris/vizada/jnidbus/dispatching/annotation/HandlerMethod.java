package fr.viveris.vizada.jnidbus.dispatching.annotation;

import fr.viveris.vizada.jnidbus.dispatching.Criteria;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface HandlerMethod {
    String member();
    Criteria.HandlerType type();
}
