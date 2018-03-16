package uk.ac.ebi.subs.ena.action;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
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
public class AssayDataActionServiceTest {

    @Autowired
    AssayDataActionService assayDataActionService;

    final uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION action = uk.ac.ebi.ena.sra.xml.SubmissionType.ACTIONS.ACTION.Factory.newInstance();

    @Test
    public void testCreateSubmittableXML() throws Exception {
        String alias = UUID.randomUUID().toString();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        final AssayData assayData = TestHelper.getAssayData(alias, team,"alias");
        AssayData [] assayDatas = new AssayData[]{assayData};
        final SubmissionType.ACTIONS.ACTION actionXML = assayDataActionService.createActionXML(assayDatas);
        assertThat(actionXML.isSetADD(), is(true));
    }

    @Test
    public void testCreateSubmittableXMLForModify() throws Exception {
        String alias = UUID.randomUUID().toString();
        Team team = new Team();
        team.setName(UUID.randomUUID().toString());
        final AssayData assayData = TestHelper.getAssayData(alias, team,"alias");
        assayData.setAccession(UUID.randomUUID().toString());
        AssayData [] assayDatas = new AssayData[]{assayData};
        final SubmissionType.ACTIONS.ACTION actionXML = assayDataActionService.createActionXML(assayDatas);
        assertThat(actionXML.isSetMODIFY(), is(true));
    }

}