package uk.ac.ebi.subs.data.submittable;

/**
 * Created by neilg on 09/04/2017.
 */
public interface ENASubmittable<T extends Submittable> extends Submittable {
    T getBaseObject ();
    void serialiseAttributes () throws IllegalAccessException;
    void deSerialiseAttributes () throws IllegalAccessException;
}
