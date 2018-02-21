package uk.ac.ebi.subs.ena.processor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.status.ProcessingStatusEnum;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.ena.EnaAgentApplication;
import uk.ac.ebi.subs.ena.helper.TestHelper;
import uk.ac.ebi.subs.ena.http.UniRestWrapper;
import uk.ac.ebi.subs.processing.ProcessingCertificate;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

/**
 * Created by karoly on 19/02/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class ENARunProcessorTest {

    @Autowired
    private ENAExperimentProcessor enaExperimentProcessor;

    @Autowired
    private ENARunProcessor enaRunProcessor;

    @Autowired
    private ENASampleProcessor enaSampleProcessor;

    @Autowired
    private ENAStudyProcessor enaStudyProcessor;

    @MockBean
    private UniRestWrapper uniRestWrapper;

    private String sampleReceiptString;
    private String studyReceiptString;
    private String experimentReceiptString;
    private String runReceiptString;

    @Before
    public void setup() throws Exception {
        sampleReceiptString = convertXMLFiletoString("receipts/sample_receipt.xml");
        studyReceiptString = convertXMLFiletoString("receipts/study_receipt.xml");
        experimentReceiptString = convertXMLFiletoString("receipts/experiment_receipt.xml");
        runReceiptString = convertXMLFiletoString("receipts/run_receipt.xml");
    }

    @Test
    public void process() throws Exception {
        doReturn(sampleReceiptString).when(uniRestWrapper).postJson(eq("SAMPLE"),anyMap());
        doReturn(studyReceiptString).when(uniRestWrapper).postJson(eq("STUDY"),anyMap());
        doReturn(experimentReceiptString).when(uniRestWrapper).postJson(eq("EXPERIMENT"),anyMap());
        doReturn(runReceiptString).when(uniRestWrapper).postJson(eq("RUN"),anyMap());

        String alias = UUID.randomUUID().toString();
        final Team team = TestHelper.getTeam("test-team");
        ENASampleProcessorTest.process(enaSampleProcessor,alias, team);
        ENAStudyProcessorTest.process(enaStudyProcessor,alias,team);
        ENAExperimentProcessorTest.process(enaExperimentProcessor, alias, team);

        final AssayData assayData = TestHelper.getAssayData(alias, team, alias);
        final ArrayList<SingleValidationResult> singleValidationResultList = new ArrayList<>();
        final ProcessingCertificate processingCertificate = enaRunProcessor.processAndConvertSubmittable(assayData, singleValidationResultList);
        assertThat(processingCertificate, is(equalTo(new ProcessingCertificate(assayData, Archive.Ena, ProcessingStatusEnum.Received, assayData.getAccession()))));
    }

    private String convertXMLFiletoString(String pathToFile) throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource(pathToFile).toURI());

        StringBuilder data = new StringBuilder();
        Stream<String> lines = Files.lines(path);
        lines.forEach(line -> data.append(line).append("\n"));
        lines.close();

        return data.toString();
    }

}