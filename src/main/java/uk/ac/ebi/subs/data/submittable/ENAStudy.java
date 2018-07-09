package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.ProjectRef;
import uk.ac.ebi.subs.ena.annotation.ENAField;
import uk.ac.ebi.subs.ena.annotation.ENAFieldAttribute;
import uk.ac.ebi.subs.ena.annotation.ENAValidation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@ENAValidation(
        value = {
                @ENAFieldAttribute(
                        name = ENAStudy.STUDY_TYPE,
                        required = true,
                        allowedValues = {
                                "Whole Genome Sequencing",
                                "Metagenomics",
                                "Transcriptome Analysis",
                                "Resequencing",
                                "Epigenetics",
                                "Synthetic Genomics",
                                "Forensic or Paleo-genomics",
                                "Gene Regulation Study",
                                "Cancer Genomics",
                                "Population Genomics",
                                "RNASeq",
                                "Exome Sequencing",
                                "Pooled Clone Sequencing",
                                "Other"}
                ),
                @ENAFieldAttribute(name = ENAStudy.STUDY_ABSTRACT, required = true)
        })
public class ENAStudy extends AbstractENASubmittable<Study> {

    public static final String STUDY_TYPE = "study_type";
    public static final String STUDY_ABSTRACT = "study_abstract";
    public static final String USI_BIOSTUDY_ID = "USI-BIOSTUDY-ID";

    @ENAField(name = STUDY_TYPE)
    String studyType;

    @ENAField(name = STUDY_ABSTRACT)
    String studyAbstract;

    private ProjectRef projectRef;

    public ENAStudy(Study study) throws IllegalAccessException {
        super(study);
    }

    public ENAStudy() {
        super();
    }

    @Override
    public Submittable createNewSubmittable() {
        final Study study = new Study();
        return study;
    }

    public String getStudyType() {
        return studyType;
    }

    public void setStudyType(String studyType) {
        this.studyType = studyType;
    }

    public String getStudyAbstract() {
        return studyAbstract;
    }

    public void setStudyAbstract(String studyAbstract) {
        this.studyAbstract = studyAbstract;
    }

    public ProjectRef getProjectRef() {
        return projectRef;
    }

    public void setProjectRef(ProjectRef projectRef) {
        this.projectRef = projectRef;
    }

    /**
     * Returning an attribute list containing one ENA attribute representing the USI Study ProjectRef.
     */
    @Override
    public Map<String, Collection<Attribute>> getAttributes() {
        Map<String, Collection<Attribute>> attributes;
        ProjectRef projectRef = ((Study) baseSubmittable).getProjectRef();

        if (projectRef != null && projectRef.getAccession() != null && !super.getAttributes().containsKey(USI_BIOSTUDY_ID)) {

            attributes = super.getAttributes()
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Attribute attribute = new Attribute();
            attribute.setValue(projectRef.getAccession());
            attributes.put(USI_BIOSTUDY_ID, Arrays.asList(attribute));
            return attributes;
        }
        return super.getAttributes();
    }

    @Override
    public void setAttributes(Map<String, Collection<Attribute>> attributes) {
        if(attributes.containsKey(USI_BIOSTUDY_ID)) {
            ProjectRef projectRef = new ProjectRef();
            projectRef.setAccession(attributes.get(USI_BIOSTUDY_ID).iterator().next().getValue());
            ((Study) baseSubmittable).setProjectRef(projectRef);

            Map<String, Collection<Attribute>> filteredAttributes = attributes
                    .entrySet().stream()
                    .filter(entrySet -> !entrySet.getKey().equals(USI_BIOSTUDY_ID))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            super.setAttributes(filteredAttributes);
        } else {
            super.setAttributes(attributes);
        }
    }
}
