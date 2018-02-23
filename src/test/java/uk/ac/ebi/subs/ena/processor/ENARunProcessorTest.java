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

    public static final String ACCESSION_ID_FROM_RECEIPT = "ERR2359245";
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

    private String runReceiptString;

    @Before
    public void setup() throws Exception {
        runReceiptString = convertXMLFileToString("receipts/run_receipt.xml");
    }

    @Test
    public void process() throws Exception {
        doReturn(runReceiptString).when(uniRestWrapper).postJson(eq("RUN"),anyMap());

        String alias = UUID.randomUUID().toString();
        final Team team = TestHelper.getTeam("test-team");

        final AssayData assayData = TestHelper.getAssayData(alias, team, alias);
        final ArrayList<SingleValidationResult> singleValidationResultList = new ArrayList<>();
        final ProcessingCertificate processingCertificate = enaRunProcessor.processAndConvertSubmittable(assayData, singleValidationResultList);
        assertThat(processingCertificate, is(equalTo(new ProcessingCertificate(assayData, Archive.Ena, ProcessingStatusEnum.Received, ACCESSION_ID_FROM_RECEIPT))));
    }

    private String convertXMLFileToString(String pathToFile) throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource(pathToFile).toURI());

        StringBuilder data = new StringBuilder();
        Stream<String> lines = Files.lines(path);
        lines.forEach(line -> data.append(line).append("\n"));
        lines.close();

        return data.toString();
    }

}