package uk.ac.ebi.subs.data.submittable;

/**
 * Created by neilg on 05/04/2017.
 */
public interface BaseSubmittableFactory<T extends Submittable>   {
    T getBaseObject ();
}
