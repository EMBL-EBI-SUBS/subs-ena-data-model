package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.util.List;

/**
 * Created by neilg on 09/04/2017.
 */
public interface ENASubmittable<T extends Submittable> extends Submittable {
    /**
     * Gets the Submittable object that underpines this object
     * @return
     */
    T getBaseObject ();

    /**
     * Serialises attributes from a Submittable to fields in an ENASubmittable.
     * @throws IllegalAccessException
     */
    void serialiseAttributes () throws IllegalAccessException;

    /**
     * Deserialises fields from an ENASubmittable to attributes in a Submittable
     * @throws IllegalAccessException
     */
    void deSerialiseAttributes () throws IllegalAccessException;

    /**
     * Used internally via JAXB moxy during serialisation / deserilisation to XML
     * @return
     */
    List<Attribute> getAttributesXML();

    /**
     * Used internally via JAXB moxy during serialisation / deserilisation to XML
     * @return
     */
    void setAttributesXML(List<Attribute> attributeList);

    /**
     * Classes that implement this interface should create a list of SingleValidationResult objects
     * during serialisation to represent any validation errors
     */
    List<SingleValidationResult> getValidationResultList();

    /**
     * @return true if any validation errors occur during serialisation
     */
    boolean isValid();
}
