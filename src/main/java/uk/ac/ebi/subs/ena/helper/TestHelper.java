package uk.ac.ebi.subs.ena.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.ac.ebi.ena.sra.xml.EXPERIMENTSETDocument;
import uk.ac.ebi.ena.sra.xml.ExperimentType;
import uk.ac.ebi.ena.sra.xml.LibraryDescriptorType;
import uk.ac.ebi.ena.sra.xml.LibraryType;
import uk.ac.ebi.ena.sra.xml.RUNSETDocument;
import uk.ac.ebi.ena.sra.xml.RunType;
import uk.ac.ebi.ena.sra.xml.SAMPLESETDocument;
import uk.ac.ebi.ena.sra.xml.STUDYSETDocument;
import uk.ac.ebi.ena.sra.xml.SampleDescriptorType;
import uk.ac.ebi.ena.sra.xml.SampleType;
import uk.ac.ebi.ena.sra.xml.StudyType;
import uk.ac.ebi.ena.sra.xml.TypeIlluminaModel;
import uk.ac.ebi.ena.sra.xml.TypeLibrarySelection;
import uk.ac.ebi.ena.sra.xml.TypeLibrarySource;
import uk.ac.ebi.ena.sra.xml.TypeLibraryStrategy;
import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.File;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.SampleUse;
import uk.ac.ebi.subs.data.component.StudyRef;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.data.submittable.ENAExperiment;
import uk.ac.ebi.subs.data.submittable.ENARun;
import uk.ac.ebi.subs.data.submittable.ENASample;
import uk.ac.ebi.subs.data.submittable.ENASequenceVariationAnalysis;
import uk.ac.ebi.subs.data.submittable.ENAStudy;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.data.submittable.Submittable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by neilg on 18/05/2017.
 */
public class TestHelper {

    static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static Study getStudyFromResource(String studyResource) throws IOException {
        final InputStream inputStream = ObjectMapper.class.getResourceAsStream(studyResource);
        final Study study = objectMapper.readValue(inputStream, Study.class);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        final UUID uuid = UUID.randomUUID();
        study.setId(uuid.toString());
        return study;

    }

    public static Assay getAssayFromResource(String assayResource) throws IOException {
        final InputStream inputStream = ObjectMapper.class.getResourceAsStream(assayResource);
        final Assay assay = objectMapper.readValue(inputStream, Assay.class);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        final UUID uuid = UUID.randomUUID();
        assay.setId(uuid.toString());
        return assay;

    }

    public static Analysis getAnalysisFromResource(String analysisResource) throws IOException {
        final InputStream inputStream = ObjectMapper.class.getResourceAsStream(analysisResource);
        final Analysis analysis = objectMapper.readValue(inputStream, Analysis.class);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        final UUID uuid = UUID.randomUUID();
        analysis.setId(uuid.toString());
        return analysis;

    }

    static Sample getSampleFromResource (String sampleResource) throws IOException {
        final InputStream inputStream = ObjectMapper.class.getResourceAsStream(sampleResource);
        final Sample sample = objectMapper.readValue(inputStream, Sample.class);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        final UUID uuid = UUID.randomUUID();
        sample.setId(uuid.toString());
        return sample;

    }

    static AssayData getAssayDataFromResource (String assayDataResource) throws IOException {
        final InputStream inputStream = ObjectMapper.class.getResourceAsStream(assayDataResource);
        final AssayData assayData = objectMapper.readValue(inputStream, AssayData.class);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        final UUID uuid = UUID.randomUUID();
        assayData.setId(uuid.toString());
        return assayData;

    }

    public static STUDYSETDocument getStudysetDocument(String alias, String centerName) {
        STUDYSETDocument studysetDocument = STUDYSETDocument.Factory.newInstance();
        final StudyType studyType = studysetDocument.addNewSTUDYSET().addNewSTUDY();
        studyType.setAlias(alias);
        studyType.setCenterName(centerName);
        final StudyType.DESCRIPTOR descriptor = studyType.addNewDESCRIPTOR();
        descriptor.setSTUDYABSTRACT("Study Abstract");
        descriptor.setSTUDYTITLE("Study Title");
        descriptor.setSTUDYDESCRIPTION("Study Description");
        descriptor.addNewSTUDYTYPE().setExistingStudyType(StudyType.DESCRIPTOR.STUDYTYPE.ExistingStudyType.WHOLE_GENOME_SEQUENCING);
        return studysetDocument;
    }

