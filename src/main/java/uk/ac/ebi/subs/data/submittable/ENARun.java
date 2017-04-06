package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.File;

import java.util.List;

/**
 * Created by neilg on 05/04/2017.
 */
public class ENARun extends ENASubmittable<AssayData> {
    public ENARun(AssayData assayData) throws IllegalAccessException {
        super(assayData);
    }

    public ENARun(BaseSubmittable<AssayData> baseSubmittable) throws IllegalAccessException {
        super(new AssayData());
    }

    public ENARun () throws IllegalAccessException {
        super(new AssayData());
    }

    public AssayRef getAssayRef () {
        return getBaseObject().getAssayRef();
    }

    public void setAssayRef (AssayRef assayRef) {
        getBaseObject().setAssayRef(assayRef);
    }

    public List<File> getFiles () {
        return getBaseObject().getFiles();
    }

    public void setFiles (List<File> files) {
        getBaseObject().setFiles(files);
    }
}
