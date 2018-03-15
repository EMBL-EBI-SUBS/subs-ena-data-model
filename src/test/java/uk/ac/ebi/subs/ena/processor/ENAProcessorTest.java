package uk.ac.ebi.subs.ena.processor;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.*;
import uk.ac.ebi.subs.ena.EnaAgentApplication;
import uk.ac.ebi.subs.ena.helper.TestHelper;
import uk.ac.ebi.subs.ena.http.UniRestWrapper;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;

import org.apache.commons.beanutils.BeanUtils;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class ENAProcessorTest {

    @Autowired
    ENAProcessor enaProcessor;

    @MockBean
    private UniRestWrapper uniRestWrapper;

    String submissionAlias;
    Team team;
    String centerName;
    Submission submission;
    public static final int SUBMITTABLE_COUNT = 10;
    DocumentBuilder documentBuilder;
    XPath xpath;
    Transformer transformer;

    public static final String STUDY_RECEIPT_XPATH_QUERY = "/RECEIPT/STUDY";
    public static final String SAMPLE_RECEIPT_XPATH_QUERY = "/RECEIPT/SAMPLE";
    public static final String ASSAY_RECEIPT_XPATH_QUERY = "/RECEIPT/EXPERIMENT";
    public static final String ASSAY_DATA_RECEIPT_XPATH_QUERY = "/RECEIPT/RUN";
    public static final String STUDY_SUBMISSION_RECEIPT_RESOURCE = "/receipts/study_submission_receipt.xml";
    public static final String MODIFY_STUDY_SUBMISSION_RECEIPT_RESOURCE = "/receipts/modify_study_submission_receipt.xml";
    private Study[] submittedStudies;
    private Study[] originalStudies;
    private Sample[] submittedSamples;
    private Sample[] originalSamples;
    private Assay[] submittedAssays;
    private Assay[] originalAssays;

    @Before
    public void setup() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        documentBuilder = dbFactory.newDocumentBuilder();
        XPathFactory xPathfactory = XPathFactory.newInstance();
        xpath = xPathfactory.newXPath();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();

        team = new Team();
        centerName = UUID.randomUUID().toString();
        team.setName(centerName);
        submission = new Submission();
        submissionAlias = UUID.randomUUID().toString();
        submission.setTeam(team);
        submission.setId(submissionAlias);

        submittedStudies = new Study[SUBMITTABLE_COUNT];
        originalStudies = new Study[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            submittedStudies[i] = TestHelper.getStudy(UUID.randomUUID().toString(), team, "abstract", "Whole Genome Sequencing");
            originalStudies[i] = (Study) BeanUtils.cloneBean(submittedStudies[i]);
        }

        submittedSamples = new Sample[SUBMITTABLE_COUNT];
        originalSamples = new Sample[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            submittedSamples[i] = TestHelper.getSample(UUID.randomUUID().toString(),team);
            originalSamples[i] = (Sample) BeanUtils.cloneBean(submittedSamples[i]);
        }

        submittedAssays = new Assay[SUBMITTABLE_COUNT];
        originalAssays = new Assay[SUBMITTABLE_COUNT];

        for (int i = 0; i < SUBMITTABLE_COUNT; i++) {
            submittedSamples[i] = TestHelper.getSample(UUID.randomUUID().toString(),team);
            submittedAssays[i] = TestHelper.getAssay(UUID.randomUUID().toString(),team, submittedSamples[i].getAlias(), submittedStudies[0].getAlias());
            originalAssays[i] = (Assay) BeanUtils.cloneBean(submittedAssays[i]);
        }
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
                final Submittable originalSubmittable = (Submittable) BeanUtils.cloneBean(submittable);
                final ENASubmittable enaSubmittable = ENASubmittable.create(enaClass, originalSubmittable);
                Element element = (Element) nodeList.item(i);
                final NamedNodeMap attributes = element.getAttributes();
                Node nodeAttr = attributes.getNamedItem("alias");
                nodeAttr.setTextContent(enaSubmittable.getAlias());
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