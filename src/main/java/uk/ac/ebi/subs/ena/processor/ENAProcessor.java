package uk.ac.ebi.subs.ena.processor;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ena.sra.xml.ID;
import uk.ac.ebi.ena.sra.xml.RECEIPTDocument;
import uk.ac.ebi.subs.data.submittable.*;
import uk.ac.ebi.subs.ena.action.*;
import uk.ac.ebi.subs.ena.submission.FullSubmissionService;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ENAProcessor {
    protected static final Logger logger = LoggerFactory.getLogger(ENAProcessor.class);

    FullSubmissionService fullSubmissionService;

    public ENAProcessor(FullSubmissionService fullSubmissionService) {
        this.fullSubmissionService = fullSubmissionService;
    }

    Collection<SingleValidationResult> process(SubmissionEnvelope submissionEnvelope) {
        String submissionId = submissionEnvelope.getSubmission().getId();
        String centerName = submissionEnvelope.getSubmission().getTeam().toString();
        List<SingleValidationResult> singleValidationResultList = new ArrayList<>();

        Predicate<? super Submittable> newFilter = new Predicate<Submittable>() {
            @Override
            public boolean test(Submittable submittable) {
                return !submittable.isAccessioned();
            }
        };

        final Map<Class<? extends ActionService>, Object> newParamMap = createParamMap(submissionEnvelope,newFilter);
        process(submissionId, centerName, newParamMap, "Error submitting new submission " + submissionEnvelope.getSubmission().getId() + " to the ENA");

        Predicate<? super Submittable> updateFilter = new Predicate<Submittable>() {
            @Override
            public boolean test(Submittable submittable) {
                return submittable.isAccessioned();
            }
        };

        final Map<Class<? extends ActionService>, Object> updateParamMap = createParamMap(submissionEnvelope,updateFilter);
        process(submissionId + "_UPDATE", centerName, updateParamMap, "Error submitting updates submission " + submissionEnvelope.getSubmission().getId() + " to the ENA");

        return singleValidationResultList;
    }

    private void process(String submissionId, String centerName, Map<Class<? extends ActionService>, Object> newParamMap, String msg) {
        try {
            final RECEIPTDocument.RECEIPT submit = fullSubmissionService.submit(submissionId, centerName, newParamMap);
            final ID[] studyArray = submit.getSTUDYArray();
            for (ID studyID : submit.getSTUDYArray()) {
                final ID.Status.Enum status = studyID.getStatus();
            }
        } catch (Exception e) {
            logger.error(msg, e);
            throw new ENAProcessorException(e);
        }
    }

    private Map<Class<? extends ActionService>,Object> createParamMap(SubmissionEnvelope submissionEnvelope, Predicate<? super Submittable> filter) {
        Map<Class<? extends ActionService>,Object> paramMap = new HashMap<>();
        if (submissionEnvelope.getStudies().size() > 0) {
            paramMap.put(StudyActionService.class,submissionEnvelope.getStudies().stream().filter(filter).toArray());
        }
        if (submissionEnvelope.getSamples().size() > 0) {
            paramMap.put(SampleActionService.class, submissionEnvelope.getSamples().stream().filter(filter).toArray());
        }
        if (submissionEnvelope.getAssays().size() > 0) {
            paramMap.put(AssayActionService.class,submissionEnvelope.getAssays().stream().filter(filter).toArray());
        }
        if (submissionEnvelope.getAssayData().size() > 0) {
            paramMap.put(AssayDataActionService.class,submissionEnvelope.getAssayData().stream().filter(filter).toArray());
        }

        return paramMap;
    }


}
