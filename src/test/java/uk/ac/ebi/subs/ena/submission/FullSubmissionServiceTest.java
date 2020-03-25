package uk.ac.ebi.subs.ena.submission;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.Before;
import org.junit.Ignore;
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
import uk.ac.ebi.subs.ena.action.*;
import uk.ac.ebi.subs.ena.helper.TestHelper;
import uk.ac.ebi.subs.data.component.File;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
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

    public static final int SUBMITTABLE_COUNT = 1;
    private final String[] BIOSAMPLE_ACCESSIONS = {
            "SAMEA168881",
            "SAMEA168882",
            "SAMEA168883",
            "SAMEA168884",
            "SAMEA168885",
            "SAMEA168886",
            "SAMEA168887",
            "SAMEA168888",
            "SAMEA168889",
            "SAMEA168890"
    };

    @Autowired
    FullSubmissionService fullSubmissionService;

    Study[] submittedStudies;
    Study[] originalStudies;
    Sample[] submittedSamples;
    Sample[] originalSamples;
    Assay[] submittedAssays;
    Analysis[] analyses;


    AssayData[] assayDatas;
    String submissionAlias;
    Team team;
    Map<Class<? extends ActionService>,Object> parameterMap = new HashMap<>();
    List<SingleValidationResult> singleValidationResults = new ArrayList<>();

    @Before
    public void setup () throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        submissionAlias = UUID.randomUUID().toString();
        team = TestHelper.getTeam(UUID.randomUUID().toString());

        submittedStudies = new Study[SUBMITTABLE_COUNT];
        originalStudies = new Study[SUBMITTABLE_COUNT];
        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            final String studyAlias = UUID.randomUUID().toString();
            submittedStudies[i] = TestHelper.getStudy(studyAlias, team, "abstract", "Whole Genome Sequencing");
            originalStudies[i] = TestHelper.getStudy(studyAlias, team, "abstract", "Whole Genome Sequencing", LocalDate.now().plusMonths(1));
        }

        submittedSamples = new Sample[SUBMITTABLE_COUNT];
        originalSamples = new Sample[SUBMITTABLE_COUNT];
        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            final String sampleAlias = UUID.randomUUID().toString();
            submittedSamples[i] = TestHelper.getSample(sampleAlias,team);
            originalSamples[i] = TestHelper.getSample(sampleAlias,team, LocalDate.now().plusMonths(2));
        }

        submittedAssays = new Assay[SUBMITTABLE_COUNT];
        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            String assayAlias = UUID.randomUUID().toString();
            submittedAssays[i] = TestHelper.getAssay(assayAlias,team, BIOSAMPLE_ACCESSIONS[i], submittedStudies[0].getAlias());
        }

        analyses = new Analysis[SUBMITTABLE_COUNT];
        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            String alias = UUID.randomUUID().toString();
            analyses[i] = TestHelper.getSeqVarAnalysis(alias,team, BIOSAMPLE_ACCESSIONS[i], submittedStudies[0].getAlias());
        }

    }

    @Test
    public void submitStudies() throws Exception {
        parameterMap.put(StudyActionService.class, submittedStudies);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),Optional.empty(),parameterMap,singleValidationResults);
        assertThat(receipt.getSuccess(), is(true));
        for (Submittable submittable : submittedStudies) {
            assertThat(submittable.getAccession(), startsWith("ERP"));
        }
    }

    @Test
    public void submitStudiesWithReleaseDate() throws Exception {
        LocalDate expectedReleaseDate = LocalDate.now().plusMonths(1);

        parameterMap.put(StudyActionService.class, submittedStudies);

        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(
                submissionAlias,team.getName(),Optional.of(expectedReleaseDate),parameterMap,singleValidationResults);

        assertThat(receipt.getSuccess(), is(true));
        for (Submittable submittable : submittedStudies) {
            assertThat(submittable.getAccession(), startsWith("ERP"));
        }

        Stream.of(receipt.getSTUDYArray()).forEach(id -> {
            testHoldUntilDate(expectedReleaseDate, id.getHoldUntilDate());
        });
    }

    @Test
    public void submitAndUpdateStudies() throws Exception {
        submitStudies();

        for (int i = 0; i < originalStudies.length; i++) {
            originalStudies[i].setAccession(submittedStudies[i].getAccession());
            originalStudies[i].setDescription("updated description");
        }

        parameterMap.clear();
        parameterMap.put(StudyActionService.class, originalStudies);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),Optional.empty(),parameterMap,singleValidationResults);
        assertThat(receipt.getSuccess(), is(true));
    }

    @Test
    public void submitAndUpdateStudiesWithReleaseDate() throws Exception {
        submitStudies();

        for (int i = 0; i < originalStudies.length; i++) {
            originalStudies[i].setAccession(submittedStudies[i].getAccession());
            originalStudies[i].setDescription("updated description");
        }

        parameterMap.clear();
        parameterMap.put(StudyActionService.class, originalStudies);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),Optional.empty(),parameterMap,singleValidationResults);
        assertThat(receipt.getSuccess(), is(true));

        Stream.of(receipt.getSTUDYArray()).forEach(id -> {
            Study orgStudy = Stream.of(originalStudies).filter(study -> study.getAccession().equals(id.getAccession())).findFirst().get();
            testHoldUntilDate(orgStudy.getReleaseDate(), id.getHoldUntilDate());
        });
    }

    @Test
    public void submitStudiesAndSamples() throws Exception {
        parameterMap.put(StudyActionService.class, submittedStudies);
        parameterMap.put(SampleActionService.class, submittedSamples);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),Optional.empty(),parameterMap,singleValidationResults);
        assertThat(receipt.getSuccess(), is(true));
    }

    @Test
    public void submitStudiesAndSamplesWithReleaseDate() throws Exception {
        LocalDate expectedReleaseDate = LocalDate.now().plusMonths(1);

        parameterMap.put(StudyActionService.class, submittedStudies);
        parameterMap.put(SampleActionService.class, submittedSamples);

        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(
                submissionAlias,team.getName(),Optional.of(expectedReleaseDate),parameterMap,singleValidationResults);

        assertThat(receipt.getSuccess(), is(true));
        for (Submittable submittable : submittedStudies) {
            assertThat(submittable.getAccession(), startsWith("ERP"));
        }

        Stream.of(receipt.getSTUDYArray()).forEach(id -> {
            testHoldUntilDate(expectedReleaseDate, id.getHoldUntilDate());
        });

        Stream.of(receipt.getSAMPLEArray()).forEach(id -> {
            testHoldUntilDate(expectedReleaseDate, id.getHoldUntilDate());
        });
    }

    @Test
    public void submitAndUpdateStudiesAndSamplesWithReleaseDate() throws Exception {
        submitStudiesAndSamples();

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            originalStudies[i].setAccession(submittedStudies[i].getAccession());
            originalStudies[i].setDescription("updated description");

            originalSamples[i].setAccession(submittedSamples[i].getAccession());
            originalSamples[i].setDescription("updated description");
        }

        parameterMap.clear();
        parameterMap.put(StudyActionService.class, originalStudies);
        parameterMap.put(SampleActionService.class, originalSamples);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),Optional.empty(),parameterMap,singleValidationResults);
        assertThat(receipt.getSuccess(), is(true));

        Stream.of(receipt.getSTUDYArray()).forEach(id -> {
            Study orgStudy = Stream.of(originalStudies).filter(study -> study.getAccession().equals(id.getAccession())).findFirst().get();
            testHoldUntilDate(orgStudy.getReleaseDate(), id.getHoldUntilDate());
        });

        Stream.of(receipt.getSAMPLEArray()).forEach(id -> {
            Sample orgSample = Stream.of(originalSamples).filter(sample -> sample.getAccession().equals(id.getAccession())).findFirst().get();
            testHoldUntilDate(orgSample.getReleaseDate(), id.getHoldUntilDate());
        });
    }

    @Test
    public void submitSamples() throws Exception {
        parameterMap.put(SampleActionService.class, submittedSamples);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),Optional.empty(),parameterMap,singleValidationResults);
        assertThat(receipt.getSuccess(), is(true));
        for (Submittable submittable : submittedSamples) {
            assertThat(submittable.getAccession(), startsWith("ERS"));
        }
    }

    @Test
    public void submitAndUpdateSamples() throws Exception {
        submitSamples();

        for (int i = 0; i < originalSamples.length; i++) {
            originalSamples[i].setAccession(submittedSamples[i].getAccession());
            originalSamples[i].setDescription("updated description");
        }

        parameterMap.clear();
        parameterMap.put(SampleActionService.class, originalSamples);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),Optional.empty(),parameterMap,singleValidationResults);
        assertThat(receipt.getSuccess(), is(true));
        final RECEIPTDocument.RECEIPT.ACTIONS.Enum[] actionsArray = receipt.getACTIONSArray();
    }

    @Test
    public void submitStudyAndExperiment() throws Exception {
        parameterMap.put(StudyActionService.class, submittedStudies);
        parameterMap.put(AssayActionService.class, submittedAssays);
        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),Optional.empty(),parameterMap,singleValidationResults);
        assertThat(receipt.getSuccess(), is(true));
        for (Submittable submittable : submittedAssays) {
            assertThat(submittable.getAccession(), startsWith("ERX"));
        }
    }

    @Test
    @Ignore
    public void submitStudyExperimentAndRun() throws Exception {
        FTPClient ftpClient = connectToWebinFTP();
        parameterMap.put(StudyActionService.class, submittedStudies);
        parameterMap.put(AssayActionService.class, submittedAssays);

        assayDatas = new AssayData[SUBMITTABLE_COUNT];
        List<java.io.File> fileList = new ArrayList<>();

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            submittedAssays[i] = TestHelper.getAssay(UUID.randomUUID().toString(),team, BIOSAMPLE_ACCESSIONS[i], submittedStudies[0].getAlias());
            assayDatas[i] = TestHelper.getAssayData(UUID.randomUUID().toString(),team, submittedAssays[i].getAlias());

            File file = new File();
            file.setChecksum("2debfdcf79f03e4a65a667d21ef9de14");
            file.setChecksumMethod("MD5");
            String fileName = UUID.randomUUID().toString() + ".fastq.gz";
            createTestFile(fileName);
            final java.io.File uploadedFile = uploadFile(ftpClient, fileName);
            fileList.add(uploadedFile);
            file.setName(fileName);
            file.setType("fastq");
            assayDatas[i].getFiles().clear();
            assayDatas[i].getFiles().add(file);
        }

        parameterMap.put(AssayDataActionService.class,assayDatas);

        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),Optional.empty(),parameterMap,singleValidationResults);

        for (java.io.File file : fileList) {
            deleteFile(ftpClient,file.getName());
            file.delete();

        }

        assertThat(receipt.getSuccess(), is(true));
        for (Submittable submittable : assayDatas) {
            assertThat(submittable.getAccession(), startsWith("ERR"));
        }
    }

    @Test
    @Ignore //TODO fix this ASAP (#SUBS-1663)
    public void submitStudyAndSeqVarAnalysis() throws Exception {
        FTPClient ftpClient = connectToWebinFTP();
        parameterMap.put(StudyActionService.class, submittedStudies);

        List<java.io.File> fileList = new ArrayList<>();

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            File file = new File();
            file.setChecksum("2debfdcf79f03e4a65a667d21ef9de14");
            file.setChecksumMethod("MD5");

            String fileName = UUID.randomUUID().toString() + ".vcf.gz";
            createTestFile(fileName);

            final java.io.File uploadedFile = uploadFile(ftpClient, fileName);
            fileList.add(uploadedFile);
            file.setName(fileName);
            file.setType("vcf");

            analyses[i].getFiles().clear();
            analyses[i].getFiles().add(file);
        }

        parameterMap.put(SequenceVariationAnalysisActionService.class,analyses);

        final RECEIPTDocument.RECEIPT receipt = fullSubmissionService.submit(submissionAlias,team.getName(),Optional.empty(),parameterMap,singleValidationResults);

        for (java.io.File file : fileList) {
            deleteFile(ftpClient,file.getName());
            file.delete();

        }

        assertThat(receipt.getSuccess(), is(true));
        for (Submittable submittable : analyses) {
            assertThat(submittable.getAccession(), startsWith("ERZ"));
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

    private boolean deleteFile(FTPClient ftpClient, String fileName) throws IOException {
        return ftpClient.deleteFile(fileName);
    }

    private void createTestFile(String fastaFileName) throws IOException {
        List<String> lines = Arrays.asList("@SEQ_ID",
                "GATTTGGGGTTCAAAGCAGTATCGATCAAATAGTAAATCCATTTGTTCAACTCACAGTTT",
                "+",
                "!''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF>>>>>>CCCCCCC65");
        Path file = Paths.get(fastaFileName);
        Files.write(file, lines, Charset.forName("UTF-8"));
    }

    private void testHoldUntilDate(LocalDate expected, Calendar actual) {
        assertThat(actual.get(Calendar.YEAR), equalTo(expected.getYear()));
        //java.util.Calendar months start from 0. The ones returned by LocalDate start from 1.
        assertThat(actual.get(Calendar.MONTH), equalTo(expected.getMonthValue() - 1));
        assertThat(actual.get(Calendar.DAY_OF_MONTH), equalTo(expected.getDayOfMonth()));
    }
}