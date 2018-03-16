package uk.ac.ebi.subs.ena.action;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.ena.EnaAgentApplication;
import uk.ac.ebi.subs.ena.helper.TestHelper;

import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class SampleActionServiceTest {

    @Autowired
    SampleActionService sampleActionService;

    final uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION action = uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION.Factory.newInstance();

    @Test
    public void testCreateSubmittableXML() throws Exception {
        String alias = UUID.randomUUID().toString();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        final Sample sample = TestHelper.getSample(alias, team);
        Sample [] samples = new Sample[]{sample};
        SubmissionType.ACTIONS.ACTION actionXML = sampleActionService.createActionXML(samples);
        assertThat(actionXML.isSetADD(), is(true));
    }

    @Test
    public void testCreateSubmittableXMLForModify() throws Exception {
        String alias = UUID.randomUUID().toString();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        final Sample sample = TestHelper.getSample(alias, team);
        sample.setAccession(UUID.randomUUID().toString());
        Sample [] samples = new Sample[]{sample};
        SubmissionType.ACTIONS.ACTION actionXML = sampleActionService.createActionXML(samples);
        assertThat(actionXML.isSetMODIFY(), is(true));
    }

}