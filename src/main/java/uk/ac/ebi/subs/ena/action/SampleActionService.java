package uk.ac.ebi.subs.ena.action;

import org.apache.xmlbeans.XmlException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import uk.ac.ebi.ena.sra.xml.STUDYSETDocument;
import uk.ac.ebi.ena.sra.xml.SampleType;
import uk.ac.ebi.ena.sra.xml.StudySetType;
import uk.ac.ebi.ena.sra.xml.StudyType;
import uk.ac.ebi.subs.data.submittable.ENASample;
import uk.ac.ebi.subs.data.submittable.ENAStudy;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;

import java.io.IOException;

@Service
public class SampleActionService extends AbstractSubmittablesActionService<Sample,ENASample> {

    public static final String SCHEMA = "sample";

    public SampleActionService(@Qualifier(SCHEMA) Marshaller marshaller) {
        super(marshaller,SCHEMA,ENASample.class);
    }

    @Override
    String getSetElementName() {
        return "SAMPLE_SET";
    }

    @Override
    public Class<Sample[]> getSubmittableClass() {
        return Sample[].class;
    }
}
