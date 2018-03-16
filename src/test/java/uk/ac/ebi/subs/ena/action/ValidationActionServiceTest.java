package uk.ac.ebi.subs.ena.action;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.ena.EnaAgentApplication;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class ValidationActionServiceTest {
    @Autowired
    ValidationActionService validationActionService;

    @Test
    public void testCreateValidateActionXML() throws Exception {
        final SubmissionType.ACTIONS.ACTION actionXML = validationActionService.createActionXML(true);
        assertThat(actionXML.isSetVALIDATE(), is(true));
    }

    @Test
    public void testCreateNoValidateActionXML() throws Exception {
        final SubmissionType.ACTIONS.ACTION actionXML = validationActionService.createActionXML(false);
        assertThat(actionXML, is(nullValue()));
    }

}