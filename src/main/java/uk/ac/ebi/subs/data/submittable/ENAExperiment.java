package uk.ac.ebi.subs.data.submittable;

import uk.ac.ebi.subs.data.component.*;
import uk.ac.ebi.subs.ena.annotation.*;

/**
 * Created by neilg on 28/03/2017.
 */
@ENAValidation(
        value = {
                @ENAFieldAttribute(attributeName = ENAExperiment.PLATFORM_TYPE, required = true),
                @ENAFieldAttribute(attributeName = ENAExperiment.INSTRUMENT_MODEL, attributeFieldName = "platform_type", required = true),
                @ENAFieldAttribute(attributeName = ENAExperiment.DESIGN_DESCRIPTION, required = true),
                @ENAFieldAttribute(attributeName = ENAExperiment.LIBRARY_NAME, required = true),
                @ENAFieldAttribute(attributeName = ENAExperiment.LIBRARY_STRATEGY, required = true),
                @ENAFieldAttribute(attributeName = ENAExperiment.LIBRARY_SOURCE, required = true),
                @ENAFieldAttribute(attributeName = ENAExperiment.LIBRARY_SELECTION, required = true),
                @ENAFieldAttribute(attributeName = ENAExperiment.LIBRARY_LAYOUT, required = true),
                @ENAFieldAttribute(attributeName = ENAExperiment.PAIRED_NOMINAL_LENGTH),
                @ENAFieldAttribute(attributeName = ENAExperiment.PAIRED_NOMINAL_SDEV)
        },
        enaControlledValueAttributes = {
                @ENAControlledValueAttribute(
                        attributeName = "platform_type",
                        allowedValues = {"LS454","ILLUMINA","HELICOS","ABI_SOLID",
                                "COMPLETE_GENOMICS","BGISEQ","OXFORD_NANOPORE",
                                "PACBIO_SMRT","ION_TORRENT","CAPILLARY"}),
        }
)
public class    ENAExperiment extends AbstractENASubmittable<Assay> {
    public static final String DESIGN_DESCRIPTION = "design_description";
    public static final String LIBRARY_NAME = "library_name";
    public static final String LIBRARY_STRATEGY = "library_strategy";
    public static final String LIBRARY_SOURCE = "library_source";
    public static final String LIBRARY_SELECTION = "library_selection";

    public static final String PLATFORM_TYPE = "platform_type";
    public static final String INSTRUMENT_MODEL = "instrument_model";

    public static final String LIBRARY_LAYOUT = "library_layout";
    public static final String PAIRED_NOMINAL_LENGTH = "paired_nominal_length";
    public static final String PAIRED_NOMINAL_SDEV = "paired_nominal_sdev";

    public static final String SINGLE = "SINGLE";
    public static final String PAIRED = "PAIRED";
    public static final String LS454 = "LS454";
    public static final String ILLUMINA = "ILLUMINA";
    public static final String HELICOS = "HELICOS";
    public static final String ABI_SOLID = "ABI_SOLID";
    public static final String COMPLETE_GENOMICS = "COMPLETE_GENOMICS";
    public static final String BGISEQ = "BGISEQ";
    public static final String OXFORD_NANOPORE = "OXFORD_NANOPORE";
    public static final String PACBIO_SMRT = "PACBIO_SMRT";
    public static final String ION_TORRENT = "ION_TORRENT";
    public static final String CAPILLARY = "CAPILLARY";



    @ENAField(fieldName = LS454, attributeName = INSTRUMENT_MODEL ,values = {"454 GS 20", "454 GS FLX", "454 GS FLX+", "454 GS FLX Titanium", "454 GS Junior", "unspecified"})
    String ls454 ;

    @ENAField(fieldName = ILLUMINA, attributeName = INSTRUMENT_MODEL ,values = {"Illumina Genome Analyzer", "Illumina Genome Analyzer II", "Illumina Genome Analyzer IIx",
            "Illumina HiSeq 2500", "Illumina HiSeq 2000", "Illumina HiSeq 1500", "Illumina HiSeq 1000", "Illumina MiSeq", "Illumina HiScanSQ",
            "HiSeq X Ten", "NextSeq 500", "HiSeq X Five", "Illumina HiSeq 3000", "Illumina HiSeq 4000", "NextSeq 550", "unspecified"})
    String illumina ;

    @ENAField(fieldName = HELICOS, attributeName = INSTRUMENT_MODEL ,values = {"Helicos HeliScope", "unspecified"})
    String helicos;

    @ENAField(fieldName = ABI_SOLID, attributeName = INSTRUMENT_MODEL ,values = {"AB SOLiD System 2.0", "AB SOLiD System 3.0", "AB SOLiD 3 Plus System",
            "AB SOLiD 4 System", "AB SOLiD 4hq System", "AB SOLiD PI System", "AB 5500 Genetic Analyzer", "AB 5500xl Genetic Analyzer",
            "AB 5500xl-W Genetic Analysis System", "unspecified"})
    String abiSolid ;

    @ENAField(fieldName = COMPLETE_GENOMICS, attributeName = INSTRUMENT_MODEL ,values = {"Complete Genomics", "unspecified"})
    String completeGenomics;

    @ENAField(fieldName = BGISEQ, attributeName = INSTRUMENT_MODEL ,values = {"BGISEQ-500"})
    String bgiseq;

    @ENAField(fieldName = OXFORD_NANOPORE, attributeName = INSTRUMENT_MODEL ,values = {"MinION", "GridION", "unspecified"})
    String oxfordNanopore;

    @ENAField(fieldName = PACBIO_SMRT, attributeName = INSTRUMENT_MODEL ,values = {"PacBio RS", "PacBio RS II", "Sequel", "unspecified"})
    String pacbioSMRT;

