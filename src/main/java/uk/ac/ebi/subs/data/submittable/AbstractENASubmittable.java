package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.ENAAttribute;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.ena.annotation.ENAField;
import uk.ac.ebi.subs.ena.annotation.ENAFieldAttribute;
import uk.ac.ebi.subs.ena.annotation.ENAValidation;
import uk.ac.ebi.subs.ena.validation.AttributeRequiredValidationResult;
import uk.ac.ebi.subs.ena.validation.InvalidAttributeValue;
import uk.ac.ebi.subs.ena.validation.SingleAttributeValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract implmentation for all ENA submittables.
 * Contains the serialisation and de-serialisation code for translating ENA specific fields which are contained as
 * attributes in the USI model.
 * Created by neilg on 03/03/2017.
 */
public abstract class AbstractENASubmittable<T extends BaseSubmittable> implements ENASubmittable<T>  {

    private static final String CENTRE_NAME = "centre name";
    protected Submittable baseSubmittable;
    private List<SingleValidationResult> validationResultList = new ArrayList<>();

    public AbstractENASubmittable(Submittable baseSubmittable) throws IllegalAccessException {
        setBaseSubmittable(baseSubmittable);
    }

    public AbstractENASubmittable() {
        this.baseSubmittable = createNewSubmittable();
    }

    public void setBaseSubmittable(Submittable baseSubmittable) throws IllegalAccessException {
        this.baseSubmittable = baseSubmittable;
        serialiseAttributes();
    }

    public void serialiseAttributes () throws IllegalAccessException {

        if (this.getClass().isAnnotationPresent(ENAValidation.class)) {
            ENAValidation enaValidation = getEnaValidation();

            if (getId() == null )
                setId(UUID.randomUUID().toString());
            parseENAFieldAttributes(enaValidation);
            serialiseFields(this.getClass(), this);
        }
    }

    ENAValidation getEnaValidation() {
        return this.getClass().getAnnotation(ENAValidation.class);
    }

    private void parseENAFieldAttributes (ENAValidation enaValidation) {
        Map<String, String> attributefieldMap = new HashMap<>();

        for (ENAFieldAttribute enaFieldAttribute : enaValidation.value()) {
            final Attribute validatedAttribute = validate(enaFieldAttribute);

            if (getAttributes().containsKey(enaFieldAttribute.attributeFieldName()) && (validatedAttribute != null)) {
                final Attribute attribute = new Attribute();
                attribute.setValue(validatedAttribute.getValue());
                addAttribute(attributefieldMap.get(enaFieldAttribute.attributeFieldName()), attribute);
                getAttributes().remove(enaFieldAttribute.name());
                getAttributes().remove(enaFieldAttribute.attributeFieldName());

            } else if (validatedAttribute != null) {
                attributefieldMap.put(enaFieldAttribute.name(), validatedAttribute.getValue());
            }

        }
    }

    private Attribute validate(ENAFieldAttribute enaFieldAttribute) {
        Attribute validatedAttribute = null;
        String tagName = enaFieldAttribute.name();
        int attributeCount = 0;
        if (getAttributes().containsKey(tagName))
            attributeCount = getAttributes().get(tagName).size();

        if (attributeCount > 1) {
            validationResultList.add(new SingleAttributeValidationResult(this, tagName));
        } else if (attributeCount == 1) {
            validatedAttribute = getAttributes().get(tagName).iterator().next();
            if (enaFieldAttribute.allowedValues().length > 0) {
                checkAllowedValues(tagName,Arrays.asList(enaFieldAttribute.allowedValues()),validatedAttribute);
            }
        } else if (attributeCount == 0 && enaFieldAttribute.required()) {
            validationResultList.add(new AttributeRequiredValidationResult(this,tagName));
        }

        return validatedAttribute;
    }

    private void parseControlledValue(String name, List<String> allowedValueList) {
        if (getAttributes().containsKey(name)) {
            for (Iterator<Attribute> it = getAttributes().get(name).iterator(); it.hasNext(); ) {
                Attribute attribute = it.next();
                checkAllowedValues(name, allowedValueList, attribute);
            }
        }
    }

    private void checkAllowedValues(String fieldName, List<String> allowedValueList, Attribute attribute) {
        if (!allowedValueList.isEmpty() && !allowedValueList.contains(attribute.getValue())) {
            validationResultList.add(
                    new InvalidAttributeValue(
                            this,
                            attribute.getValue(),
                            fieldName,
                            allowedValueList.toArray(new String[0])));
        }
    }

    private void serialiseFields(Class<?> aClass, Object obj) throws IllegalAccessException {

        final Field[] fields = aClass.getDeclaredFields();

        for (Field field : fields ) {
            if (field.isAnnotationPresent(ENAField.class)) {
                final ENAField enaField = field.getAnnotation(ENAField.class);
                if (getAttributes().containsKey(enaField.name())) {
                    parseControlledValue(enaField.name(), Arrays.asList(enaField.values()));
                    final Collection<Attribute> attributes = getAttributes().get(enaField.name());
                    final Attribute attribute = attributes.iterator().next();
                    field.set(obj, attribute.getValue());
                    getAttributes().remove(enaField.name());
                }
            }

        }
    }

