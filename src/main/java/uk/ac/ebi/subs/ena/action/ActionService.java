package uk.ac.ebi.subs.ena.action;

import uk.ac.ebi.ena.sra.xml.SubmissionType;

public interface ActionService<T> {
    SubmissionType.ACTIONS.ACTION createActionXML (T submittableObject) throws IllegalArgumentException;
    Class<T> getSubmittableClass ();
    String getName();
}
