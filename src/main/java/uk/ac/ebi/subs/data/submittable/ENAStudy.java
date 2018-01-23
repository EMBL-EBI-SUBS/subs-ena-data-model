package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.StudyDataType;
import uk.ac.ebi.subs.ena.annotation.*;

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
    @ENAField(name = STUDY_TYPE)
    String studyType;

    @ENAField(name = STUDY_ABSTRACT)
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
        return study;
    }
}
