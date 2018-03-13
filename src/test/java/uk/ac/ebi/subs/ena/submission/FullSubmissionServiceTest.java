package uk.ac.ebi.subs.ena.submission;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.ac.ebi.subs.data.component.File;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class FullSubmissionServiceTest {
    @Value("${ena.ftp.url}")
    String enaFTPServerURL;

    @Value("${ena.login_name}")
    String ftpUsername;

    @Value("${ena.password}")
    String ftpPassword;

    private String FASTQ_FILE_NAME = "test_forward_" + UUID.randomUUID().toString() + ".gz";

    private String FASTQ_FILE = "src/test/resources/uk/ac/ebi/subs/ena/" + FASTQ_FILE_NAME;

    private File fastQFile;

    public static final int SUBMITTABLE_COUNT = 10;
    @Autowired
    FullSubmissionService fullSubmissionService;

    Study[] studies;
    Sample[] samples;
    Assay[] assays;
    AssayData[] assayDatas;
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

    //@Test
    public void submitStudySamplesExperimentAndRun() throws Exception {
        FTPClient ftpClient = connectToWebinFTP();
        parameterMap.put(StudyActionService.class,studies);
        parameterMap.put(SampleActionService.class,samples);
        parameterMap.put(AssayActionService.class,assays);

        assayDatas = new AssayData[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            samples[i] = TestHelper.getSample(UUID.randomUUID().toString(),team);
            assays[i] = TestHelper.getAssay(UUID.randomUUID().toString(),team,samples[i].getAlias(),studies[0].getAlias());
            assayDatas[i] = TestHelper.getAssayData(UUID.randomUUID().toString(),team,assays[i].getAlias());
            File file = new File();
            file.setChecksum("abcdefgh12345678abcdefgh12345678");
            file.setChecksumMethod("MD5");
            String fileName = UUID.randomUUID().toString() + ".fastq";
            createTestFile(fileName);
            java.io.File uploadedFile = uploadFile(ftpClient, fileName);
            file.setName(fileName);
            file.setType("fastq");
            assayDatas[i].getFiles().add(file);
        }

        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),parameterMap);
        assertThat(receipt.getSuccess(), is(true));
        for (Submittable submittable : assays) {
            assertThat(submittable.getAccession(), startsWith("ERX"));
        }
    }

    private FTPClient connectToWebinFTP() throws IOException {
        FTPClient ftp = new FTPClient();
        ftp.connect(enaFTPServerURL);
        ftp.enterLocalPassiveMode();
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

    private java.io.File uploadFile(FTPClient ftpClient, String fileName) throws IOException {
        java.io.File file = new java.io.File(fileName);
        InputStream inputStream = new FileInputStream(file);

        final boolean success = ftpClient.storeFile(file.getName(), inputStream);
        inputStream.close();
        if (!success) {
            throw new RuntimeException("Failed to upload file " + file.getName() + " to " + enaFTPServerURL);
        }
        return file;
    }

    private void createTestFile(String fastaFileName) throws IOException {
        List<String> lines = Arrays.asList("@SEQ_ID",
                "GATTTGGGGTTCAAAGCAGTATCGATCAAATAGTAAATCCATTTGTTCAACTCACAGTTT",
                "+",
                "!''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF>>>>>>CCCCCCC65");
        Path file = Paths.get(fastaFileName);
        Files.write(file, lines, Charset.forName("UTF-8"));
    }

}