    public void deSerialiseAttributes () throws IllegalAccessException {
        deSerialiseFields(this.getClass(), this);
    }

    private void deSerialiseFields (Class<?> aClass, Object obj) throws IllegalAccessException {
        Map<String,String> attributefieldMap = new HashMap<>();

        if (this.getClass().isAnnotationPresent(ENAValidation.class)) {
            ENAValidation enaValidation = getEnaValidation();
            for (ENAFieldAttribute enaFieldAttribute : enaValidation.value()) {

                if (!enaFieldAttribute.attributeFieldName().equals(ENAFieldAttribute.NO_FIELD)) {
                    attributefieldMap.put(enaFieldAttribute.name(),enaFieldAttribute.attributeFieldName());
                }
            }
        }

        final Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields ) {
            if (field.isAnnotationPresent(ENAField.class)) {
                final ENAField enaField = field.getAnnotation(ENAField.class);
                final Object o = field.get(obj);
                if ( o != null ) {

                    if (attributefieldMap.containsKey(enaField.attributeName())) {
                        Attribute attribute1 = new Attribute();
                        attribute1.setValue(enaField.name());
                        addAttribute(attributefieldMap.get(enaField.attributeName()),attribute1);
                        Attribute attribute2 = new Attribute();
                        attribute2.setValue(o.toString());
                        addAttribute(enaField.attributeName(),attribute2);
                    } else {
                        Attribute attribute = new Attribute();
                        attribute.setValue(o.toString());
                        addAttribute(enaField.name(),attribute);
                    }

                }
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
        return ENASubmittable.getENAAlias(baseSubmittable.getAlias(),getTeamName());
    }

    @Override
    public void setAlias(String alias) {
        baseSubmittable.setAlias(ENASubmittable.removeENAAlias(alias));
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
    public Map<String, Collection<Attribute>> getAttributes() {
        return baseSubmittable.getAttributes();
    }

    @Override
    public void setAttributes(Map<String, Collection<Attribute>> attributes) {
        baseSubmittable.setAttributes(attributes);
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

    public void setTeamName(String teamName) {
        Team team = new Team();
        team.setName(teamName);
        setTeam(team);
    }

    @Override
    public T getBaseObject() {
        return (T)baseSubmittable;
    }

    public Map<String, Collection<Attribute>> getAttributesXML() {
        Map<String, Collection<Attribute>> map = null;
        if (baseSubmittable.getAttributes().isEmpty()) {
            return null;
        } else {
            final Map<String, Collection<Attribute>> attributes = baseSubmittable.getAttributes();
            return baseSubmittable.getAttributes();
        }
    }

    public String getCentreName(){
        if (this.getBaseObject().getTeam() != null && this.getBaseObject().getTeam().getProfile().containsKey(CENTRE_NAME)){
            return this.getBaseObject().getTeam().getProfile().get(CENTRE_NAME);
        }
        return null;
    }
    public void setCentreName(String centerName){
        if (this.getBaseObject().getTeam() != null){
            this.getBaseObject().getTeam().getProfile().put(CENTRE_NAME, centerName);
        }
    }

    @Override
    public List<ENAAttribute> getEnaAttributeList() {
        List<ENAAttribute> enaAttributeList = new ArrayList<>();

        final Map<String, Collection<Attribute>> attributes = getAttributes();

        if (attributes.isEmpty()) return null;

        for (String key : getAttributes().keySet()) {

            for (Attribute attributeValue :  getAttributes().get(key)) {
                ENAAttribute enaAttribute = new ENAAttribute(key,attributeValue.getValue(),attributeValue.getUnits());
                enaAttributeList.add(enaAttribute);
            }

        }

        return enaAttributeList;
    }

    @Override
    public void setEnaAttributeList(List<ENAAttribute> enaAttributeList) {

        for (ENAAttribute enaAttribute : enaAttributeList) {
            if (!getAttributes().containsKey(enaAttribute))
                getAttributes().put(enaAttribute.getTag(),new ArrayList<>());
            final Attribute attribute = new Attribute();
            attribute.setValue(enaAttribute.getValue());
            attribute.setUnits(enaAttribute.getUnits());
            getAttributes().get(enaAttribute.getTag()).add(attribute);
        }

    }

    public List<SingleValidationResult> getValidationResultList() {
        return validationResultList;
    }

    @Override
    public boolean isValid() {
        return validationResultList.isEmpty();
    }

    public void addAttribute (String name, Attribute attribute) {
        if (!getAttributes().containsKey(name)) {
            getAttributes().put(name,new ArrayList<Attribute>());
        }
        getAttributes().get(name).add(attribute);
    }

}
