package uk.ac.ebi.subs.data.submittable;

/**
 * Created by neilg on 05/03/2017.
 */
public interface BaseSubmittableFactory {

    public static <T extends ENASubmittable> T create(Class<T> clasz, Submittable submittable) throws IllegalAccessException, InstantiationException {
        final T t = create(clasz);
        t.setBaseSubmittable(submittable);
        return t;
    }

    public static <T extends ENASubmittable> T create(Class<T> clasz) throws IllegalAccessException, InstantiationException {
        final T t = clasz.newInstance();
        return t;
    }

}
