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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

        List<SingleValidationResult> singleValidationResults = processNew(submissionId, centerName, newParamMap);

        singleValidationResults.addAll(processUpdate(submissionId + "_UPDATE", centerName, updateParamMap));

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

    //See Notes in README.md for clarification.
    private List<SingleValidationResult> processNew(String submissionId, String centerName, Map<Class<? extends ActionService>, Object> paramMap) {
        List<SingleValidationResult> singleValidationResults = new ArrayList<>();

        Map<Optional<LocalDate>, Map<Class<? extends ActionService>, Object>> releaseDateGroupedParamMaps = createReleaseDateGroups(paramMap);

        releaseDateGroupedParamMaps.forEach((releaseDate, group) -> {
            singleValidationResults.addAll(process(submissionId, centerName, releaseDate, group));
        });

        return singleValidationResults;
    }

    private List<SingleValidationResult> processUpdate(String submissionId, String centerName, Map<Class<? extends ActionService>, Object> paramMap) {
        List<SingleValidationResult> singleValidationResults = new ArrayList<>();

        singleValidationResults.addAll(process(submissionId, centerName, Optional.empty(), paramMap));

        return singleValidationResults;
    }

    /**
     * Submits submission to FullSubmissionService and processes the receipt to extract any errors
     *
     * @param submissionId
     * @param centerName
     * @param paramMap
     */
    private List<SingleValidationResult> process(String submissionId, String centerName, Optional<LocalDate> releaseDate, Map<Class<? extends ActionService>, Object> paramMap) {
        List<SingleValidationResult> singleValidationResults = new ArrayList<>();

        try {
            if (paramMap.size() > 0) {
                final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(
                        submissionId, centerName, releaseDate, paramMap, singleValidationResults);

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
                    .map(study -> setDefaultStudyReleaseDate(submissionEnvelope, study))
                    .toArray(Study[]::new);

            if (studies.length > 0) {
                paramMap.put(StudyActionService.class, studies);
            }
        }

        if (typeProcessingConfig.isSamplesEnabled()) {
            final Sample[] samples = submissionEnvelope.getSamples().stream()
                    .filter(filter)
                    .map(sample -> setDefaultSampleReleaseDate(submissionEnvelope, sample))
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

    private Study setDefaultStudyReleaseDate(SubmissionEnvelope submissionEnvelope, Study study) {
        Date subDate = submissionEnvelope.getSubmission().getSubmissionDate();

        if (subDate != null && !study.isAccessioned() && study.getReleaseDate() == null) {
            study.setReleaseDate(convertSubmissionDate(submissionEnvelope.getSubmission().getSubmissionDate()));
        }

        return study;
    }

    private Sample setDefaultSampleReleaseDate(SubmissionEnvelope submissionEnvelope, Sample sample) {
        Date subDate = submissionEnvelope.getSubmission().getSubmissionDate();

        if (subDate != null && !sample.isAccessioned() && sample.getReleaseDate() == null) {
            sample.setReleaseDate(convertSubmissionDate(submissionEnvelope.getSubmission().getSubmissionDate()));
        }

        return sample;
    }

    private LocalDate convertSubmissionDate(Date submissionDate) {
        return LocalDate.ofInstant(Instant.ofEpochMilli(submissionDate.getTime()), ZoneId.of("UTC"));
    }

    /**
     * Organize the action services and their submittables into groups with similar release dates.
     *
     * @param serviceSubmittableMap
     * @return
     */
    private Map<Optional<LocalDate>, Map<Class<? extends ActionService>, Object>> createReleaseDateGroups(
            Map<Class<? extends ActionService>, Object> serviceSubmittableMap) {

        //Map with date as the key and its group as value.
        Map<Optional<LocalDate>, Map<Class<? extends ActionService>, Object>> groups = new LinkedHashMap<>();

        serviceSubmittableMap.entrySet().stream().forEach(serviceSubmittableEntry -> {

            Class<? extends ActionService> service = serviceSubmittableEntry.getKey();
            Object serviceSubmittable = serviceSubmittableEntry.getValue();

            groupByReleaseDate(serviceSubmittable).entrySet().stream().forEach(dateSubmittableEntry -> {

                Optional<LocalDate> date = dateSubmittableEntry.getKey();
                Object dateSubmittable = dateSubmittableEntry.getValue();

                Map<Class<? extends ActionService>, Object> group = groups.get(date);
                if (group == null) {
                    group = new HashMap<>();
                    groups.put(date, group);
                }

                group.put(service, dateSubmittable);
            });
        });

        //Data types like assay can reference a study. Due to grouping, it is possible that such dependent data types
        //may get moved into a separate group that might get submitted before the group its referenced data is present in.
        //Following will ensure that all non-null release date datatypes get submitted before the null ones.
        Optional<LocalDate> emptyOpt = Optional.empty();
        if (groups.containsKey(emptyOpt)) {
            //Move the group with null release dates to bottom.
            groups.put(emptyOpt, groups.remove(Optional.empty()));
        }

        return groups;
    }

    private Map<Optional<LocalDate>, Object> groupByReleaseDate(Object submittableObjects) {
        Map<Optional<LocalDate>, Object> res = new HashMap<>();

        if (submittableObjects instanceof Study[]) {
            res.putAll(Arrays.asList((Study[])submittableObjects).stream().collect(Collectors.groupingBy(
                    study -> Optional.ofNullable(study.getReleaseDate()),
                    Collectors.collectingAndThen(Collectors.toList(), studies -> (Object)studies.toArray(new Study[studies.size()])))));
        } else if (submittableObjects instanceof Sample[]) {
            res.putAll(Arrays.asList((Sample[])submittableObjects).stream().collect(Collectors.groupingBy(
                    sample -> Optional.ofNullable(sample.getReleaseDate()),
                    Collectors.collectingAndThen(Collectors.toList(), samples -> (Object)samples.toArray(new Sample[samples.size()])))));
        } else {
            res.put(Optional.empty(), submittableObjects);
        }

        return res;
    }
}
