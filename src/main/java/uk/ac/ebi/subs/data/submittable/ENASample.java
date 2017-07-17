package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.ena.annotation.ENAValidation;

/**
 * Created by neilg on 27/03/2017.
 * Purely used for testing the agent as Samples are not submitted to ENA
 */
public class ENASample extends AbstractENASubmittable<Sample> {
    public ENASample(Submittable baseSubmittable) throws IllegalAccessException {
        super(baseSubmittable);
    }

    public ENASample() {
        super();
    }

    @Override
    public Submittable createNewSubmittable() {
        return new Sample();
    }

    public Long getTaxonId () {
        return getBaseObject().getTaxonId();
    }

    public void setTaxonId (Long taxonId) {
        getBaseObject().setTaxonId(taxonId);
    }

}
