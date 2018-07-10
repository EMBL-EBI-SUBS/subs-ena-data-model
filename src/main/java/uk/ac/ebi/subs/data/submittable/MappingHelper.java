package uk.ac.ebi.subs.data.submittable;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.MarshallerProperties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MappingHelper {
    public static String ATTRIBUTE_MAPPING = "uk/ac/ebi/subs/data/component/attribute_mapping.xml";
    public static String SUBMITTABLE_PACKAGE = "uk.ac.ebi.subs.data.submittable";
    public static String COMPONENT_PACKAGE = "uk.ac.ebi.subs.data.component";
    public static String EXPERIMENT_MARSHALLER = "uk/ac/ebi/subs/data/submittable/experiment_mapping.xml";
    public static String RUN_MARSHALLER = "uk/ac/ebi/subs/data/submittable/run_mapping.xml";
    public static String SAMPLE_MARSHALLER = "uk/ac/ebi/subs/data/submittable/sample_mapping.xml";
    public static String STUDY_MARSHALLER = "uk/ac/ebi/subs/data/submittable/study_mapping.xml";
    public static String ANALYSIS_MARSHALLER = "uk/ac/ebi/subs/data/submittable/analysis_mapping.xml";


    public static StreamSource createStreamSource (String resourceName) throws URISyntaxException {
        final InputStream resourceAsStream = MappingHelper.class.getClassLoader().getResourceAsStream(resourceName);
        StreamSource streamSource = new StreamSource(resourceAsStream);
        return streamSource;
    }

    public static Marshaller createMarshaller (Class cl, String objectPackage, String objectMapperResource,
                                               String componentPackage, String componentResource) throws URISyntaxException, JAXBException {
        JAXBContext jaxbContext = getJaxbContext(cl, objectPackage, objectMapperResource, componentPackage, componentResource);

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS, false);
        return marshaller;
    }

    public static Unmarshaller createUnmarshaller (Class cl, String objectPackage, String objectMapperResource,
                                                   String componentPackage, String componentResource) throws URISyntaxException, JAXBException {
        JAXBContext jaxbContext = getJaxbContext(cl, objectPackage, objectMapperResource, componentPackage, componentResource);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller;
    }

    private static JAXBContext getJaxbContext(Class cl, String objectPackage, String objectMapperResource, String componentPackage, String componentResource) throws URISyntaxException, JAXBException {
        Map<String, Source> metadata = new HashMap<String, Source>();
        metadata.put(objectPackage, createStreamSource(objectMapperResource));
        metadata.put(componentPackage, createStreamSource(componentResource));

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, metadata);

        return JAXBContext.newInstance(new Class[] {cl}, properties);
    }
}
