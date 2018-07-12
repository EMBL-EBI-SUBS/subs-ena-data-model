package uk.ac.ebi.subs.data.submittable;


import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.StudyRef;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ENASequenceVariationAnalysis extends AbstractENASubmittable<Analysis> {

    public static final String ASSEMBLY_NAME_ATTRIBUTE = "assembly name";
    public static final String EXPERIMENT_TYPE_ATTRIBUTE = "experiment type";
    public static final String PROGRAM_ATRRIBUTE = "program";
    public static final String PLATFORM_ATTRIBUTE = "platform";
    public static final String IMPUTATION_ATTRIBUTE = "imputation";

    public ENASequenceVariationAnalysis(Analysis analysis) throws IllegalAccessException {
        super(analysis);
    }

    public ENASequenceVariationAnalysis() throws IllegalAccessException {
        super();
    }

    @Override
    public Submittable createNewSubmittable() {
        return new Analysis();
    }

    public List<StudyRef> getStudyRefs() {
        return this.getBaseObject().getStudyRefs();
    }

    public void setStudyRef(List<StudyRef> studyRefs) {
        this.getBaseObject().setStudyRefs(studyRefs);
    }

    public List<SampleRef> getSampleRefs() {
        return this.getBaseObject().getSampleRefs();
    }

    public void setSampleRefs(List<SampleRef> sampleRefs) {
        this.getBaseObject().setSampleRefs(sampleRefs);
    }

    public String getAssemblyName() {
        Map<String, Collection<Attribute>> attributes = this.getBaseObject().getAttributes();

        if (attributes.containsKey(ASSEMBLY_NAME_ATTRIBUTE)) {
            Collection<Attribute> assemblyNames = attributes.remove(ASSEMBLY_NAME_ATTRIBUTE);

            if (!assemblyNames.isEmpty()) {
                return assemblyNames.iterator().next().getValue();
            }
        }
        return null;
    }

    public void setAssemblyName(String assemblyName) {
        if (assemblyName != null) {
            setAttribute(ASSEMBLY_NAME_ATTRIBUTE, assemblyName);
        }
    }

    public String getExperimentType() {
        Map<String, Collection<Attribute>> attributes = this.getBaseObject().getAttributes();

        if (attributes.containsKey(EXPERIMENT_TYPE_ATTRIBUTE)) {
            Collection<Attribute> experimentTypes = attributes.remove(EXPERIMENT_TYPE_ATTRIBUTE);

            if (!experimentTypes.isEmpty()) {
                return experimentTypes.iterator().next().getValue();
            }
        }
        return null;
    }

    public void setExperimentType(String experimentType) {
        if (experimentType != null) {
            setAttribute(EXPERIMENT_TYPE_ATTRIBUTE, experimentType);
        }
    }

    public List<String> getPrograms() {
        Map<String, Collection<Attribute>> attributes = this.getBaseObject().getAttributes();

        if (attributes.containsKey(PROGRAM_ATRRIBUTE)) {
            Collection<Attribute> programAttributes = attributes.remove(PROGRAM_ATRRIBUTE);

            List<String> programs = programAttributes.stream()
                    .map(Attribute::getValue)
                    .collect(Collectors.toList());
            return programs;
        }
        return null;
    }

    public void setPrograms(List<String> programs) {
        if (programs != null) {
            setAttribute(PROGRAM_ATRRIBUTE, programs);
        }

    }

    public List<String> getPlatforms() {
        Map<String, Collection<Attribute>> attributes = this.getBaseObject().getAttributes();

        if (attributes.containsKey(PLATFORM_ATTRIBUTE)) {
            Collection<Attribute> platformAttributes = attributes.remove(PLATFORM_ATTRIBUTE);

            List<String> platforms = platformAttributes.stream()
                    .map(Attribute::getValue)
                    .collect(Collectors.toList());
            return platforms;
        }

        return null;
    }

    public void setPlatforms(List<String> platforms) {
        if (platforms != null) {
            setAttribute(PLATFORM_ATTRIBUTE, platforms);
        }

    }


    public String getImputation() {
        Map<String, Collection<Attribute>> attributes = this.getBaseObject().getAttributes();

        if (attributes.containsKey(IMPUTATION_ATTRIBUTE)) {
            Collection<Attribute> imputationAttributes = attributes.remove(IMPUTATION_ATTRIBUTE);

            Optional<String> imputation = imputationAttributes
                    .stream()
                    .map(attribute -> attribute.getValue())
                    .map(imputationValue -> {
                        if ("true".equalsIgnoreCase(imputationValue)) {
                            return "1";
                        }
                        if ("false".equalsIgnoreCase(imputationValue)) {
                            return "0";
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .findAny();
            return imputation.get();
        }
        return null;
    }

    public void setImputation(String imputation) {
        if (imputation != null) {
            if (imputation.equalsIgnoreCase("1")) {
                setAttribute(IMPUTATION_ATTRIBUTE, "true");
            }
            if (imputation.equalsIgnoreCase("0")) {
                setAttribute(IMPUTATION_ATTRIBUTE, "false");
            }
        }
    }

    private void setAttribute(String name, String value) {
        this.getBaseObject()
                .getAttributes()
                .put(
                        name,
                        Arrays.asList(
                                valueAttribute(value)
                        )
                );
    }

    private static Attribute valueAttribute(String value) {
        Attribute a = new Attribute();
        a.setValue(value);
        return a;
    }

    private void setAttribute(String name, Collection<String> values) {
        List<Attribute> attributes = values.stream().map(v -> valueAttribute(v)).collect(Collectors.toList());

        this.getBaseObject().getAttributes().put(name, attributes);
    }

}
