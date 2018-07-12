package uk.ac.ebi.subs.ena.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ena.sra.xml.RECEIPTDocument;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.ena.action.ActionService;
import uk.ac.ebi.subs.ena.action.AssayActionService;
import uk.ac.ebi.subs.ena.action.AssayDataActionService;
import uk.ac.ebi.subs.ena.action.SampleActionService;
import uk.ac.ebi.subs.ena.action.SequenceVariationAnalysisActionService;
import uk.ac.ebi.subs.ena.action.StudyActionService;
import uk.ac.ebi.subs.ena.config.TypeProcessingConfig;
import uk.ac.ebi.subs.ena.submission.FullSubmissionService;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class ENAProcessor {
    protected static final Logger logger = LoggerFactory.getLogger(ENAProcessor.class);

    FullSubmissionService fullSubmissionService;
    TypeProcessingConfig typeProcessingConfig;

    public ENAProcessor(FullSubmissionService fullSubmissionService, TypeProcessingConfig typeProcessingConfig) {
        this.fullSubmissionService = fullSubmissionService;
        this.typeProcessingConfig = typeProcessingConfig;
    }

    public List<SingleValidationResult> process(SubmissionEnvelope submissionEnvelope) {
        String submissionId = submissionEnvelope.getSubmission().getId();
        String centerName = centerName(submissionEnvelope);

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

    private String centerName(SubmissionEnvelope submissionEnvelope) {
        Team team = submissionEnvelope.getSubmission().getTeam();

        if (team.getProfile() != null &&
                team.getProfile().get("centre name") != null &&
                !team.getProfile().get("centre name").isEmpty()) {
            return  team.getProfile().get("centre name");
        }

        return team.toString();
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
                final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionId, centerName, newParamMap, singleValidationResults);
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

        if (typeProcessingConfig.isStudiesEnabled()) {
            final Study[] studies = submissionEnvelope.getStudies().stream().filter(filter).toArray(Study[]::new);

            if (studies.length > 0) {
                paramMap.put(StudyActionService.class, studies);
            }
        }

        if (typeProcessingConfig.isSamplesEnabled()) {
            final Sample[] samples = submissionEnvelope.getSamples().stream().filter(filter).toArray(Sample[]::new);

            if (samples.length > 0) {
                paramMap.put(SampleActionService.class, samples);
            }
        }

        if (typeProcessingConfig.isAssaysEnabled()) {
            final Assay[] assays = submissionEnvelope.getAssays().stream()
                    .filter(filter).toArray(Assay[]::new);

            if (assays.length > 0) {
                paramMap.put(AssayActionService.class, assays);
            }
        }

        if (typeProcessingConfig.isAssayDataEnabled()) {
            final AssayData[] assayData = submissionEnvelope.getAssayData().stream().filter(filter).toArray(AssayData[]::new);

            if (assayData.length > 0) {
                paramMap.put(AssayDataActionService.class, assayData);
            }
        }

        if (typeProcessingConfig.isSequenceVariationEnabled()) {
            final Analysis[] seqVarAnalysis = submissionEnvelope.getAnalyses().stream()
                    .filter(filter)
                    .filter(analysis -> analysis.getAnalysisType() != null)
                    .filter(analysis -> analysis.getAnalysisType().equals("sequence variation") )
                    .toArray(Analysis[]::new);

            if (seqVarAnalysis.length > 0) {
                paramMap.put(SequenceVariationAnalysisActionService.class, seqVarAnalysis);
            }
        }
        return paramMap;
    }


}
