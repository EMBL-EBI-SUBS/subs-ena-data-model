package uk.ac.ebi.subs.ena.submission;

import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.*;

import java.util.*;

public class ENASubmissionEnvelope {
    private String submissionAccession;
    private String submissionAlias;
    private Team team;

    Map<String,ENASubmittablesEnvelope> enaSubmittablesEnvelopeMap = new HashMap();
    ENASubmittablesEnvelope<Project> projectENASubmittablesEnvelope = new ENASubmittablesEnvelope<Project>();
    ENASubmittablesEnvelope<Study> studyENASubmittablesEnvelope = new ENASubmittablesEnvelope<Study>();
    ENASubmittablesEnvelope<Sample> sampleENASubmittablesEnvelope = new ENASubmittablesEnvelope<Sample>();
    ENASubmittablesEnvelope<Assay> assayENASubmittablesEnvelope = new ENASubmittablesEnvelope<Assay>();
    ENASubmittablesEnvelope<AssayData> assayDataENASubmittablesEnvelope = new ENASubmittablesEnvelope<AssayData>();


    public ENASubmissionEnvelope() {
        enaSubmittablesEnvelopeMap.put("Project", projectENASubmittablesEnvelope);
        enaSubmittablesEnvelopeMap.put("Study", studyENASubmittablesEnvelope);
        enaSubmittablesEnvelopeMap.put("Sample", sampleENASubmittablesEnvelope);
        enaSubmittablesEnvelopeMap.put("Experiment", assayENASubmittablesEnvelope);
        enaSubmittablesEnvelopeMap.put("AssayData", assayDataENASubmittablesEnvelope);
    }

    public Collection<Project> getProjects() {
        return projectENASubmittablesEnvelope.getSubmittableCollection();
    }

    public void setProjects(Collection<Project> projects) {
        projectENASubmittablesEnvelope.setSubmittableCollection(projects);
    }

    public Collection<Study> getStudies() {
        return studyENASubmittablesEnvelope.getSubmittableCollection();
    }

    public void setStudies(Collection<Study> studies) {
        studyENASubmittablesEnvelope.setSubmittableCollection(studies);
    }

    public Collection<Sample> getSamples() {
        return sampleENASubmittablesEnvelope.getSubmittableCollection();
    }

    public void setSamples(Collection<Sample> samples) {
        sampleENASubmittablesEnvelope.setSubmittableCollection(samples);
    }

    public Collection<Assay> getAssays() {
        return assayENASubmittablesEnvelope.getSubmittableCollection();
    }

    public void setAssays(Collection<Assay> assays) {
        assayENASubmittablesEnvelope.setSubmittableCollection(assays);
    }

    public Collection<AssayData> getAssayDatas() {
        return assayDataENASubmittablesEnvelope.getSubmittableCollection();
    }

    public void setAssayDatas(Collection<AssayData> assayDatas) {
        assayDataENASubmittablesEnvelope.setSubmittableCollection(assayDatas);
    }

    public Map<String, ENASubmittablesEnvelope> getEnaSubmittablesEnvelopeMap() {
        return enaSubmittablesEnvelopeMap;
    }

    public String getSubmissionAccession() {
        return submissionAccession;
    }

    public void setSubmissionAccession(String submissionAccession) {
        this.submissionAccession = submissionAccession;
    }

    public String getSubmissionAlias() {
        return submissionAlias;
    }

    public void setSubmissionAlias(String submissionAlias) {
        this.submissionAlias = submissionAlias;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
