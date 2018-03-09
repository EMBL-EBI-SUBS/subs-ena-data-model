package uk.ac.ebi.subs.ena.action;


import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.springframework.oxm.Marshaller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import uk.ac.ebi.ena.sra.xml.ObjectType;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.data.submittable.Submittable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSubmittablesActionService<S extends Submittable,T extends ENASubmittable<S>> implements SubmittablesActionService<S> {

    Class<T> enaClass;
    protected Marshaller marshaller;
    protected String schemaName = null;
    static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    static DocumentBuilder documentBuilder;
    static TransformerFactory transformerFactory;
    StringBuilder xmlBuffer = new StringBuilder();
    Boolean modify;

    static {
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public AbstractSubmittablesActionService(Marshaller marshaller, String schemaName,Class<T> enaClass) {
        this.marshaller = marshaller;
        this.schemaName = schemaName;
        this.enaClass = enaClass;
    }

    @Override
    public SubmissionType.ACTIONS.ACTION createActionXML(S[] submittableObject) {
        if (submittableObject != null) {
            modify = null;

            for (S submittable : submittableObject) {
                if (modify != null && modify.booleanValue() != submittable.isAccessioned()) {
                    throw new IllegalArgumentException("Submittables must be ALL accessioned (for the MODIFY ACTION) or all un-accessioned (for the CREATE action)");
                }
                modify = submittable.isAccessioned();
            }

            final SubmissionType.ACTIONS.ACTION action = SubmissionType.ACTIONS.ACTION.Factory.newInstance();

            if (modify) {
                final SubmissionType.ACTIONS.ACTION.MODIFY modifyAction = action.addNewMODIFY();
                modifyAction.setSchema(uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION.MODIFY.Schema.Enum.forString(getSchemaName()));
                modifyAction.setSource(getSchemaName() + ".xml");
            } else {
                final SubmissionType.ACTIONS.ACTION.ADD addAction = action.addNewADD();
                addAction.setSchema(uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION.ADD.Schema.Enum.forString(getSchemaName()));
                addAction.setSource(getSchemaName() + ".xml");
            }

            return action;
        } else {
            return null;
        }
    }

    @Override
    public String getSchemaName() {
        return schemaName;
    }

    protected Document getDocument (ENASubmittable submittable) throws IOException {
        final Document document = documentBuilder.newDocument();
        marshaller.marshal(submittable,new DOMResult(document));
        return document;
    }

    String getDocumentString (Node node) throws TransformerException {
        DOMSource domSource = new DOMSource(node);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    protected Document getDocument(final S[] submittableObject) throws IOException {
        final Document document = documentBuilder.newDocument();
        Element rootElement = document.createElement(getSetElementName());
        document.appendChild(rootElement);
        for (S submittable : submittableObject) {

            try {
                Document submittableDocument = getDocument(T.create(enaClass, submittable));
                final Node node = document.importNode(submittableDocument.getFirstChild(), true);
                try {
                    final String documentString = getDocumentString(node);
                    System.out.println(documentString);
                } catch (TransformerException e) {
                    e.printStackTrace();
                }
                rootElement.appendChild(node);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        return document;
    }

    private InputStream nodeToInputStream(Node node) throws TransformerException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result outputTarget = new StreamResult(outputStream);
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.transform(new DOMSource(node), outputTarget);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    @Override
    public InputStream getXMLInputStream(S[] submittables) throws IOException, XmlException, TransformerException {
        if (submittables != null && submittables.length > 0) {
            return nodeToInputStream(getDocument(submittables));
        }
        return null;
    }

    abstract String getSetElementName ();

}
