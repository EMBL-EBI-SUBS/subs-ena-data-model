package uk.ac.ebi.subs.xml;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.ENAAnalysis;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.data.submittable.MappingHelper;
import uk.ac.ebi.subs.ena.helper.TestHelper;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;

public class AnalysisSerialisationTest extends SerialisationTest {

    private final static String ANALYSIS_RESOURCE = "/uk/ac/ebi/subs/ena/submittable/eva.analysis.json";

    @Override
    @Before
    public void setUp() throws IOException, JAXBException, URISyntaxException {
        super.setUp();
        marshaller = MappingHelper.createMarshaller(ENAAnalysis.class, MappingHelper.SUBMITTABLE_PACKAGE, MappingHelper.ANALYSIS_MARSHALLER, MappingHelper.COMPONENT_PACKAGE, MappingHelper.ATTRIBUTE_MAPPING);
        unmarshaller = MappingHelper.createUnmarshaller(ENAAnalysis.class, MappingHelper.SUBMITTABLE_PACKAGE, MappingHelper.ANALYSIS_MARSHALLER, MappingHelper.COMPONENT_PACKAGE, MappingHelper.ATTRIBUTE_MAPPING);
        marshaller.setProperty(MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS, false);
    }

    @Test
    public void testAnalysisSerialisation() throws IOException, IllegalAccessException, JAXBException, ParserConfigurationException, TransformerException {
        final Analysis analysisFromResource = TestHelper.getAnalysisFromResource(ANALYSIS_RESOURCE);
        ENAAnalysis enaAnalysis = new ENAAnalysis(analysisFromResource);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaAnalysis, new DOMResult(document));
        final String documentString = getDocumentString(document);
        logger.info(documentString);
        assertNotNull(enaAnalysis);
    }

    @Override
    protected ENASubmittable createENASubmittable() throws IllegalAccessException {
        return new ENAAnalysis();
    }

    @Override
    protected String getName() {
        return "ANALYSIS";
    }
}
