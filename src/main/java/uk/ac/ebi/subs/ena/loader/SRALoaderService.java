package uk.ac.ebi.subs.ena.loader;

import uk.ac.ebi.subs.data.submittable.ENASubmittable;

/**
 * Created by neilg on 12/04/2017.
 */
public interface SRALoaderService<T extends ENASubmittable> {
    boolean executeSRASubmission(T enaSubmittable, boolean validateOnly) throws Exception;
    boolean executeSRASubmission(String submittableType, String submissionXML, String submittableXML) throws Exception;
    String getSchema ();
    String [] getErrorMessages();
    String [] getInfoMessages();
    String getAccession();
}
