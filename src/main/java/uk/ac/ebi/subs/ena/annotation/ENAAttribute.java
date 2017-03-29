package uk.ac.ebi.subs.ena.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is used to tag a member of a class that extends ENASubmittable .
 * Used to copy values from the submittables attributes to the member value
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ENAAttribute {
    /**
     * The name of the attribute who's value will be copied over
     * @return
     */
    String name();
    boolean required() default false;
    String [] allowedValues() default {};
}
