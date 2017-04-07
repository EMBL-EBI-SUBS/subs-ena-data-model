package uk.ac.ebi.subs.ena.config;


import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import org.springframework.core.io.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ENAMarshaller {

    static final Logger logger = LoggerFactory.getLogger(ENAMarshaller.class);
    static final String ATTRIBUTE_MAPPING = "uk/ac/ebi/subs/data/component/attribute_mapping.xml";
    static final String SUBMITTABLE_PACKAGE = "uk.ac.ebi.subs.data.submittable";
    static final String COMPONENT_PACKAGE = "uk.ac.ebi.subs.data.component";
    static final String STUDY_MARSHALLER = "uk/ac/ebi/subs/data/submittable/study_mapping.xml";



    public static Marshaller createMarshaller (Class cl, String objectPackage, String objectMapperResource,
                                        String componentPackage, String componentResource) throws URISyntaxException, JAXBException {
        Map<String, Source> metadata = new HashMap<String, Source>();
        metadata.put(objectPackage, createStreamSource(objectMapperResource));
        metadata.put(componentPackage, createStreamSource(componentResource));

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, metadata);

        JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {cl}, properties);

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS, false);
        return marshaller;
    }

    private static StreamSource createStreamSource (String resourceName) throws URISyntaxException {
        final URL resource = ENAMarshaller.class.getClassLoader().getResource(resourceName);
        URI uri = resource.toURI();
        File file = new File(uri);
        StreamSource streamSource = new StreamSource(file);
        return streamSource;
    }

}
