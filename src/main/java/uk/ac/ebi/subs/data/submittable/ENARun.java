package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.File;

import java.util.Arrays;
import java.util.List;

/**
 * Created by neilg on 05/04/2017.
 */
public class ENARun extends AbstractENASubmittable<AssayData> {
    public ENARun(AssayData assayData) throws IllegalAccessException {
        super(assayData);
    }

    public ENARun () throws IllegalAccessException {
        super();
    }

    public AssayRef getAssayRef () {
        if (getBaseObject().getAssayRefs() == null || getBaseObject().getAssayRefs().isEmpty()){
            return null;
        }
        final AssayRef assayRef = getBaseObject().getAssayRefs().iterator().next();
        assayRef.setAlias(ENASubmittable.getENAAlias(assayRef.getAlias(),assayRef.getTeam()));
        return assayRef;
    }

    public void setAssayRef (AssayRef assayRef) {
        assayRef.setAlias(ENASubmittable.removeENAAlias(assayRef.getAlias()));
        getBaseObject().setAssayRefs(Arrays.asList(assayRef));
    }

    public List<File> getFiles () {
        return getBaseObject().getFiles();
    }

    public void setFiles (List<File> files) {
        getBaseObject().setFiles(files);
    }

    @Override
    public Submittable createNewSubmittable() {
        return new AssayData();
    }
}
