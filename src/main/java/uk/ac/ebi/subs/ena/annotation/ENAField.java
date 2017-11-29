package uk.ac.ebi.subs.ena.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * /**
 * Created by neilg on 06/07/2017.
 * Annotation used to indicate that a <code>Submittable</code> used to generate an <code>ENASubmittable</code>
 * should contain an attribute
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface ENAField {
    String TYPE_FIELD_NAME = "Type_Field_Name";
    String NO_ATTRIBUTE_TYPE = "No_Attribute_Type";
    String [] values() default {};
    String name() default TYPE_FIELD_NAME;
    String attributeName() default NO_ATTRIBUTE_TYPE;
    String [] allowedValues() default {};
}
