package uk.ac.ebi.subs.data.submittable;

import org.springframework.beans.BeanUtils;

import uk.ac.ebi.subs.data.component.Contacts;
import uk.ac.ebi.subs.data.component.Publications;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.ena.annotation.ENAAttribute;
import uk.ac.ebi.subs.ena.annotation.ENAValidation;

@ENAValidation(requiredAttributes = {"existing_study_type"})
public class ENAStudy extends Study implements ENASubmittable {

    public static final String EXISTING_STUDY_TYPE = "existing_study_type";
    String testString;
    @ENAAttribute(name = EXISTING_STUDY_TYPE)
    String studyType;

    public ENAStudy (Study study) throws IllegalAccessException {
        super();
        BeanUtils.copyProperties(study,this);
        serialiseAttributes();
    }

    public ENAStudy () {}

    public static ENAStudy createENAStudy(Study study) {
        ENAStudy enaStudy = new ENAStudy();
        BeanUtils.copyProperties(study,enaStudy);
        return enaStudy;
    }

    @Override
    public void setAccession(String accession) {
        super.setAccession(accession);
    }

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }


    @Override
    public String getTeamName() {
        Team team = getTeam();
        if (team != null)
            return team.getName();
        else return null;
    }

    @Override
    public void setTeamName(String teamName) {
        Team team = new Team();
        team.setName(teamName);
        setTeam(team);
    }

    public void setStudyType(String studyType) {
        this.studyType = studyType;
    }
}
