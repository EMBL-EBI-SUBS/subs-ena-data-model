package uk.ac.ebi.subs.ena.submission;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ena.sra.xml.RECEIPTDocument;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.*;
import uk.ac.ebi.subs.ena.EnaAgentApplication;
import uk.ac.ebi.subs.ena.action.ActionService;
import uk.ac.ebi.subs.ena.action.AssayActionService;
import uk.ac.ebi.subs.ena.action.SampleActionService;
import uk.ac.ebi.subs.ena.action.StudyActionService;
import uk.ac.ebi.subs.ena.helper.TestHelper;
import org.apache.commons.lang.SerializationUtils;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class FullSubmissionServiceTest {

    public static final int SUBMITTABLE_COUNT = 10;
    @Autowired
    FullSubmissionService fullSubmissionService;

    Study[] studies;
    Sample[] samples;
    Assay[] assays;
    AssayData assayData;
    String submissionAlias;
    Team team;
    Map<Class<? extends ActionService>,Object> parameterMap = new HashMap<>();

    @Before
    public void setup () {
        submissionAlias = UUID.randomUUID().toString();
        team = TestHelper.getTeam(UUID.randomUUID().toString());
        studies = new Study[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            studies[i] = TestHelper.getStudy(UUID.randomUUID().toString(), team, "abstract", "Whole Genome Sequencing");
        }

        samples = new Sample[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            samples[i] = TestHelper.getSample(UUID.randomUUID().toString(),team);
        }

        assays = new Assay[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            samples[i] = TestHelper.getSample(UUID.randomUUID().toString(),team);
            assays[i] = TestHelper.getAssay(UUID.randomUUID().toString(),team,samples[i].getAlias(),studies[0].getAlias());
        }
    }

    @Test
    public void submitStudies() throws Exception {
        parameterMap.put(StudyActionService.class,studies);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),parameterMap);
        assertThat(receipt.getSuccess(), is(true));
        for (Submittable submittable : studies) {
            assertThat(submittable.getAccession(), startsWith("ERP"));
        }
    }

    //@Test
    public void submitAndUpdateStudies() throws Exception {
        Study [] modifiedStudies = new Study[studies.length];

        submitStudies();
        for (Study study : studies) {
            study.setDescription("Modified description");
        }
        parameterMap.put(StudyActionService.class,studies);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),parameterMap);
        assertThat(receipt.getSuccess(), is(true));
    }


    @Test
    public void submitStudiesAndSamples() throws Exception {
        parameterMap.put(StudyActionService.class,studies);
        parameterMap.put(SampleActionService.class,samples);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),parameterMap);
        assertThat(receipt.getSuccess(), is(true));
    }

    @Test
    public void submitSamples() throws Exception {
        parameterMap.put(SampleActionService.class,samples);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),parameterMap);
        assertThat(receipt.getSuccess(), is(true));
        for (Submittable submittable : samples) {
            assertThat(submittable.getAccession(), startsWith("ERS"));
        }
    }

    @Test
    public void submitStudySamplesAndExperiment() throws Exception {
        parameterMap.put(StudyActionService.class,studies);
        parameterMap.put(SampleActionService.class,samples);
        parameterMap.put(AssayActionService.class,assays);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),parameterMap);
        assertThat(receipt.getSuccess(), is(true));
        for (Submittable submittable : assays) {
            assertThat(submittable.getAccession(), startsWith("ERX"));
        }
    }

}