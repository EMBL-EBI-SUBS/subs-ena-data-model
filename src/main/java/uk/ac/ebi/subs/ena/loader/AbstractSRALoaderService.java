package uk.ac.ebi.subs.ena.loader;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.w3c.dom.Document;
import uk.ac.ebi.ena.sra.xml.ID;
import uk.ac.ebi.ena.sra.xml.RECEIPTDocument;
import uk.ac.ebi.ena.sra.xml.SUBMISSIONSETDocument;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.ena.http.UniRestWrapper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by neilg on 12/04/2017.
 */
public abstract class AbstractSRALoaderService<T extends ENASubmittable> implements SRALoaderService<T> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Marshaller marshaller;
    static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    static DocumentBuilder documentBuilder;
    static TransformerFactory transformerFactory;

    RECEIPTDocument.RECEIPT receipt = null;
    String accession = null;

    @Autowired
    UniRestWrapper uniRestWrapper;

    static {
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            transformerFactory = TransformerFactory.newInstance();
        } catch (ParserConfigurationException e) {
        }
    }

    @Override
    /**
     * Executes an ENA submission for a submittable using Unirest
     */
    public boolean executeSRASubmission(String submittableType, String submissionXML, String submittableXML) throws Exception {
        final InputStream submissionXMLInputStream = IOUtils.toInputStream(submissionXML, Charset.forName("UTF-8"));
        final InputStream submittableInputStream = IOUtils.toInputStream(submittableXML, Charset.forName("UTF-8"));


        Map<String, UniRestWrapper.Field> parameterMap = new HashMap<>();
        parameterMap.put(submittableType ,new UniRestWrapper.Field("submittable.xml", submittableInputStream));
        parameterMap.put("SUBMISSION", new UniRestWrapper.Field("submission.xml", submissionXMLInputStream));

        final String receiptString = uniRestWrapper.postJson(parameterMap);

        logger.info(receiptString);
        final RECEIPTDocument receiptDocument = RECEIPTDocument.Factory.parse(receiptString);
        receipt = receiptDocument.getRECEIPT();

        if (receipt.getSuccess()) {
            final ID[] iDs = getIDs(receipt);
            if (iDs.length != 1) {
                throw new Exception("Found " + iDs.length + " accessions");
            }
            accession = iDs[0].getAccession();

        }

        return receipt.getSuccess();
    }

    @Override
    /**
     * Executes an ENA for an ENA submittable
     */
    public boolean executeSRASubmission(ENASubmittable enaSubmittable, boolean validateOnly) throws Exception {
        final String submissionXML = createSubmissionXML(enaSubmittable, enaSubmittable.getId(), validateOnly);
        Document document = documentBuilder.newDocument();
        marshaller.marshal(enaSubmittable,new DOMResult(document));
        String submittableXML = getDocumentString(document);
        String submittableType = getSchema().toUpperCase();
        final boolean success = executeSRASubmission(submittableType, submissionXML, submittableXML);
        enaSubmittable.setAccession(getAccession());
        return success;
    }

    private String createSubmissionXML(ENASubmittable enaSubmittable, String submissionAlias, boolean validateOnly) {
        final SUBMISSIONSETDocument submissionsetDocument = SUBMISSIONSETDocument.Factory.newInstance();
        final SubmissionType submissionType = submissionsetDocument.addNewSUBMISSIONSET().addNewSUBMISSION();
        submissionType.setCenterName(enaSubmittable.getTeam().getName());
        submissionType.setAlias(submissionAlias);
        createActions(submissionType,enaSubmittable,getSchema(), validateOnly);
        return submissionsetDocument.xmlText();
    }

    /**
     * Creates the submission actions in the submissions XML
     *
     * @param submissionType
     * @param enaSubmittable
     * @param schema
     * @return
     */
    SubmissionType.ACTIONS createActions(SubmissionType submissionType, ENASubmittable enaSubmittable, String schema, boolean validateOnly) {
        final SubmissionType.ACTIONS actions = submissionType.addNewACTIONS();
        final SubmissionType.ACTIONS.ACTION action = actions.addNewACTION();
        if (enaSubmittable.isAccessioned()) {
            SubmissionType.ACTIONS.ACTION.MODIFY modify = action.addNewMODIFY();
            modify.setSchema(uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION.MODIFY.Schema.Enum.forString(schema));
        } else {
            SubmissionType.ACTIONS.ACTION.ADD add = action.addNewADD();
            add.setSchema(uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION.ADD.Schema.Enum.forString(schema));
            add.setSource(schema + ".xml");
        }
        if (validateOnly) actions.addNewACTION().addNewVALIDATE();
        return actions;
    }

    String getDocumentString (Document document) throws TransformerException {
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Override
    public String[] getErrorMessages() {
        String [] errorMessages = new String[0];
        if (receipt != null) {
            errorMessages = receipt.getMESSAGES().getERRORArray();
        }
        return errorMessages;
    }

    @Override
    public String[] getInfoMessages() {
        String [] infoMessages = new String[0];
        if (receipt != null) {
            infoMessages = receipt.getMESSAGES().getINFOArray();
        }
        return infoMessages;
    }

    @Override
    public String getAccession() {
        return accession;
    }

    abstract ID[] getIDs (RECEIPTDocument.RECEIPT receipt);
}
