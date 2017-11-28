package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.ena.component.ENAAttribute;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    List<ENAAttribute> getEnaAttributeList ();

    void setEnaAttributeList(List<ENAAttribute> enaAttributeList);

    /**
     * Classes that implement this interface should create a list of SingleValidationResult objects
     * during serialisation to represent any validation errors
     */
    List<SingleValidationResult> getValidationResultList();

    /**
     * @return true if any validation errors occur during serialisation
     */
    boolean isValid();

    void setBaseSubmittable(Submittable submittable) throws IllegalAccessException;

    Submittable createNewSubmittable();

    static <T extends ENASubmittable> T create(Class<T> clasz, Submittable submittable) throws IllegalAccessException, InstantiationException {
        final T t = create(clasz);
        t.setBaseSubmittable(submittable);
        return t;
    }

    static <T extends ENASubmittable> T create(Class<T> clasz) throws IllegalAccessException, InstantiationException {
        final T t = clasz.newInstance();
        return t;
    }
}
