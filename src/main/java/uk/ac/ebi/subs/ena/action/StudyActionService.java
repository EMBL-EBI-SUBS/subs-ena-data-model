package uk.ac.ebi.subs.ena.action;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import uk.ac.ebi.ena.sra.xml.STUDYSETDocument;
import uk.ac.ebi.ena.sra.xml.StudySetType;
import uk.ac.ebi.ena.sra.xml.StudyType;
import uk.ac.ebi.subs.data.submittable.ENAStudy;
import uk.ac.ebi.subs.data.submittable.Study;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class StudyActionService extends AbstractSubmittablesActionService<Study,ENAStudy> {

    public static final String SCHEMA = "study";

    public StudyActionService(@Qualifier(SCHEMA) Marshaller marshaller) {
        super(marshaller,SCHEMA,ENAStudy.class);
    }

    @Override
    String getSetElementName() {
        return "STUDY_SET";
    }

    @Override
    public Class<Study[]> getSubmittableClass() {
        return Study[].class;
    }
}
