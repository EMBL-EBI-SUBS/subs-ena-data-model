package uk.ac.ebi.subs.ena.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ena.sra.xml.RECEIPTDocument;
import uk.ac.ebi.subs.data.submittable.*;
import uk.ac.ebi.subs.ena.action.*;
import uk.ac.ebi.subs.ena.submission.FullSubmissionService;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.util.*;
import java.util.function.Predicate;

@Service
public class ENAProcessor {
    protected static final Logger logger = LoggerFactory.getLogger(ENAProcessor.class);

    FullSubmissionService fullSubmissionService;

    public ENAProcessor(FullSubmissionService fullSubmissionService) {
        this.fullSubmissionService = fullSubmissionService;
    }

    public List<SingleValidationResult> process(SubmissionEnvelope submissionEnvelope) {
        String submissionId = submissionEnvelope.getSubmission().getId();
        String centerName = submissionEnvelope.getSubmission().getTeam().toString();

        Predicate<? super Submittable> newFilter = new Predicate<Submittable>() {
            @Override
            public boolean test(Submittable submittable) {
                return !submittable.isAccessioned();
            }
        };

        final Map<Class<? extends ActionService>, Object> newParamMap = createParamMap(submissionEnvelope, newFilter);

        Predicate<? super Submittable> updateFilter = new Predicate<Submittable>() {
            @Override
            public boolean test(Submittable submittable) {
                return submittable.isAccessioned();
            }
        };

        final Map<Class<? extends ActionService>, Object> updateParamMap = createParamMap(submissionEnvelope, updateFilter);

        List<SingleValidationResult> singleValidationResults = process(
                submissionId,
                centerName,
                newParamMap);
        singleValidationResults.addAll(process(
                submissionId + "_UPDATE",
                centerName,
                updateParamMap));

        return singleValidationResults;
    }

    /**
     * Submits submission to FullSubmissionService and processes the receipt to extract any errors
     *
     * @param submissionId
     * @param centerName
     * @param newParamMap
     */
    private List<SingleValidationResult> process(String submissionId, String centerName, Map<Class<? extends ActionService>, Object> newParamMap) {
        List<SingleValidationResult> singleValidationResults = new ArrayList<>();

        try {
            if (newParamMap.size() > 0) {
                final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionId, centerName, newParamMap,singleValidationResults);
                for (String infoMessage : receipt.getMESSAGES().getINFOArray()) {
                    logger.info("Info message from the ENA submission for submissionId " + submissionId + " : " + infoMessage);
                }
                for (String errorMessage : receipt.getMESSAGES().getERRORArray()) {
                    final SingleValidationResult singleValidationResult = new SingleValidationResult();
                    singleValidationResult.setMessage(errorMessage);
                    singleValidationResult.setValidationStatus(SingleValidationResultStatus.Error);
                    singleValidationResult.setValidationAuthor(ValidationAuthor.Ena);
                    singleValidationResults.add(singleValidationResult);
                }

            }
        } catch (Exception e) {
            logger.error("Error submitting submission " + submissionId + " to the ENA", e);
            throw new ENAProcessorException(e);
        }
        return singleValidationResults;
    }

    private Map<Class<? extends ActionService>, Object> createParamMap(SubmissionEnvelope submissionEnvelope, Predicate<? super Submittable> filter) {
        Map<Class<? extends ActionService>, Object> paramMap = new HashMap<>();
        final Study[] studies = submissionEnvelope.getStudies().stream().filter(filter).toArray(Study[]::new);

        if (studies.length > 0)
            paramMap.put(StudyActionService.class, studies);

        final Sample[] samples = submissionEnvelope.getSamples().stream().filter(filter).toArray(Sample[]::new);

        if (samples.length > 0)
            paramMap.put(SampleActionService.class, samples);

        final Assay[] assays = submissionEnvelope.getAssays().stream().filter(filter).toArray(Assay[]::new);

        if (assays.length > 0)
            paramMap.put(AssayActionService.class, assays);

        final AssayData[] assayData = submissionEnvelope.getAssayData().stream().filter(filter).toArray(AssayData[]::new);

        if (assayData.length > 0)
            paramMap.put(AssayDataActionService.class, assays);

        return paramMap;
    }


}
