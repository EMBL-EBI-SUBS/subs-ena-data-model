package uk.ac.ebi.subs.xml;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.ENASample;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.data.submittable.MappingHelper;
import uk.ac.ebi.subs.data.submittable.Sample;

import javax.xml.bind.JAXBException;
import javax.xml.transform.dom.DOMResult;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by neilg on 27/03/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class SampleSerialisationTest extends SerialisationTest {
    String SAMPLE_RESOURCE = "/uk/ac/ebi/subs/ena/submittable/sample_template.json";


    static String SAMPLE_ACCESSION_XPATH = "/SAMPLE/@accession";
    static String SAMPLE_ALIAS_XPATH = "/SAMPLE/@alias";
    static String SAMPLE_CENTER_NAME_XPATH = "/SAMPLE/@center_name";
    static String SAMPLE_TAXON_ID = "/SAMPLE[1]/SAMPLE_NAME[1]/TAXON_ID[1]/text()";
    static String SAMPLE_TITLE_XPATH = "/SAMPLE/TITLE[1]/text()";
    static String SAMPLE_DESCRIPTION_XPATH = "/SAMPLE/DESCRIPTION[1]/text()";
    static String SAMPLE_ATTRIBUTE = "/SAMPLE/SAMPLE_ATTRIBUTES[1]/SAMPLE_ATTRIBUTE";

    @Before
    public void setUp() throws IOException, JAXBException, URISyntaxException {
        super.setUp();
        marshaller = MappingHelper.createMarshaller(ENASample.class, MappingHelper.SUBMITTABLE_PACKAGE, MappingHelper.SAMPLE_MARSHALLER, MappingHelper.COMPONENT_PACKAGE, MappingHelper.ATTRIBUTE_MAPPING);
    }

    @Override
    protected String getName() {
        return "SAMPLE";
    }

    @Test
    public void testMarshalStudyAccession() throws Exception {
        Sample sample = new Sample();
        sample.setAccession(UUID.randomUUID().toString());
        ENASample enaSample = new ENASample(sample);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaSample,new DOMResult(document));
        String xmlSampleAccession = executeXPathQueryNodeValue(document,SAMPLE_ACCESSION_XPATH);
        assertThat("study accession serialised to XML", enaSample.getAccession(), equalTo(xmlSampleAccession));
    }

    @Test
    public void testMarshalStudyAlias() throws Exception {
        Sample sample = new Sample();
        sample.setAlias(UUID.randomUUID().toString());
        ENASample enaSample = new ENASample(sample);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaSample,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,SAMPLE_ALIAS_XPATH);
        assertThat("sample alias serialised to XML", enaSample.getAlias(), equalTo(str));
    }

    @Test
    public void testMarshalCenterName() throws Exception {
        Sample sample = new Sample();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        team.getProfile().put("center name","EBI");
        sample.setTeam(team);
        ENASample enaSample = new ENASample(sample);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaSample,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,SAMPLE_CENTER_NAME_XPATH);
        assertThat("sample center_name serialised to XML", team.getProfile().get("center name"), equalTo(str));
    }

    @Test
    public void testMarshalTaxonId() throws Exception {
        Sample sample = new Sample();
        sample.setTaxonId(9606l);
        ENASample enaSample = new ENASample(sample);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaSample,new DOMResult(document));
        String taxonIdString = executeXPathQueryNodeValue(document,SAMPLE_TAXON_ID);
        assertThat("sample center_name serialised to XML", taxonIdString, equalTo(sample.getTaxonId().toString()));
    }

    @Test
    public void testMarshallTitle() throws Exception {
        Sample sample = new Sample();
        sample.setTitle(UUID.randomUUID().toString());
        ENASample enaSample = new ENASample(sample);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaSample,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,SAMPLE_TITLE_XPATH);
        assertThat("sample center_name serialised to XML", sample.getTitle(), equalTo(str));
    }

    @Test
    public void testMarshallDescription() throws Exception {
        Sample sample = new Sample();
        sample.setDescription(UUID.randomUUID().toString());
        ENASample enaSample = new ENASample(sample);
        final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        marshaller.marshal(enaSample,new DOMResult(document));
        String str = executeXPathQueryNodeValue(document,SAMPLE_DESCRIPTION_XPATH);
        assertThat("sample center_name serialised to XML", sample.getDescription(), equalTo(str));
    }

    @Override
    protected ENASubmittable createENASubmittable() throws IllegalAccessException {
        return new ENASample();
    }

}
