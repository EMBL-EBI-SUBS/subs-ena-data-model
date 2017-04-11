package uk.ac.ebi.subs.data.submittable;

/**
 * Created by neilg on 05/04/2017.
 */
public interface BaseSubmittableFactory<T extends Submittable> extends ENASubmittable<T> {
    void setBaseSubmittable(Submittable submittable) throws IllegalAccessException;
    Submittable createNewSubmittable();

    static ENASubmittable create (Class<? extends BaseSubmittableFactory> clasz,
                                  Submittable submittable) throws IllegalAccessException, InstantiationException {
        BaseSubmittableFactory baseSubmittableFactory = clasz.newInstance();
        baseSubmittableFactory.setBaseSubmittable(submittable);
        return baseSubmittableFactory;
    }

    static ENASubmittable create (Class<? extends BaseSubmittableFactory> clasz) throws IllegalAccessException, InstantiationException {
        BaseSubmittableFactory baseSubmittableFactory = clasz.newInstance();
        final Submittable newSubmittable = baseSubmittableFactory.createNewSubmittable();
        return create(clasz,newSubmittable);
    }





}
