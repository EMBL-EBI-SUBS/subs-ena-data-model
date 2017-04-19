package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.Attribute;

import java.util.List;

/**
 * Created by neilg on 09/04/2017.
 */
public interface ENASubmittable<T extends Submittable> extends Submittable {
    T getBaseObject ();
    void serialiseAttributes () throws IllegalAccessException;
    void deSerialiseAttributes () throws IllegalAccessException;
    List<Attribute> getAttributesXML();
    void setAttributesXML(List<Attribute> attributeList);
}