    @ENAField(fieldName = ION_TORRENT, attributeName = INSTRUMENT_MODEL ,values = {"Ion Torrent PGM", "Ion Torrent Proton", "unspecified"})
    String ionTorrent;

    @ENAField(fieldName = CAPILLARY, attributeName = INSTRUMENT_MODEL ,values = {"AB 3730xL Genetic Analyzer", "AB 3730 Genetic Analyzer", "AB 3500xL Genetic Analyzer",
            "AB 3500 Genetic Analyzer", "AB 3130xL Genetic Analyzer", "AB 3130 Genetic Analyzer", "AB 3130 Genetic Analyzer", "AB 310 Genetic Analyzer",
            "unspecified"})
    String capillary;

    @ENAField(fieldName = DESIGN_DESCRIPTION)
    String designDescription;

    @ENAField(fieldName = LIBRARY_NAME)
    String libraryName;

    @ENAField(fieldName = LIBRARY_STRATEGY)
    String libraryStrategy;

    @ENAField(fieldName = LIBRARY_SOURCE)
    String librarySource;

    @ENAField(fieldName = LIBRARY_SELECTION)
    String librarySelection;

    @ENAField(fieldName = LIBRARY_LAYOUT)
    String libraryLayout;

    @ENAField(fieldName = PAIRED_NOMINAL_LENGTH)
    String nominalLength = null;

    @ENAField(fieldName = PAIRED_NOMINAL_SDEV)
    String nominalSdev = null;

    String singleLibraryLayout;
    PairedLibraryLayout pairedLibraryLayout;

    public ENAExperiment(Assay assay) throws IllegalAccessException {
        super(assay);
    }

    public ENAExperiment() throws IllegalAccessException {
        super();
    }

    @Override
    public void serialiseAttributes() throws IllegalAccessException {
        super.serialiseAttributes();
        serialiseLibraryLayout();
    }

    public void deSerialiseAttributes () throws IllegalAccessException {
        deserialiseLibraryLayout();
        super.deSerialiseAttributes();
        final ENAFieldAttribute instrumentModel = getInstrumentModel();
    }

    public void serialiseLibraryLayout() throws IllegalAccessException {
        if (libraryLayout == null)
            libraryLayout = SINGLE;
        else if (libraryLayout.equals(PAIRED)) {
            this.pairedLibraryLayout = new PairedLibraryLayout(nominalLength, nominalSdev);
            this.singleLibraryLayout = null;
        } else if (libraryLayout.equals(SINGLE)) {
            this.singleLibraryLayout = "";
            this.pairedLibraryLayout = null;
        }
    }

    public void deserialiseLibraryLayout() throws IllegalAccessException {
        if (pairedLibraryLayout != null) {
            nominalLength = pairedLibraryLayout.getNominalLength();
            nominalSdev = pairedLibraryLayout.getNominalSdev();
            libraryLayout = PAIRED;
        } else {
            libraryLayout = SINGLE;
        }
    }

    public SampleRef getSampleRef () {
        Assay assay = getBaseObject();
        if (assay.getSampleUses().isEmpty())
            return null;
        else {
            final SampleRef sampleRef = assay.getSampleUses().get(0).getSampleRef();
            if (sampleRef.getTeam() == null) {
                sampleRef.setTeam(getTeamName());
            }
            sampleRef.setAlias(getENAAlias(sampleRef.getAlias(),sampleRef.getTeam()));
            return sampleRef;
        }
    }

    public void setSampleRef (SampleRef sampleRef) {
        sampleRef.setAlias(removeENAAlias(sampleRef.getAlias()));
        SampleUse sampleUse = new SampleUse(sampleRef);
        getBaseObject().getSampleUses().add(sampleUse);
    }

    @Override
    public Submittable createNewSubmittable() {
        return new Assay();
    }

    public static class Single {}

    public static class PairedLibraryLayout {
        String nominalLength = null;
        String nominalSdev = null;

        public PairedLibraryLayout(String nominalLength, String nominalSdev) {
            this.nominalLength = nominalLength;
            this.nominalSdev = nominalSdev;
        }

        public PairedLibraryLayout () {}

        public String getNominalLength() {
            return nominalLength;
        }

        public String getNominalSdev() {
            return nominalSdev;
        }
    }


    public StudyRef getStudyRef() {
        final StudyRef studyRef = getBaseObject().getStudyRef();
        if (studyRef.getTeam() == null) {
            studyRef.setTeam(getTeamName());
        }
        studyRef.setAlias(getENAAlias(studyRef.getAlias(),studyRef.getTeam()));
        return studyRef;

    }

    public void setStudyRef(StudyRef studyRef) {
        studyRef.setAlias(removeENAAlias(studyRef.getAlias()));
        getBaseObject().setStudyRef(studyRef);
    }

    public void setIllumina(String illumina) {
        this.illumina = illumina;
    }

    public void setDesignDescription(String designDescription) {
        this.designDescription = designDescription;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public void setLibraryStrategy(String libraryStrategy) {
        this.libraryStrategy = libraryStrategy;
    }

    public String getLibrarySource() {
        return librarySource;
    }

    public void setLibrarySource(String librarySource) {
        this.librarySource = librarySource;
    }

    public void setLibrarySelection(String librarySelection) {
        this.librarySelection = librarySelection;
    }

    public void setLibraryLayout(String libraryLayout) {
        this.libraryLayout = libraryLayout;
    }

    public ENAFieldAttribute getInstrumentModel () {
        final ENAValidation enaValidation = getEnaValidation();
        for (ENAFieldAttribute enaFieldAttribute : enaValidation.value()) {
            if (enaFieldAttribute.attributeName().equals("instrument_model")) {
                return enaFieldAttribute;
            }
        }
        return null;
    }

}
