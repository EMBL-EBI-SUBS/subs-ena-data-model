package uk.ac.ebi.subs.ena.processor;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.xmlbeans.XmlException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.ebi.ena.sra.xml.RECEIPTDocument;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.ENAStudy;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.ena.EnaAgentApplication;
import uk.ac.ebi.subs.ena.action.ActionService;
import uk.ac.ebi.subs.ena.action.AssayActionService;
import uk.ac.ebi.subs.ena.action.SampleActionService;
import uk.ac.ebi.subs.ena.action.StudyActionService;
import uk.ac.ebi.subs.ena.helper.TestHelper;
import uk.ac.ebi.subs.ena.http.UniRestWrapper;
import uk.ac.ebi.subs.ena.submission.FullSubmissionService;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class ENAProcessorTest {

    @Autowired
    ENAProcessor enaProcessor;

    @MockBean
    private UniRestWrapper uniRestWrapper;

    String submissionAlias;
    Team team;
    String centreName;
    Submission submission;
    public static final int SUBMITTABLE_COUNT = 10;
    DocumentBuilder documentBuilder;
    XPath xpath;
    Transformer transformer;

    @MockBean
    RECEIPTDocument.RECEIPT emptyReceipt;

    public static final String STUDY_RECEIPT_XPATH_QUERY = "/RECEIPT/STUDY";
    public static final String SAMPLE_RECEIPT_XPATH_QUERY = "/RECEIPT/SAMPLE";
    public static final String ASSAY_RECEIPT_XPATH_QUERY = "/RECEIPT/EXPERIMENT";
    public static final String ASSAY_DATA_RECEIPT_XPATH_QUERY = "/RECEIPT/RUN";
    public static final String ANALYSIS_DATA_RECEIPT_XPATH_QUERY = "/RECEIPT/ANALYSIS";
    public static final String STUDY_SUBMISSION_RECEIPT_RESOURCE = "/receipts/study_submission_receipt.xml";
    public static final String MODIFY_STUDY_SUBMISSION_RECEIPT_RESOURCE = "/receipts/modify_study_submission_receipt.xml";
    private Study[] submittedStudies;
    private Study[] originalStudies;
    private Sample[] submittedSamples;
    private Sample[] originalSamples;
    private Assay[] submittedAssays;
    private Assay[] originalAssays;
    private Analysis[] submittedSeqVarAnalysis;
    private Analysis[] originalSeqVarAnalysis;

    @Before
    public void setup() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        documentBuilder = dbFactory.newDocumentBuilder();
        XPathFactory xPathfactory = XPathFactory.newInstance();
        xpath = xPathfactory.newXPath();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();

        team = new Team();
        centreName = "test-centre";
        team.setName("test-team");
        team.getProfile().put("centre name",centreName);
        submission = new Submission();
        submissionAlias = "sub-alias-" + UUID.randomUUID().toString();
        submission.setTeam(team);
        submission.setId(submissionAlias);
        submission.setSubmissionDate(new Date());

        submittedStudies = new Study[SUBMITTABLE_COUNT];
        originalStudies = new Study[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            String studyAlias = "study-alias-" + UUID.randomUUID().toString();
            submittedStudies[i] = TestHelper.getStudy(studyAlias, team, "abstract", "Whole Genome Sequencing");
            originalStudies[i] = TestHelper.getStudy(studyAlias, team, "abstract", "Whole Genome Sequencing");
        }

        submittedSamples = new Sample[SUBMITTABLE_COUNT];
        originalSamples = new Sample[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            String sampleAlias = "sample-alias-" + UUID.randomUUID().toString();
            submittedSamples[i] = TestHelper.getSample(sampleAlias,team);
            originalSamples[i] = TestHelper.getSample(sampleAlias,team);
        }

        submittedAssays = new Assay[SUBMITTABLE_COUNT];
        originalAssays = new Assay[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            String assayAlias = "assay-alias-" + UUID.randomUUID().toString();
            submittedAssays[i] = TestHelper.getAssay(assayAlias,team, submittedSamples[i].getAccession(), submittedStudies[0].getAlias());
            originalAssays[i] = TestHelper.getAssay(assayAlias,team, submittedSamples[i].getAccession(), submittedStudies[0].getAlias());
        }

        submittedSeqVarAnalysis = new Analysis[SUBMITTABLE_COUNT];
        originalSeqVarAnalysis = new Analysis[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            String alias = "analysis-alias-" + UUID.randomUUID().toString();
            submittedSeqVarAnalysis[i] = TestHelper.getSeqVarAnalysis(alias,team, submittedSamples[i].getAccession(), submittedStudies[0].getAlias());
            originalSeqVarAnalysis[i] = TestHelper.getSeqVarAnalysis(alias,team, submittedSamples[i].getAccession(), submittedStudies[0].getAlias());
        }

        RECEIPTDocument.RECEIPT.MESSAGES messages = mock(RECEIPTDocument.RECEIPT.MESSAGES.class);
        when(messages.getINFOArray()).thenReturn(new String[]{});
        when(messages.getERRORArray()).thenReturn(new String[]{});

        when(emptyReceipt.getMESSAGES()).thenReturn(messages);
    }

    @Test
    public void processSubmissionWithStudies() throws Exception {

        final Document studyReceiptDocument = getDocument(STUDY_SUBMISSION_RECEIPT_RESOURCE);
        String receipt = updateReceipt(submittedStudies,studyReceiptDocument,STUDY_RECEIPT_XPATH_QUERY,ENAStudy.class);
        doReturn(receipt).when(uniRestWrapper).postJson(anyMap());

        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setStudies(Arrays.asList(submittedStudies));
        submissionEnvelope.setSubmission(submission);
        final List<SingleValidationResult> singleValidationResultList = enaProcessor.process(submissionEnvelope);
        assertThat(singleValidationResultList.isEmpty(), Is.is(true));
    }

    @Test
    public void processSubmissionWithStudiesWithUpdates() throws Exception {
        processSubmissionWithStudies();

        for (int i =0; i < originalStudies.length; i++) {
            originalStudies[i].setAccession(submittedStudies[i].getAccession());
            originalStudies[i].setDescription("Updated description");
        }

        final Document studyReceiptDocument = getDocument(MODIFY_STUDY_SUBMISSION_RECEIPT_RESOURCE);
        String receipt = updateReceipt(originalStudies,studyReceiptDocument,STUDY_RECEIPT_XPATH_QUERY,ENAStudy.class);
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setStudies(Arrays.asList(originalStudies));
        submissionEnvelope.setSubmission(submission);
        final List<SingleValidationResult> singleValidationResultList = enaProcessor.process(submissionEnvelope);
        assertThat(singleValidationResultList.isEmpty(), Is.is(true));
    }

    @Test
    public void testAllSubmittablesHavingNullReleaseDate() throws XmlException, TransformerException, IOException {
        FullSubmissionService original = (FullSubmissionService)ReflectionTestUtils.getField(enaProcessor, "fullSubmissionService");

        FullSubmissionService mockedFSS = Mockito.mock(FullSubmissionService.class);
        ReflectionTestUtils.setField(enaProcessor, "fullSubmissionService", mockedFSS);

        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setStudies(Arrays.asList(submittedStudies));
        submissionEnvelope.setSamples(Arrays.asList(submittedSamples));
        submissionEnvelope.setSubmission(submission);

        LocalDate expectedReleaseDate = LocalDate.ofInstant(
                Instant.ofEpochMilli(submission.getSubmissionDate().getTime()), ZoneId.of("UTC"));

        AtomicInteger invocationCount = new AtomicInteger(0);

        when(mockedFSS.submit(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            Optional<LocalDate> subRelDate = invocation.getArgumentAt(2, Optional.class);
            Map<Class<? extends ActionService>,Object> paramMap = invocation.getArgumentAt(3, Map.class);

            invocationCount.incrementAndGet();

            assertThat(paramMap.size(), CoreMatchers.is(2));
            assertThat(subRelDate.get(), CoreMatchers.equalTo(expectedReleaseDate));

            return emptyReceipt;
        });

        try {
            enaProcessor.process(submissionEnvelope);
        } finally {
            ReflectionTestUtils.setField(enaProcessor, "fullSubmissionService", original);
        }

        assertThat(invocationCount.intValue(), CoreMatchers.equalTo(1));
    }

    @Test
    public void testSomeSubmittablesHavingNullReleaseDate() throws XmlException, TransformerException, IOException {
        FullSubmissionService original = (FullSubmissionService)ReflectionTestUtils.getField(enaProcessor, "fullSubmissionService");

        FullSubmissionService mockedFSS = Mockito.mock(FullSubmissionService.class);
        ReflectionTestUtils.setField(enaProcessor, "fullSubmissionService", mockedFSS);

        Study studyWithReleaseDate = TestHelper.getStudy(UUID.randomUUID().toString(), team, "abstract", "Whole Genome Sequencing", LocalDate.now().plusMonths(1));
        Study studyWithoutReleaseDate = TestHelper.getStudy(UUID.randomUUID().toString(), team, "abstract", "Whole Genome Sequencing");

        Sample sampleWithReleaseDate = TestHelper.getSample(UUID.randomUUID().toString(),team, LocalDate.now().plusMonths(2));
        Sample sampleWithoutReleaseDate = TestHelper.getSample(UUID.randomUUID().toString(),team);

        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setStudies(Arrays.asList(studyWithReleaseDate, studyWithoutReleaseDate));
        submissionEnvelope.setSamples(Arrays.asList(sampleWithReleaseDate, sampleWithoutReleaseDate));
        submissionEnvelope.setAssays(Arrays.asList(
                TestHelper.getAssay(UUID.randomUUID().toString(),team, null, studyWithReleaseDate.getAlias()),
                TestHelper.getAssay(UUID.randomUUID().toString(),team, null, studyWithoutReleaseDate.getAlias())));
        submissionEnvelope.setSubmission(submission);

        LocalDate expectedDefaultReleaseDate = LocalDate.ofInstant(
                Instant.ofEpochMilli(submission.getSubmissionDate().getTime()), ZoneId.of("UTC"));

        AtomicInteger invocationCount = new AtomicInteger(0);

        when(mockedFSS.submit(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            Optional<LocalDate> defaultReleaseDate = invocation.getArgumentAt(2, Optional.class);
            Map<Class<? extends ActionService>,Object> paramMap = invocation.getArgumentAt(3, Map.class);

            invocationCount.incrementAndGet();

            if (invocationCount.intValue() == 1) {//on first invocation
                assertThat(paramMap.size(), CoreMatchers.equalTo(3));

                List<Study> studies =  Arrays.asList((Study[])paramMap.get(StudyActionService.class));
                studies.forEach(study -> study.setAccession("acc-abc"));

                List<Sample> samples = Arrays.asList((Sample[])paramMap.get(SampleActionService.class));
                samples.forEach(sample -> sample.setAccession("acc-xyz"));

                assertThat(defaultReleaseDate.get(), CoreMatchers.equalTo(expectedDefaultReleaseDate));
                assertThat(studies.size(), CoreMatchers.is(2));
                assertThat(samples.size(), CoreMatchers.is(2));
                assertThat(((Assay[])paramMap.get(AssayActionService.class)).length, CoreMatchers.is(2));
            } else if (invocationCount.intValue() == 2) {//on second invocation
                assertThat(paramMap.size(), CoreMatchers.equalTo(2));

                List<Study> studies = Arrays.asList((Study[])paramMap.get(StudyActionService.class));
                List<Sample> samples = Arrays.asList((Sample[])paramMap.get(SampleActionService.class));

                assertThat(defaultReleaseDate, CoreMatchers.equalTo(Optional.empty()));
                assertThat(studies.size(), CoreMatchers.is(1));
                assertThat(studies.stream()
                        .allMatch(study -> study.getReleaseDate() != null), CoreMatchers.equalTo(true));
                assertThat(samples.size(), CoreMatchers.is(1));
                assertThat(samples.stream()
                        .allMatch(sample -> sample.getReleaseDate() != null), CoreMatchers.equalTo(true));
            }

            return emptyReceipt;
        });

        try {
            enaProcessor.process(submissionEnvelope);
        } finally {
            ReflectionTestUtils.setField(enaProcessor, "fullSubmissionService", original);
        }

        assertThat(invocationCount.intValue(), CoreMatchers.is(2));
    }

    @Test
    public void testAllSubmittablesHavingReleaseDate() throws XmlException, TransformerException, IOException {
        FullSubmissionService original = (FullSubmissionService)ReflectionTestUtils.getField(enaProcessor, "fullSubmissionService");

        FullSubmissionService mockedFSS = Mockito.mock(FullSubmissionService.class);
        ReflectionTestUtils.setField(enaProcessor, "fullSubmissionService", mockedFSS);

        Study studyWithReleaseDate = TestHelper.getStudy(UUID.randomUUID().toString(), team, "abstract", "Whole Genome Sequencing", LocalDate.now().plusMonths(1));

        Sample sampleWithReleaseDate = TestHelper.getSample(UUID.randomUUID().toString(),team, LocalDate.now().plusMonths(2));

        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setStudies(Arrays.asList(studyWithReleaseDate));
        submissionEnvelope.setSamples(Arrays.asList(sampleWithReleaseDate));
        submissionEnvelope.setAssays(Arrays.asList(
                TestHelper.getAssay(UUID.randomUUID().toString(),team, null, studyWithReleaseDate.getAlias())));
        submissionEnvelope.setSubmission(submission);

        AtomicInteger invocationCount = new AtomicInteger(0);

        when(mockedFSS.submit(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            Optional<LocalDate> defaultReleaseDate = invocation.getArgumentAt(2, Optional.class);
            Map<Class<? extends ActionService>,Object> paramMap = invocation.getArgumentAt(3, Map.class);

            invocationCount.incrementAndGet();

            if (invocationCount.intValue() == 1) {//on first invocation
                assertThat(paramMap.size(), CoreMatchers.equalTo(3));

                List<Study> studies =  Arrays.asList((Study[])paramMap.get(StudyActionService.class));
                studies.forEach(study -> study.setAccession("acc-abc"));

                List<Sample> samples = Arrays.asList((Sample[])paramMap.get(SampleActionService.class));
                samples.forEach(sample -> sample.setAccession("acc-xyz"));

                assertThat(defaultReleaseDate, CoreMatchers.equalTo(Optional.empty()));
                assertThat(studies.size(), CoreMatchers.is(1));
                assertThat(samples.size(), CoreMatchers.is(1));
                assertThat(((Assay[])paramMap.get(AssayActionService.class)).length, CoreMatchers.is(1));
            } else if (invocationCount.intValue() == 2) {//on second invocation
                assertThat(paramMap.size(), CoreMatchers.equalTo(2));

                List<Study> studies = Arrays.asList((Study[])paramMap.get(StudyActionService.class));
                List<Sample> samples = Arrays.asList((Sample[])paramMap.get(SampleActionService.class));

                assertThat(defaultReleaseDate, CoreMatchers.equalTo(Optional.empty()));
                assertThat(studies.size(), CoreMatchers.is(1));
                assertThat(studies.stream()
                        .allMatch(study -> study.getReleaseDate() != null), CoreMatchers.equalTo(true));
                assertThat(samples.size(), CoreMatchers.is(1));
                assertThat(samples.stream()
                        .allMatch(sample -> sample.getReleaseDate() != null), CoreMatchers.equalTo(true));
            }

            return emptyReceipt;
        });

        try {
            enaProcessor.process(submissionEnvelope);
        } finally {
            ReflectionTestUtils.setField(enaProcessor, "fullSubmissionService", original);
        }

        assertThat(invocationCount.intValue(), CoreMatchers.is(2));
    }

    //@Test
    public void processStudiesSubmission() throws Exception {

        submissionAlias = UUID.randomUUID().toString();
        team = TestHelper.getTeam(UUID.randomUUID().toString());
        List<Study> studyList = new ArrayList<>();

        final Document studyReceiptDocument = getDocument(STUDY_SUBMISSION_RECEIPT_RESOURCE);
        String studySubmissionReceiptString = getStudySubmissionReceipt(studyList, studyReceiptDocument);
        doReturn(studySubmissionReceiptString).when(uniRestWrapper).postJson(anyMap());

        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setStudies(studyList);
        submissionEnvelope.setSubmission(submission);
        final List<SingleValidationResult> singleValidationResultList = enaProcessor.process(submissionEnvelope);
        assertThat(singleValidationResultList.isEmpty(), Is.is(true));
    }

    private String getStudySubmissionReceipt(List<Study> studyList, Document receiptDocument) throws XPathExpressionException, IllegalAccessException, InvocationTargetException, InstantiationException, TransformerException {
        XPathExpression studyXpathExpression = xpath.compile(STUDY_RECEIPT_XPATH_QUERY);
        final NodeList nodeList = (NodeList) studyXpathExpression.evaluate(receiptDocument, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                final Study study = TestHelper.getStudy(UUID.randomUUID().toString(), team, "abstract", "Whole Genome Sequencing");
                Study originalStudy = new Study();
                BeanUtils.copyProperties(originalStudy,study);
                ENAStudy originalENAStudy = ENASubmittable.create(ENAStudy.class,originalStudy);
                Element element = (Element) nodeList.item(i);
                final NamedNodeMap attributes = element.getAttributes();
                Node nodeAttr = attributes.getNamedItem("alias");
                nodeAttr.setTextContent(originalENAStudy.getAlias());
                studyList.add(study);
            }
        }

        return getDocumentString(receiptDocument);
    }

    /**
     * Takes in existing list of submittedStudies an injects the alias into the receipt
     *
     */
    private String updateReceipt(Submittable[] submittableList, Document receiptDocument, String xpathQuery, Class<? extends ENASubmittable> enaClass) throws XPathExpressionException, IllegalAccessException, InvocationTargetException, InstantiationException, TransformerException, NoSuchMethodException {
        XPathExpression studyXpathExpression = xpath.compile(xpathQuery);
        final NodeList nodeList = (NodeList) studyXpathExpression.evaluate(receiptDocument, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                final Submittable submittable = submittableList[i];
                Element element = (Element) nodeList.item(i);
                final NamedNodeMap attributes = element.getAttributes();
                Node nodeAttr = attributes.getNamedItem("alias");
                nodeAttr.setTextContent(ENASubmittable.getENAAlias(submittable.getAlias(),submittable.getTeam().getName()));
            }
        }

        return getDocumentString(receiptDocument);
    }

    private Document getDocument (InputStream inputStream) throws IOException, SAXException {
        return documentBuilder.parse(inputStream);
    }

    private Document getDocument (String documentResource) throws IOException, SAXException {
        return documentBuilder.parse(getClass().getResourceAsStream(documentResource) );
    }

    String getDocumentString (Node node) throws TransformerException {
        DOMSource domSource = new DOMSource(node);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(domSource, result);
        return writer.toString();
    }
}