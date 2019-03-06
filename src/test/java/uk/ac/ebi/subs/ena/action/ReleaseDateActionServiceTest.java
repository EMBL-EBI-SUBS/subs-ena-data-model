package uk.ac.ebi.subs.ena.action;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.ena.EnaAgentApplication;
import uk.ac.ebi.subs.ena.helper.DateConverter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EnaAgentApplication.class})
public class ReleaseDateActionServiceTest {
    @Autowired
    ReleaseDateActionService releaseDateActionService;

    @Test
    public void testCreateValidateActionXML() throws Exception {
        final String releaseDate = "2019-03-06";
        final SubmissionType.ACTIONS.ACTION actionXML = releaseDateActionService.createActionXML(releaseDate);

        final SubmissionType.ACTIONS.ACTION.HOLD hold = actionXML.getHOLD();

        assertThat(hold.getHoldUntilDate().compareTo(DateConverter.convertDateStr(releaseDate)), is(equalTo(0)));
    }


}