package uk.ac.ebi.subs.data.submittable;


public class ENAAnalysis extends AbstractENASubmittable<Analysis> {

    public ENAAnalysis(Analysis analysis) throws IllegalAccessException {
        super(analysis);
    }

    public ENAAnalysis() throws IllegalAccessException {
        super();
    }

    @Override
    public Submittable createNewSubmittable() {
        return new Analysis();
    }
}