    public static SAMPLESETDocument getSamplesetDocument(String alias, String centerName) {
        SAMPLESETDocument samplesetDocument = SAMPLESETDocument.Factory.newInstance();
        final SampleType sampleType = samplesetDocument.addNewSAMPLESET().addNewSAMPLE();
        sampleType.setAlias(alias);
        sampleType.setCenterName(centerName);
        final SampleType.SAMPLENAME samplename = sampleType.addNewSAMPLENAME();
        samplename.setTAXONID(9606);
        return samplesetDocument;
    }

    public static EXPERIMENTSETDocument getExperimentSetDocument(String alias, String studyAlias, String sampleAlias, String centerName) {
        EXPERIMENTSETDocument experimentsetDocument = EXPERIMENTSETDocument.Factory.newInstance();
        final ExperimentType experimentType = experimentsetDocument.addNewEXPERIMENTSET().addNewEXPERIMENT();
        experimentType.setAlias(alias);
        experimentType.setCenterName(centerName);
        experimentType.addNewSTUDYREF().setRefname(studyAlias);
        experimentType.addNewPLATFORM().addNewILLUMINA().setINSTRUMENTMODEL(TypeIlluminaModel.ILLUMINA_GENOME_ANALYZER);
        final LibraryType libraryType = experimentType.addNewDESIGN();
        libraryType.setDESIGNDESCRIPTION("design description");
        final LibraryDescriptorType libraryDescriptorType = libraryType.addNewLIBRARYDESCRIPTOR();
        libraryDescriptorType.addNewLIBRARYLAYOUT().addNewSINGLE();
        libraryDescriptorType.setLIBRARYSELECTION(TypeLibrarySelection.RANDOM);
        libraryDescriptorType.setLIBRARYNAME("Library Name");
        libraryDescriptorType.setLIBRARYSOURCE(TypeLibrarySource.GENOMIC);
        libraryDescriptorType.setLIBRARYSTRATEGY(TypeLibraryStrategy.WGS);
        final SampleDescriptorType sampleDescriptorType = libraryType.addNewSAMPLEDESCRIPTOR();
        sampleDescriptorType.addNewIDENTIFIERS().addNewSUBMITTERID().setStringValue(sampleAlias);
        return experimentsetDocument;
    }

    public static RUNSETDocument getRunSetDocument(String alias, String experimentAlias, String centerName, String fileName, String fileType) {
        RUNSETDocument runsetDocument = RUNSETDocument.Factory.newInstance();
        final RunType runType = runsetDocument.addNewRUNSET().addNewRUN();
        runType.setAlias(alias);
        runType.setCenterName(centerName);
        runType.addNewEXPERIMENTREF().addNewIDENTIFIERS().addNewSUBMITTERID().setStringValue(experimentAlias);
        final RunType.DATABLOCK datablock = runType.addNewDATABLOCK();
        final RunType.DATABLOCK.FILES.FILE file = datablock.addNewFILES().addNewFILE();
        file.setChecksumMethod(RunType.DATABLOCK.FILES.FILE.ChecksumMethod.MD_5);
        file.setChecksum("12345678123456781234567812345678");
        file.setFilename(fileName);
        file.setFiletype(RunType.DATABLOCK.FILES.FILE.Filetype.Enum.forString(fileType));
        return runsetDocument;
    }

    public static ENAStudy getENAStudy(String alias, Team team) throws Exception {
        ENAStudy enaStudy = new ENAStudy();
        enaStudy.setId(UUID.randomUUID().toString());
        enaStudy.setAlias(alias);
        enaStudy.setTeam(team);
        enaStudy.setStudyType("Whole Genome Sequencing");
        enaStudy.setTitle("Study Title");
        enaStudy.setStudyAbstract("Study abstract");
        return enaStudy;
    }

