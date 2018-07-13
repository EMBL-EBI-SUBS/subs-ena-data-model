package uk.ac.ebi.subs.ena.action;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.ena.EnaAgentApplication;
import uk.ac.ebi.subs.ena.helper.TestHelper;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class SequenceVariationAnalysisActionServiceTest {

    @Autowired
    private SequenceVariationAnalysisActionService actionService;

    private final static String ANALYSIS_RESOURCE = "/uk/ac/ebi/subs/ena/submittable/seq_var.analysis.json";

    final uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION action = uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION.Factory.newInstance();

    @Test
    public void testCreateSubmittableXML() throws Exception {
        String alias = UUID.randomUUID().toString();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        final Analysis analysis= TestHelper.getAnalysisFromResource(ANALYSIS_RESOURCE);
        Analysis [] analyses = new Analysis[]{analysis};
        final SubmissionType.ACTIONS.ACTION actionXML = actionService.createActionXML(analyses);
        assertThat(actionXML.isSetADD(), is(true));
    }

    @Test
    public void testCreateSubmittableXMLForModify() throws Exception {
        String alias = UUID.randomUUID().toString();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        final Analysis analysis= TestHelper.getAnalysisFromResource(ANALYSIS_RESOURCE);
        analysis.setAccession(UUID.randomUUID().toString());
        Analysis [] analyses = new Analysis[]{analysis};
        final SubmissionType.ACTIONS.ACTION actionXML = actionService.createActionXML(analyses);
        assertThat(actionXML.isSetMODIFY(), is(true));
    }
}
