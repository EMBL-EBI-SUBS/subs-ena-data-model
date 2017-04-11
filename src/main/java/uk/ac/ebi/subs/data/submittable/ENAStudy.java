package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.ena.annotation.ENAAttribute;
import uk.ac.ebi.subs.ena.annotation.ENAValidation;

@ENAValidation(requiredAttributes = {"existing_study_type","study_abstract"})
public class ENAStudy extends AbstractENASubmittable<Study> {

    public static final String EXISTING_STUDY_TYPE = "existing_study_type";
    @ENAAttribute(name = EXISTING_STUDY_TYPE)
    String studyType;
    public static final String STUDY_ABSTRACT = "study_abstract";
    @ENAAttribute(name = STUDY_ABSTRACT)
    String studyAbstract;

    public ENAStudy (Study study) throws IllegalAccessException {
        super(study);
        //serialiseAttributes();
    }

    public ENAStudy () throws IllegalAccessException {
        super(new Study());
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
        return new Study();
    }
}
