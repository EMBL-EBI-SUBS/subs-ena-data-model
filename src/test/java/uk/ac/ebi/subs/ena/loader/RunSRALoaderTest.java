package uk.ac.ebi.subs.ena.loader;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ena.sra.xml.EXPERIMENTSETDocument;
import uk.ac.ebi.ena.sra.xml.RUNSETDocument;
import uk.ac.ebi.ena.sra.xml.SAMPLESETDocument;
import uk.ac.ebi.ena.sra.xml.STUDYSETDocument;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.ena.EnaAgentApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;
import static uk.ac.ebi.subs.ena.helper.TestHelper.getExperimentSetDocument;
import static uk.ac.ebi.subs.ena.helper.TestHelper.getRunSetDocument;
import static uk.ac.ebi.subs.ena.helper.TestHelper.getSamplesetDocument;
import static uk.ac.ebi.subs.ena.helper.TestHelper.getStudysetDocument;

/**
 * Created by neilg on 22/05/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class RunSRALoaderTest extends AbstractSRALoaderTest {


    @Value("${ena.ftp.url}")
    String enaFTPServerURL;

    @Value("${ena.login_name}")
    String ftpUsername;

    @Value("${ena.password}")
    String ftpPassword;

    private String FASTQ_FILE_NAME = "test_forward_" + UUID.randomUUID().toString() + ".gz";

    private String FASTQ_FILE = "src/test/resources/uk/ac/ebi/subs/ena/" + FASTQ_FILE_NAME;

    private FTPClient ftpClient;

    private File fastQFile;

    @Autowired
    StudySRALoader studySRALoader;

    @Autowired
    SampleSRALoader sampleSRALoader;

    @Autowired
    ExperimentSRALoader experimentSRALoader;

    @Autowired
    RunSRALoader runSRALoader;


    @Before
    public void setUp () throws Exception {
        ftpClient = connectToWebinFTP();

        createTestFile();

        fastQFile = new File(FASTQ_FILE);
        InputStream inputStream = new FileInputStream(fastQFile);

        final boolean success = ftpClient.storeFile(fastQFile.getName(), inputStream);
        inputStream.close();
        if (!success) {
            throw new RuntimeException("Failed to upload file " + fastQFile.getName() + " to " + enaFTPServerURL);
        }
    }

    @After
    public void finish () throws IOException {
        ftpClient.deleteFile(fastQFile.getName());
        ftpClient.disconnect();

        Files.deleteIfExists(Paths.get(FASTQ_FILE));
    }

    @Test
    public void executeSRALoader() throws Exception {
        String alias = UUID.randomUUID().toString();
        STUDYSETDocument studysetDocument = getStudysetDocument(alias,getCenterName());
        String studySubmissionXML = createSubmittable("study.xml", SubmissionType.ACTIONS.ACTION.ADD.Schema.STUDY,alias + "_study");
        studySRALoader.executeSRASubmission("STUDY", studySubmissionXML, studysetDocument.xmlText());
        final String studyAccession = studySRALoader.getAccession();
        assertThat(studyAccession, startsWith("ERP"));

        SAMPLESETDocument samplesetDocument = getSamplesetDocument(alias,getCenterName());
        String sampleSubmissionXML = createSubmittable("sample.xml", SubmissionType.ACTIONS.ACTION.ADD.Schema.SAMPLE,alias + "sample");
        sampleSRALoader.executeSRASubmission("SAMPLE", sampleSubmissionXML, samplesetDocument.xmlText());
        final String sampleAccession = sampleSRALoader.getAccession();
        assertThat(sampleAccession, startsWith("ERS"));

        EXPERIMENTSETDocument experimentsetDocument = getExperimentSetDocument(alias,alias,alias,getCenterName());
        String experimentSubmissionXML = createSubmittable("experiment.xml", SubmissionType.ACTIONS.ACTION.ADD.Schema.EXPERIMENT,alias);
        experimentSRALoader.executeSRASubmission("EXPERIMENT", experimentSubmissionXML, experimentsetDocument.xmlText());
        final String experimentAccession = experimentSRALoader.getAccession();
        assertThat(experimentAccession, startsWith("ERX"));

        RUNSETDocument runsetDocument = getRunSetDocument(alias, alias, getCenterName(), FASTQ_FILE_NAME,"fastq");
        String runSubmissionXML = createSubmittable("run.xml", SubmissionType.ACTIONS.ACTION.ADD.Schema.RUN,alias+ "_RUN");
        runSRALoader.executeSRASubmission("RUN", runSubmissionXML, runsetDocument.xmlText());
        final String runAccession = runSRALoader.getAccession();
        assertThat(runAccession, startsWith("ERR"));
    }

    private void createTestFile() throws IOException {
        List<String> lines = Arrays.asList("This is a TEST file.", "This is the second line.");
        Path file = Paths.get(FASTQ_FILE);
        Files.write(file, lines, Charset.forName("UTF-8"));
    }

    private FTPClient connectToWebinFTP() throws IOException {
        FTPClient ftp = new FTPClient();
        ftp.connect(enaFTPServerURL);
        int reply = ftp.getReplyCode();

        if (!FTPReply.isPositiveCompletion(reply))  {
            ftp.disconnect();
            throw new IOException("FTP server " + enaFTPServerURL + " refused connection");
        }

        if (!ftp.login(ftpUsername, ftpPassword)) {
            ftp.logout();
            throw new IOException("FTP server failed to login using username " + ftpUsername);
        }

        ftp.setFileType(FTP.BINARY_FILE_TYPE);

        return ftp;
    }
}