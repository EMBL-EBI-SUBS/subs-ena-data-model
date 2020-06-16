package uk.ac.ebi.subs.ena.action;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.ena.EnaAgentApplication;
import uk.ac.ebi.subs.ena.helper.TestHelper;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class AssayActionServiceTest {

    public static final String ILLUMINA_GENOME_ANALYZER_INSTRUMENT_MODELL = "Illumina Genome Analyzer";

    @Autowired
    AssayActionService assayActionService;

    final uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION action = uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION.Factory.newInstance();

    @Test
    public void testCreateSubmittableXML() throws Exception {
        String alias = UUID.randomUUID().toString();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        final Assay assay = TestHelper.getAssay(alias, team,"SAMEA4862012","alias",
                ILLUMINA_GENOME_ANALYZER_INSTRUMENT_MODELL);
        Assay [] assays = new Assay[]{assay};
        final SubmissionType.ACTIONS.ACTION actionXML = assayActionService.createActionXML(assays);
        assertThat(actionXML.isSetADD(), is(true));
    }

    @Test
    public void testCreateSubmittableXMLForModify() throws Exception {
        String alias = UUID.randomUUID().toString();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        final Assay assay = TestHelper.getAssay(alias, team,"SAMEA4862012","alias",
                ILLUMINA_GENOME_ANALYZER_INSTRUMENT_MODELL);
        assay.setAccession(UUID.randomUUID().toString());
        Assay [] assays = new Assay[]{assay};
        final SubmissionType.ACTIONS.ACTION actionXML = assayActionService.createActionXML(assays);
        assertThat(actionXML.isSetMODIFY(), is(true));
    }

}