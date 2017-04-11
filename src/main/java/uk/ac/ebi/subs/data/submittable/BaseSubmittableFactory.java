package uk.ac.ebi.subs.data.submittable;

/**
 * Created by neilg on 05/04/2017.
 */
public interface BaseSubmittableFactory<T extends Submittable>   {
    T getBaseObject ();
    void setBaseSubmittable(BaseSubmittable baseSubmittable) throws IllegalAccessException;

    static BaseSubmittableFactory create (Class<? extends BaseSubmittableFactory> clasz,
                                          BaseSubmittable baseSubmittable) throws IllegalAccessException, InstantiationException {
        BaseSubmittableFactory baseSubmittableFactory = clasz.newInstance();
        baseSubmittableFactory.setBaseSubmittable(baseSubmittable);
        return baseSubmittableFactory;
    }
}
