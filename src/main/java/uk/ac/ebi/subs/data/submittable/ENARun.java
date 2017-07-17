package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.File;

import java.util.List;

/**
 * Created by neilg on 05/04/2017.
 */
public class ENARun extends AbstractENASubmittable<AssayData> {
    public ENARun(AssayData assayData) throws IllegalAccessException {
        super(assayData);
    }

    public ENARun () {
        super();
    }

    public AssayRef getAssayRef () {
        final AssayRef assayRef = getBaseObject().getAssayRef();
        assayRef.setAlias(getENAAlias(assayRef.getAlias(),assayRef.getTeam()));
        return assayRef;
    }

    public void setAssayRef (AssayRef assayRef) {
        assayRef.setAlias(removeENAAlias(assayRef.getAlias()));
        getBaseObject().setAssayRef(assayRef);
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
