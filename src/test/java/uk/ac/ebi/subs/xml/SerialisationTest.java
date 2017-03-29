package uk.ac.ebi.subs.xml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by neilg on 28/03/2017.
 */
public class SerialisationTest {
    static final Logger logger = LoggerFactory.getLogger(SerialisationTest.class);
    String ATTRIBUTE_MAPPING = "uk/ac/ebi/subs/data/component/attribute_mapping.xml";
    String SUBMITTABLE_PACKAGE = "uk.ac.ebi.subs.data.submittable";
    String COMPONENT_PACKAGE = "uk.ac.ebi.subs.data.component";

    DocumentBuilderFactory documentBuilderFactory = null;
    XPathFactory xPathFactory = null;
    XmlOptions xmlOptions = new XmlOptions();
    ArrayList<XmlError> validationErrors;
    ObjectMapper objectMapper = new ObjectMapper();
    Marshaller marshaller;

    public void setUp() throws IOException, JAXBException, URISyntaxException {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        validationErrors = new ArrayList<>();
        xmlOptions.setErrorListener(validationErrors);
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        xPathFactory = XPathFactory.newInstance();
    }

    public Document marshal (Object object , Marshaller marshaller) throws ParserConfigurationException, JAXBException {
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(object,new DOMResult(document));
        return document;
    }

    String executeXPathQuery (Document document, String xPathExpression) throws XPathExpressionException, TransformerException {
        final XPath xPath = xPathFactory.newXPath();
        StudySerialisationTest.logger.info(getDocumentString(document));
        final XPathExpression xpe = xPath.compile(xPathExpression);
        Node node = (Node) xpe.evaluate(document, XPathConstants.NODE);
        return node.getNodeValue();
    }

    String getDocumentString (Document document) throws TransformerException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    public StreamSource createStreamSource (String resourceName) throws URISyntaxException {
        final URL resource = getClass().getClassLoader().getResource(resourceName);
        URI uri = resource.toURI();
        File file = new File(uri);
        StreamSource streamSource = new StreamSource(file);
        return streamSource;
    }

    public Marshaller createMarshaller (Class cl, String objectPackage, String objectMapperResource,
                                        String componentPackage, String componentResource) throws URISyntaxException, JAXBException {

        Map<String, Source> metadata = new HashMap<String, Source>();
        metadata.put(objectPackage, createStreamSource(objectMapperResource));
        metadata.put(componentPackage, createStreamSource(componentResource));

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, metadata);

        JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {cl}, properties);

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }
}
