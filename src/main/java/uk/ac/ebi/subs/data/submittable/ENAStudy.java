package uk.ac.ebi.subs.data.submittable;

import org.springframework.beans.BeanUtils;

import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.Contacts;
import uk.ac.ebi.subs.data.component.Publications;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.ena.annotation.ENAAttribute;
import uk.ac.ebi.subs.ena.annotation.ENAValidation;

import java.util.List;

@ENAValidation(requiredAttributes = {"existing_study_type"})
public class ENAStudy extends ENASubmittable<Study> {

    public static final String EXISTING_STUDY_TYPE = "existing_study_type";
    @ENAAttribute(name = EXISTING_STUDY_TYPE)
    String studyType;
    String description;

    public ENAStudy (Study study) throws IllegalAccessException {
        super(study);
        //BeanUtils.copyProperties(study,this);
        //serialiseAttributes();
        final BaseSubmittable<Study> baseSubmittable = getBaseSubmittable();
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

}