    public static ENASample getENASample(String alias, Team team) throws Exception {
        ENASample enaSample = new ENASample();
        enaSample.setId(UUID.randomUUID().toString());
        enaSample.setAlias(alias);
        enaSample.setTeam(team);
        enaSample.setTaxonId(9606l);
        enaSample.setTitle("Sample Title");
        enaSample.setDescription("Sample Description");
        return enaSample;
    }

    public static ENAExperiment getENAExperiment(String alias, Team team) throws Exception {
        ENAExperiment enaExperiment = new ENAExperiment();
        enaExperiment.setAlias(alias);
        enaExperiment.setTeam(team);
        enaExperiment.setId(UUID.randomUUID().toString());
        StudyRef studyRef = new StudyRef();
        studyRef.setAlias(alias);
        studyRef.setTeam(team.getName());
        enaExperiment.setStudyRef(studyRef);
        SampleRef sampleRef = new SampleRef();
        sampleRef.setAlias(alias);
        sampleRef.setTeam(team.getName());
        enaExperiment.setSampleRef(sampleRef);
        enaExperiment.setIllumina("Illumina Genome Analyzer");
        enaExperiment.setDesignDescription("Design Desc");
        enaExperiment.setLibraryName("Library name");
        enaExperiment.setLibraryLayout("SINGLE");
        enaExperiment.serialiseLibraryLayout();
        enaExperiment.setLibrarySelection("RANDOM");
        enaExperiment.setLibrarySource("GENOMIC");
        enaExperiment.setLibraryStrategy("WGS");
        return enaExperiment;
    }

    public static ENARun getENARun(String alias, Team team) throws IllegalAccessException {
        ENARun enaRun = new ENARun();
        enaRun.setAlias(alias);
        enaRun.setTeam(team);
        enaRun.setId(UUID.randomUUID().toString());

        AssayRef assayRef = new AssayRef();
        assayRef.setAlias(alias);
        assayRef.setTeam(team.getName());
        enaRun.setAssayRef(assayRef);

        List<File> files = new ArrayList<>();
        File aFile = new File();
        aFile.setChecksum("12345678901234567890123456789012");
        aFile.setChecksumMethod("MD5");
        aFile.setLabel("some label");
        aFile.setName("test_file.cram");
        aFile.setType("cram");
        aFile.setUnencryptedChecksum("unencryptedChecksum");
        files.add(aFile);

        enaRun.setFiles(files);

        return  enaRun;
    }

    public static Team getTeam (String centerName) {
        Team team = new Team();
        team.setName(centerName);
        return team;
    }

    public static Study getStudy (String alias, Team team, String studyAbstract, String studyType) {
        Study study = new Study();
        study.setId(UUID.randomUUID().toString());
        study.setAlias(alias);
        study.setTeam(team);
        study.setTitle("Study Title");
        Attribute studyAbstractAttibute = new Attribute();
        studyAbstractAttibute.setValue(studyAbstract);
        addAttribute(study,"study_abstract",studyAbstractAttibute);
        Attribute studyTypeAttribute = new Attribute();
        studyTypeAttribute.setValue(studyType);
        addAttribute(study,ENAStudy.STUDY_TYPE,studyTypeAttribute);
        return study;
    }

    public static Study getStudy (String alias, Team team) {
        return getStudy(alias,team,"study abstract","Whole Genome Sequencing");
    }

