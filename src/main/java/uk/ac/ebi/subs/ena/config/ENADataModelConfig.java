package uk.ac.ebi.subs.ena.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import org.springframework.core.io.Resource;

public class ENADataModelConfig {
    Jaxb2Marshaller jaxb2Marshaller;

    @Bean(name = "marshaller")
    @Value(value = "classpath:attribute_mapping.xml")
    Jaxb2Marshaller castorMarshaller (Resource schemaResource) {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setSchema(schemaResource);
        jaxb2Marshaller.setCheckForXmlRootElement(false);
        return jaxb2Marshaller;
    }

}
