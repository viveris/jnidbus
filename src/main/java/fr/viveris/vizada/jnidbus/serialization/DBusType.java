package fr.viveris.vizada.jnidbus.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation mandatory for Messages classes. Define how the object will be serialized by telling to which fields of the
 * object the signature elements are bound. Each field put in this annotation should have a public getter and setter.
 *
 * ex: if your object contains a string in the field "myString", the annotation should be:
 *          DBusType(signature="s",fields={"myString"})
 *
 *     if your object contains a list of integer in the field "myList", the annotation should be:
 *          DBusType(signature="ai",fields={"myList"}).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DBusType {
    String signature();
    String[] fields();
}
