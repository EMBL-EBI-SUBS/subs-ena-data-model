package uk.ac.ebi.subs.data.submittable;

/**
 * Created by neilg on 05/03/2017.
 */
interface BaseSubmittableFactory<T extends Submittable> extends ENASubmittable<T> {
    void setBaseSubmittable(Submittable submittable) throws IllegalAccessException;
    Submittable createNewSubmittable();

    static ENASubmittable create (Class<? extends BaseSubmittableFactory> clasz,
                                  Submittable submittable) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        BaseSubmittableFactory baseSubmittableFactory = clasz.newInstance();
        baseSubmittableFactory.setBaseSubmittable(submittable);
        baseSubmittableFactory.serialiseAttributes();
        return baseSubmittableFactory;
    }

    static ENASubmittable create (Class<? extends BaseSubmittableFactory> clasz) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        BaseSubmittableFactory baseSubmittableFactory = clasz.newInstance();
        return baseSubmittableFactory;
    }





}
