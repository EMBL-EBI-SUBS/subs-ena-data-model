package uk.ac.ebi.subs.ena.action;

import org.apache.xmlbeans.XmlException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import uk.ac.ebi.ena.sra.xml.*;
import uk.ac.ebi.subs.data.submittable.*;

import java.io.IOException;

@Service
public class AssayActionService extends AbstractSubmittablesActionService<Assay,ENAExperiment> {

    public static final String SCHEMA = "experiment";

    public AssayActionService(@Qualifier(SCHEMA) Marshaller marshaller) {
        super(marshaller,SCHEMA,ENAExperiment.class);
    }

    @Override
    String getSetElementName() {
        return "EXPERIMENT_SET";
    }
}