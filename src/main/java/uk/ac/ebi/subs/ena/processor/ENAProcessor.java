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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        final Map<Class<? extends ActionService>, Object> newParamMap = createParamMap(
                submissionEnvelope, (submittable -> !submittable.isAccessioned()));

        final Map<Class<? extends ActionService>, Object> updateParamMap = createParamMap(
                submissionEnvelope, (submittable -> submittable.isAccessioned()));

        List<SingleValidationResult> singleValidationResults = processNew(
                submissionEnvelope, submissionId, centerName, newParamMap);

        singleValidationResults.addAll(process(submissionId + "_UPDATE", centerName, Optional.empty(), updateParamMap));

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

    private List<SingleValidationResult> processNew(SubmissionEnvelope submissionEnvelope,
            String submissionId, String centerName, Map<Class<? extends ActionService>, Object> paramMap) {

        Study[] studies = (Study[])paramMap.get(StudyActionService.class);
        Sample[] samples = (Sample[])paramMap.get(SampleActionService.class);

        if (studies == null && samples == null) {
            return process(submissionId, centerName, Optional.empty(), paramMap);
        }

        List<SingleValidationResult> res;

        List<LocalDate> allReleaseDates = Stream.concat(
                studies == null ? Stream.empty() : Stream.of(studies).map(Study::getReleaseDate),
                samples == null ? Stream.empty() : Stream.of(samples).map(Sample::getReleaseDate))
                .collect(Collectors.toList());

        Date subReleaseDate = submissionEnvelope.getSubmission().getSubmissionDate();
        Optional<LocalDate> defaultReleaseDate = subReleaseDate == null
                ? Optional.empty()
                : Optional.of(convertSubmissionDate(subReleaseDate));

        //No release date specified in any submittable.
        if (allReleaseDates.stream().allMatch(Objects::isNull)) {
            //Submit with a default release date.
            res = process(submissionId, centerName, defaultReleaseDate, paramMap);

        //Some submittable have release dates and some don't.
        } else if (allReleaseDates.stream().anyMatch(Objects::isNull)) {
            //Submit with a default release date for submittables that don't have release dates.
            res = process(submissionId, centerName, defaultReleaseDate, paramMap);
            //Resubmit an update for submittables that have release dates (Per submittable release date is only allowed in updates).
            res.addAll(process(submissionId, centerName, Optional.empty(), createNonNullReleaseDateUpdateParamMap(
                    studies, samples)));

        //All submittable have release dates.
        } else {
            //Submit all submittables without a default release date.
            res = process(submissionId, centerName, Optional.empty(), paramMap);
            //Resubmit an update for release dates (Per submittable release date is only allowed in updates).
            res.addAll(process(submissionId, centerName, Optional.empty(), createNonNullReleaseDateUpdateParamMap(
                    studies, samples)));
        }

        return res;
    }

    /**
     * Submits submission to FullSubmissionService and processes the receipt to extract any errors
     *
     * @param submissionId
     * @param centerName
     * @param paramMap
     */
    private List<SingleValidationResult> process(
            String submissionId, String centerName, Optional<LocalDate> submissionReleaseDate,
            Map<Class<? extends ActionService>, Object> paramMap) {
        List<SingleValidationResult> singleValidationResults = new ArrayList<>();

        try {
            if (paramMap.size() > 0) {
                final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(
                        submissionId, centerName, submissionReleaseDate, paramMap, singleValidationResults);

                for (String infoMessage : receipt.getMESSAGES().getINFOArray()) {
                    logger.debug("Info message from the ENA submission for submissionId " + submissionId + " : " + infoMessage);
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
            final Study[] studies = submissionEnvelope.getStudies().stream()
                    .filter(filter)
                    .toArray(Study[]::new);

            if (studies.length > 0) {
                paramMap.put(StudyActionService.class, studies);
            }
        }

        if (typeProcessingConfig.isSamplesEnabled()) {
            final Sample[] samples = submissionEnvelope.getSamples().stream()
                    .filter(filter)
                    .toArray(Sample[]::new);

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
//TODO - add filter for analysis types once we have more than one
                    .toArray(Analysis[]::new);

            if (seqVarAnalysis.length > 0) {
                paramMap.put(SequenceVariationAnalysisActionService.class, seqVarAnalysis);
            }
        }
        return paramMap;
    }

    private Map<Class<? extends ActionService>, Object> createNonNullReleaseDateUpdateParamMap(
            Study[] studies, Sample[] samples) {
        Map<Class<? extends ActionService>, Object> resMap = new HashMap<>();

        //Studies and Samples should always have an accession at this point but they will still get checked for it
        //to avoid making unnecessary update in case they could not get accession numbers due to some other error.

        if (studies != null) {
            Study[] filteredStudies = Stream.of(studies)
                    .filter(study -> study.getReleaseDate() != null && study.getAccession() != null)
                    .toArray(Study[]::new);
            if (filteredStudies.length != 0) {
                resMap.put(StudyActionService.class, filteredStudies);
            }
        }

        if (samples != null) {
            Sample[] filteredSamples = Stream.of(samples)
                    .filter(sample -> sample.getReleaseDate() != null && sample.getAccession() != null)
                    .toArray(Sample[]::new);
            if (filteredSamples.length != 0) {
                resMap.put(SampleActionService.class, filteredSamples);
            }
        }

        return resMap;
    }

    private LocalDate convertSubmissionDate(Date submissionDate) {
        return LocalDate.ofInstant(Instant.ofEpochMilli(submissionDate.getTime()), ZoneId.of("UTC"));
    }
}
