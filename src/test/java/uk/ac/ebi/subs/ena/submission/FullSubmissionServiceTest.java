package uk.ac.ebi.subs.ena.submission;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ena.sra.xml.RECEIPTDocument;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.ena.EnaAgentApplication;
import uk.ac.ebi.subs.ena.helper.TestHelper;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class FullSubmissionServiceTest {

    public static final int SUBMITTABLE_COUNT = 10;
    @Autowired
    FullSubmissionService fullSubmissionService;

    @Test
    public void submitStudies() throws Exception {
        String submissionAlias = UUID.randomUUID().toString();
        Team team = TestHelper.getTeam(UUID.randomUUID().toString());
        List<Study> studyList = new ArrayList<>();
        Study[] studies = new Study[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            studies[i] = TestHelper.getStudy(UUID.randomUUID().toString(), team, "abstract", "Whole Genome Sequencing");
        }


        Map<Class, Object> parameterMap = new HashMap<>();
        parameterMap.put(studies.getClass(),studies);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),parameterMap);
        assertThat(receipt.getSuccess(), is(true));
    }

    @Test
    public void submitStudiesAndSamples() throws Exception {
        String submissionAlias = UUID.randomUUID().toString();
        Team team = TestHelper.getTeam(UUID.randomUUID().toString());
        Study[] studies = new Study[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            studies[i] = TestHelper.getStudy(UUID.randomUUID().toString(), team, "abstract", "Whole Genome Sequencing");
        }

        Sample[] samples = new Sample[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            samples[i] = TestHelper.getSample(UUID.randomUUID().toString(),team);
        }

        Map<Class, Object> parameterMap = new HashMap<>();
        parameterMap.put(studies.getClass(),studies);
        parameterMap.put(samples.getClass(),samples);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),parameterMap);
        assertThat(receipt.getSuccess(), is(true));
    }

    @Test
    public void submitSamples() throws Exception {
        String submissionAlias = UUID.randomUUID().toString();
        Team team = TestHelper.getTeam(UUID.randomUUID().toString());
        List<Study> studyList = new ArrayList<>();
        Sample[] samples = new Sample[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            samples[i] = TestHelper.getSample(UUID.randomUUID().toString(),team);
        }

        Map<Class, Object> parameterMap = new HashMap<>();
        parameterMap.put(samples.getClass(),samples);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),parameterMap);
        assertThat(receipt.getSuccess(), is(true));
    }

    @Test
    public void submitStudySamplesAndExperiment() throws Exception {
        String submissionAlias = UUID.randomUUID().toString();
        Team team = TestHelper.getTeam(UUID.randomUUID().toString());
        final Study study = TestHelper.getStudy(UUID.randomUUID().toString(), team, "abstract", "Whole Genome Sequencing");
        Study[] studies = new Study[]{study};

        Sample[] samples = new Sample[SUBMITTABLE_COUNT];
        Assay[] assays = new Assay[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            samples[i] = TestHelper.getSample(UUID.randomUUID().toString(),team);
            assays[i] = TestHelper.getAssay(UUID.randomUUID().toString(),team,samples[i].getAlias(),study.getAlias());
        }

        Map<Class, Object> parameterMap = new HashMap<>();
        parameterMap.put(studies.getClass(),studies);
        parameterMap.put(samples.getClass(),samples);
        parameterMap.put(assays.getClass(),assays);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),parameterMap);
        assertThat(receipt.getSuccess(), is(true));
    }

}