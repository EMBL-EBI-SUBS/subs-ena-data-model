package uk.ac.ebi.subs.ena.action;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.ENASequenceVariationAnalysis;

@Service
public class SequenceVariationAnalysisActionService extends AbstractSubmittablesActionService<Analysis, ENASequenceVariationAnalysis> {

    public static final String SCHEMA = "sequenceVariationAnalysis";

    public SequenceVariationAnalysisActionService(@Qualifier(SCHEMA) Marshaller marshaller) {
        super(marshaller, SCHEMA, ENASequenceVariationAnalysis.class);
    }

    @Override
    String getSetElementName() {
        return "ANALYSIS_SET";
    }

    @Override
    public Class<Analysis[]> getSubmittableClass() {
        return Analysis[].class;
    }
}
