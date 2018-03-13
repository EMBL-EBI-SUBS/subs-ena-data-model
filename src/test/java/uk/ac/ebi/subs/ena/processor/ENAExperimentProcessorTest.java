package uk.ac.ebi.subs.ena.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.status.ProcessingStatusEnum;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.ENAExperiment;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.ena.EnaAgentApplication;
import uk.ac.ebi.subs.ena.helper.TestHelper;
import uk.ac.ebi.subs.processing.ProcessingCertificate;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by neilg on 18/05/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class ENAExperimentProcessorTest {

    @Autowired
    ENAExperimentProcessor enaExperimentProcessor;

    @Autowired
    ENASampleProcessor enaSampleProcessor;

    @Autowired
    ENAStudyProcessor enaStudyProcessor;

    static void process(ENAExperimentProcessor enaExperimentProcessor, String alias, Team team) throws Exception {
        final ENAExperiment enaExperiment = TestHelper.getENAExperiment(alias, team);
        final ProcessingCertificate processingCertificate = enaExperimentProcessor.process(enaExperiment);
        assertThat(processingCertificate, is(equalTo(new ProcessingCertificate(enaExperiment, Archive.Ena, ProcessingStatusEnum.Received, enaExperiment.getAccession()))));
    }

    @Test
    public void process() throws Exception {
        String alias = UUID.randomUUID().toString();
        final Team team = TestHelper.getTeam("test-team");
        ENASampleProcessorTest.process(enaSampleProcessor,alias, team);
        ENAStudyProcessorTest.process(enaStudyProcessor,alias,team);
        final Assay assay = TestHelper.getAssay(alias, team, alias, alias);

        final ArrayList<SingleValidationResult> singleValidationResultList = new ArrayList<>();
        final ProcessingCertificate processingCertificate = enaExperimentProcessor.processAndConvertSubmittable(assay, singleValidationResultList);

        assertThat(processingCertificate, is(equalTo(new ProcessingCertificate(assay, Archive.Ena, ProcessingStatusEnum.Received, assay.getAccession()))));
    }

    private static void addAttribute(Submittable submittable, String attributeName, String attributeValue) {
        Attribute attribute = new Attribute();
        attribute.setValue(attributeValue);
        submittable.addAttribute(attributeName, attribute);
    }
}