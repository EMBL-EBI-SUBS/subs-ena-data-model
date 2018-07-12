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
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AnalysisSerialisationTest extends SerialisationTest {

    private final static String ANALYSIS_RESOURCE = "/uk/ac/ebi/subs/ena/submittable/eva.analysis.json";

    private final static String ASSEMBLY_REF_NAME_XPATH = "/ANALYSIS/ANALYSIS_TYPE/SEQUENCE_VARIATION/ASSEMBLY/STANDARD/@refname";
    private final static String SEQ_VAR_EXPERIMENT_TYPE_XPATH = "/ANALYSIS/ANALYSIS_TYPE/SEQUENCE_VARIATION/EXPERIMENT_TYPE/text()";
    private final static String SEQ_VAR_PLATFORM_XPATH = "/ANALYSIS/ANALYSIS_TYPE/SEQUENCE_VARIATION/PLATFORM/text()";
    private final static String SEQ_VAR_PROGRAM_XPATH = "/ANALYSIS/ANALYSIS_TYPE/SEQUENCE_VARIATION/PROGRAM/text()";
    private final static String SEQ_VAR_IMPUTATION_XPATH = "/ANALYSIS/ANALYSIS_TYPE/SEQUENCE_VARIATION/IMPUTATION/text()";

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
        Document document = marshallAnalysisResource();
        String documentString = getDocumentString(document);
        logger.info(documentString);

    }

    @Test
    public void testSequenceVariationAssembly() throws ParserConfigurationException, JAXBException, IllegalAccessException, IOException, XPathExpressionException, TransformerException {
        Document document = marshallAnalysisResource();
        String assembly = executeXPathQueryNodeValue(document, ASSEMBLY_REF_NAME_XPATH);
        assertThat("assembly name serialised to XML", assembly, equalTo("GRCh38"));
    }

    @Test
    public void testSequenceVariationExperimentType() throws ParserConfigurationException, JAXBException, IllegalAccessException, IOException, XPathExpressionException, TransformerException {
        Document document = marshallAnalysisResource();
        String experimentType = executeXPathQueryNodeValue(document, SEQ_VAR_EXPERIMENT_TYPE_XPATH);
        assertThat("experiment type serialised to XML", experimentType, equalTo("Genotyping by sequencing"));
    }

    @Test
    public void testSequenceVariationPlatform() throws ParserConfigurationException, JAXBException, IllegalAccessException, IOException, XPathExpressionException, TransformerException {
        Document document = marshallAnalysisResource();
        String platform = executeXPathQueryNodeValue(document, SEQ_VAR_PLATFORM_XPATH);
        assertThat("platform serialised to XML", platform, equalTo("Illumina"));
    }

    @Test
    public void testSequenceVariationProgram() throws ParserConfigurationException, JAXBException, IllegalAccessException, IOException, XPathExpressionException, TransformerException {
        Document document = marshallAnalysisResource();
        String program = executeXPathQueryNodeValue(document, SEQ_VAR_PROGRAM_XPATH);
        assertThat("program serialised to XML", program, equalTo("GATK"));
    }

    @Test
    public void testSequenceVariationImputation() throws ParserConfigurationException, JAXBException, IllegalAccessException, IOException, XPathExpressionException, TransformerException {
        Document document = marshallAnalysisResource();
        String imputation = executeXPathQueryNodeValue(document, SEQ_VAR_IMPUTATION_XPATH);
        assertThat("platform serialised to XML", imputation, equalTo("0"));
    }


    private Document marshallAnalysisResource() throws IOException, IllegalAccessException, ParserConfigurationException, JAXBException {
        final Analysis analysisFromResource = TestHelper.getAnalysisFromResource(ANALYSIS_RESOURCE);
        ENAAnalysis enaAnalysis = new ENAAnalysis(analysisFromResource);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaAnalysis, new DOMResult(document));
        return document;
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
