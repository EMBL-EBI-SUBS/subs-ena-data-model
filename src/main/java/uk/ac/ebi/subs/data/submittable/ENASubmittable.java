package uk.ac.ebi.subs.data.submittable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.ena.annotation.ENAAttribute;
import uk.ac.ebi.subs.ena.annotation.ENAValidation;
import uk.ac.ebi.subs.ena.exception.AttributeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ENASubmittable extends Submittable {

    static final String MULTIPLE_VALUES_ERROR_MESSAGE = "Multiple values found for attribute %s.";
    static final String ATTRIBUTE_VALUE_REQUIRED_ERROR_MESSAGE = "Value for attribute %s is required.";
    static final String INVALID_VALUE_ERROR_MESSAGE = "Invalid value for attribute %s value must be one of %s.";

    default Optional<Attribute> getExistingStudyTypeAttribute(String attributeName, boolean allowMultiple) {
        if (!allowMultiple && getAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName)).count() > 1)
            throw new IllegalArgumentException(String.format(MULTIPLE_VALUES_ERROR_MESSAGE,attributeName));
        return getAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName)).findFirst();
    }

    default Stream<Attribute> getExistingStudyTypeAttributes(String attributeName) {
        return getAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName));
    }

    default int getAttributeCount(String attributeName) {
        return (int)getAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName)).count();
    }

    default void deleteAttribute(Attribute attribute) {
        getAttributes().remove(attribute);
    }

    /*
    default String getExistingStudyTypeValue(String attributeName) {
        final Optional<Attribute> existingStudyTypeAttribute = getExistingStudyTypeAttribute(attributeName);
        if (existingStudyTypeAttribute.isPresent()) {
            return existingStudyTypeAttribute.get().getValue();
        } else {
            return null;
        }
    }
    */

    default void serialiseAttributes () throws IllegalAccessException {
        if (this.getClass().isAnnotationPresent(ENAValidation.class))
            serialiseFields(this.getClass(), this);
    }

    default void serialiseFields (Class<?> aClass, Object obj) throws IllegalAccessException {
        final Field[] fields = aClass.getDeclaredFields();

        for (Field field : fields ) {
            if (field.isAnnotationPresent(ENAAttribute.class)) {
                final ENAAttribute annotation = field.getAnnotation(ENAAttribute.class);
                int attributeCount = getAttributeCount(annotation.name());
                if (attributeCount == 1) {
                    final Optional<Attribute> existingStudyTypeAttribute = getExistingStudyTypeAttribute(annotation.name(),false);
                    if (annotation.allowedValues().length > 0) {
                        if ( !ArrayUtils.contains( annotation.allowedValues(), existingStudyTypeAttribute.get().getValue() ) ) {
                            throw new IllegalArgumentException(String.format(INVALID_VALUE_ERROR_MESSAGE,annotation.name(), StringUtils.join(annotation.allowedValues())));
                        }
                    }
                    field.set(obj,existingStudyTypeAttribute.get().getValue());
                    deleteAttribute(existingStudyTypeAttribute.get());
                } else if (attributeCount > 1) {
                    throw new AttributeException("Multiple values found for attribute " + annotation.name());
                } else if (attributeCount == 0 && annotation.required()) {
                    throw new AttributeException("Value for attribute " + annotation.name() + " is required");
                }
            } else if (field.getType().isMemberClass()) {
                serialiseFields(field.getType(),field.get(obj));
            }
        }
    }

    default void deSerialiseAttributes () throws IllegalAccessException {
        deSerialiseFields(this.getClass(), this);
    }

    default void deSerialiseFields (Class<?> aClass, Object obj) throws IllegalAccessException {
        final Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields ) {
            if (field.isAnnotationPresent(ENAAttribute.class)) {
                final ENAAttribute annotation = field.getAnnotation(ENAAttribute.class);
                final Object o = field.get(obj);
                Attribute attribute = new Attribute();
                attribute.setName(annotation.name());
                attribute.setValue(o.toString());
                getAttributes().add(attribute);
            } else if (field.getType().isMemberClass()) {
                deSerialiseFields(field.getType(),field.get(obj));
            }

        }
    }

    /**
     * Bug in moxy which prevents using a default version here.
     */
    String getTeamName ();

    /**
     * Bug in moxy which prevents using a default version here
     */
    void setTeamName (String teamName);

}
