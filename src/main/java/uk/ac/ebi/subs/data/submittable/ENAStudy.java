package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.StudyDataType;
import uk.ac.ebi.subs.ena.annotation.*;

@ENAValidation(
        value = {
                @ENAFieldAttribute(attributeName = ENAStudy.STUDY_TYPE, required = true),
                @ENAFieldAttribute(attributeName = ENAStudy.STUDY_ABSTRACT, required = true)
        })
public class ENAStudy extends AbstractENASubmittable<Study> {

    public static final String STUDY_TYPE = "study_type";
    public static final String STUDY_ABSTRACT = "study_abstract";
    @ENAField(fieldName = STUDY_TYPE, values = {
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
            "Other"})
    String studyType;

    @ENAField(fieldName = STUDY_ABSTRACT)
    String studyAbstract;

    public ENAStudy(Study study) throws IllegalAccessException {
        super(study);
    }

    public ENAStudy() throws IllegalAccessException {
        super();
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

    @Override
    public Submittable createNewSubmittable() {
        final Study study = new Study();
        study.setStudyType(StudyDataType.Sequencing);
        return study;
    }
}
