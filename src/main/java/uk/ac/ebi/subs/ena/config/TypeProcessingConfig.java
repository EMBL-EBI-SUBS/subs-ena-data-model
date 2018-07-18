package uk.ac.ebi.subs.ena.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ena.typeProcessing")
@Component
@Data
public class TypeProcessingConfig {

    private boolean studiesEnabled = true;
    private boolean samplesEnabled = true;
    private boolean assaysEnabled = true;
    private boolean assayDataEnabled = true;
    private boolean sequenceVariationEnabled = true;

}
