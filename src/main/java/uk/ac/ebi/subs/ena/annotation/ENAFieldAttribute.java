package uk.ac.ebi.subs.ena.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ENAFieldAttribute {

    static String NO_FIELD = "No_Field";

    /**
     * The attribute name to retrieve
     * @return
     */
    String name();
    boolean required() default false;

    /**
     * If this is set it will use another attributes value as the field name.
     * The attribute must have been previously validated
     * @return
     */
    String attributeFieldName() default NO_FIELD;

    String [] allowedValues() default {};

}
