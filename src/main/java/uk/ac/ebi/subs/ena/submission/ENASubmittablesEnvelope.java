package uk.ac.ebi.subs.ena.submission;

import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.data.submittable.Submittable;

import java.util.*;

public class ENASubmittablesEnvelope<T extends Submittable> {
    private Map<String,T> aliasSubmittableMap = new HashMap();

    public ENASubmittablesEnvelope() {
    }

    public Map<String, T> getAliasSubmittableMap() {
        return aliasSubmittableMap;
    }

    public void setAliasSubmittableMap(Map<String, T> aliasSubmittableMap) {
        this.aliasSubmittableMap = aliasSubmittableMap;
    }

    public Collection<T> getSubmittableCollection() {
        return aliasSubmittableMap.values();
    }

    public void setSubmittableCollection(Collection<T> submittableCollection) {
        for (T submittable : submittableCollection) {
            aliasSubmittableMap.put(submittable.getAlias(),submittable);
        }
    }


}
