package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.ENAAttribute;
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
     * Returns a list of <code>ENAAttribute</code>
     * @return
     */
    List<ENAAttribute> getEnaAttributeList ();

    /**
     * Sets the <code>ENAAttribute</code> list
     * @param enaAttributeList
     */
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

    /**
     * Sets the underlying Submittable, all methods in <code>Submittable</code> interface should be delegating in
     * the implement class to this submittable
     * @param submittable
     * @throws IllegalAccessException
     */
    void setBaseSubmittable(Submittable submittable) throws IllegalAccessException;

    /**
     * Creates an empty <code>Submittable</code>
     * @return
     */
    Submittable createNewSubmittable();

    /**
     * Creates a new instance
     * @param   clasz   a class that implements this interface
     * @param   submittable a <Code>Submittable</Code> that will be used as the delegate
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    static <T extends ENASubmittable> T create(Class<T> clasz, Submittable submittable) throws IllegalAccessException, InstantiationException {
        final T t = create(clasz);
        t.setBaseSubmittable(submittable);
        return t;
    }

    /**
     * Creates a new instance
     * @param   clasz   a class that implements this interface
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    static <T extends ENASubmittable> T create(Class<T> clasz) throws IllegalAccessException, InstantiationException {
        final T t = clasz.newInstance();
        return t;
    }
}
