package uk.ac.ebi.subs.ena.action;

import org.apache.xmlbeans.XmlException;
import org.springframework.oxm.Marshaller;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface SubmittablesActionService<T extends Submittable> extends ActionService<T[]> {
    String getSchemaName ();
    InputStream getXMLInputStream (T[] submittables,List<SingleValidationResult> singleValidationResults) throws IOException, XmlException, TransformerException;
}