    public static Sample getSample(String alias, Team team) {
        Sample sample = new Sample();
        sample.setId(UUID.randomUUID().toString());
        sample.setAlias(alias);
        sample.setTeam(team);
        sample.setTaxonId(9606l);
        sample.setDescription("Sample description");
        sample.setTitle("Sample title");
        return sample;
    }
    public static Analysis getSeqVarAnalysis(String alias, Team team, String biosampleAccession, String studyAlias){
        Analysis a = new Analysis();
        a.setId(UUID.randomUUID().toString());
        a.setAlias(alias);
        a.setTeam(team);
        a.setTitle("Analysis Title ");
        a.setDescription("Test analysis");
        addAttribute(a,ENASequenceVariationAnalysis.ASSEMBLY_NAME_ATTRIBUTE,"GRCh38");
        addAttribute(a,ENASequenceVariationAnalysis.EXPERIMENT_TYPE_ATTRIBUTE,"Exome sequencing");
        addAttribute(a,ENASequenceVariationAnalysis.PROGRAM_ATRRIBUTE,"MiniMap");
        addAttribute(a,ENASequenceVariationAnalysis.PLATFORM_ATTRIBUTE,"Illumina HiSeq 2500");
        addAttribute(a,ENASequenceVariationAnalysis.IMPUTATION_ATTRIBUTE,"false");

        File file = new File();
        file.setType("vcf");
        file.setChecksum("2debfdcf79f03e4a65a667d21ef9de14");
        file.setName("Test.vcf.gz");
        file.setChecksumMethod("MD5");
        a.getFiles().add(file);

        SampleRef sampleRef = new SampleRef();
        sampleRef.setAccession(biosampleAccession);
        a.getSampleRefs().add(sampleRef);

        StudyRef studyRef = new StudyRef();
        studyRef.setAlias(studyAlias);
        a.getStudyRefs().add(studyRef);

        return a;
    }


    public static Assay getAssay(String alias, Team team, String biosampleAccession, String studyAlias) {
        Assay a = new Assay();
        a.setId(UUID.randomUUID().toString());
        a.setAlias(alias);
        a.setTeam(team);
        a.setTitle("Assay Title ");
        a.setDescription("Test assay");
        addAttribute(a,ENAExperiment.PLATFORM_TYPE,"ILLUMINA");
        addAttribute(a,ENAExperiment.INSTRUMENT_MODEL,"Illumina Genome Analyzer");
        addAttribute(a,ENAExperiment.LIBRARY_LAYOUT,ENAExperiment.SINGLE);
        addAttribute(a,ENAExperiment.DESIGN_DESCRIPTION,"Design Description");
        addAttribute(a,ENAExperiment.LIBRARY_NAME,"Example Library");
        addAttribute(a,ENAExperiment.LIBRARY_SELECTION,"RANDOM");
        addAttribute(a,ENAExperiment.LIBRARY_SOURCE,"GENOMIC");
        addAttribute(a,ENAExperiment.LIBRARY_STRATEGY,"WGS");

        SampleRef sampleRef = new SampleRef();
        sampleRef.setAccession(biosampleAccession);
        SampleUse sampleUse = new SampleUse(sampleRef);
        a.getSampleUses().add(sampleUse);

        StudyRef studyRef = new StudyRef();
        studyRef.setAlias(studyAlias);
        a.setStudyRef(studyRef);
        return a;
    }

    public static AssayData getAssayData (String alias, Team team, String assayAlias) {
        AssayData assayData = new AssayData();
        assayData.setId(UUID.randomUUID().toString());
        assayData.setAlias(alias);
        assayData.setTeam(team);
        assayData.setAlias(alias);
        AssayRef assayRef = new AssayRef();
        assayRef.setAlias(assayAlias);
        assayRef.setTeam(team.getName());
        assayData.setAssayRefs(Arrays.asList(assayRef));
        assayData.setTitle("Test Title");
        File file = new File();
        file.setType("fastq");
        file.setChecksum("2debfdcf79f03e4a65a667d21ef9de14");
        file.setName("Test.fastq.gz");
        file.setChecksumMethod("MD5");
        assayData.getFiles().add(file);
        return assayData;
    }

    public static void addAttribute (Submittable submittable , String name, Attribute attribute) {
        if (!submittable.getAttributes().containsKey(name))
            submittable.getAttributes().put(name,new ArrayList<Attribute>());

        submittable.getAttributes().get(name).add(attribute);
    }

    public static void addAttribute (Submittable submittable , String name, String value) {
    Attribute attribute = attribute(value);
        if (!submittable.getAttributes().containsKey(name)) {
            submittable.getAttributes().put(name,new ArrayList<Attribute>());
        }
        submittable.getAttributes().get(name).add(attribute);
    }

    public static Attribute attribute(String value) {
        Attribute attribute = new Attribute();
        attribute.setValue(value);
        return attribute;
    }

}
