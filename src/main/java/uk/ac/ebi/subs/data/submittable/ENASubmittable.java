package uk.ac.ebi.subs.data.submittable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.subs.data.component.AbstractSubsRef;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.ena.annotation.ENAAttribute;
import uk.ac.ebi.subs.ena.annotation.ENAValidation;
import uk.ac.ebi.subs.ena.exception.AttributeException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by neilg on 04/04/2017.
 */
public abstract class ENASubmittable <T extends BaseSubmittable> implements Submittable, BaseSubmittableFactory<T> {
    static final String MULTIPLE_VALUES_ERROR_MESSAGE = "Multiple values found for attribute %s.";
    static final String ATTRIBUTE_VALUE_REQUIRED_ERROR_MESSAGE = "Value for attribute %s is required.";
    static final String INVALID_VALUE_ERROR_MESSAGE = "Invalid value for attribute %s value must be one of %s.";

    BaseSubmittable<T> baseSubmittable;

    public ENASubmittable(BaseSubmittable<T> baseSubmittable) throws IllegalAccessException {
        this.baseSubmittable = baseSubmittable;
        serialiseAttributes();
    }


    Optional<Attribute> getExistingStudyTypeAttribute(String attributeName, boolean allowMultiple) {
        if (!allowMultiple && getAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName)).count() > 1)
            throw new IllegalArgumentException(String.format(MULTIPLE_VALUES_ERROR_MESSAGE,attributeName));
        return getAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName)).findFirst();
    }

    Stream<Attribute> getExistingStudyTypeAttributes(String attributeName) {
        return getAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName));
    }

    int getAttributeCount(String attributeName) {
        final List<Attribute> attributeList = getAttributes();
        if (attributeList == null) {
            return 0;
        } else {
            return (int)attributeList.stream().filter(attribute -> attribute.getName().equalsIgnoreCase(attributeName)).count();
        }
    }

    void deleteAttribute(Attribute attribute) {
        getAttributes().remove(attribute);
    }

    void serialiseAttributes () throws IllegalAccessException {
        if (this.getClass().isAnnotationPresent(ENAValidation.class))
            serialiseFields(this.getClass(), this);
    }

    void serialiseFields (Class<?> aClass, Object obj) throws IllegalAccessException {
        final Field[] fields = aClass.getDeclaredFields();
        String string;
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

    void deSerialiseAttributes () throws IllegalAccessException {
        deSerialiseFields(this.getClass(), this);
    }

    void deSerialiseFields (Class<?> aClass, Object obj) throws IllegalAccessException {
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

    @Override
    public String getId() {
        return baseSubmittable.getId();
    }

    @Override
    public void setId(String id) {
        baseSubmittable.setId(id);
    }

    @Override
    public String getAccession() {
        return baseSubmittable.getAccession();
    }

    @Override
    public void setAccession(String accession) {
        baseSubmittable.setAccession(accession);
    }

    @Override
    public String getAlias() {
        return baseSubmittable.getAlias();
    }

    @Override
    public void setAlias(String alias) {
        baseSubmittable.setAlias(alias);
    }

    @Override
    public Team getTeam() {
        return baseSubmittable.getTeam();
    }

    @Override
    public void setTeam(Team team) {
        baseSubmittable.setTeam(team);
    }

    @Override
    public String getTitle() {
        return baseSubmittable.getTitle();
    }

    @Override
    public void setTitle(String title) {
        baseSubmittable.setTitle(title);
    }

    @Override
    public String getDescription() {
        return baseSubmittable.getDescription();
    }

    @Override
    public void setDescription(String description) {
        baseSubmittable.setDescription(description);
    }

    /**
     * Return null for a empty list to prevent moxy from creating an empty attributes element as the schema doesn't allow this
     */
    @Override
    public List<Attribute> getAttributes() {
        if (baseSubmittable.getAttributes().isEmpty()) {
            return null;
        } else {
            return baseSubmittable.getAttributes();
        }
    }

    @Override
    public void setAttributes(List<Attribute> attributes) {
        baseSubmittable.setAttributes(attributes);
    }

    @Override
    public Archive getArchive() {
        return baseSubmittable.getArchive();
    }

    @Override
    public void setArchive(Archive archive) {
        baseSubmittable.setArchive(archive);
    }

    @Override
    public boolean isAccessioned() {
        return baseSubmittable.isAccessioned();
    }

    public String getTeamName() {
        Team team = getTeam();
        if (team != null)
            return team.getName();
        else return null;
    }

    //@Override
    public void setTeamName(String teamName) {
        Team team = new Team();
        team.setName(teamName);
        setTeam(team);
    }

    public BaseSubmittable<T> getBaseSubmittable() {
        return baseSubmittable;
    }

    public void setBaseSubmittable(BaseSubmittable<T> baseSubmittable) {
        this.baseSubmittable = baseSubmittable;
    }

    @Override
    public T getBaseObject() {
        return (T)baseSubmittable;
    }
}
