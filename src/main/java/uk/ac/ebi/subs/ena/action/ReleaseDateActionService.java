package uk.ac.ebi.subs.ena.action;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ena.sra.xml.SubmissionType;

import java.text.ParseException;
import java.util.Calendar;

import static uk.ac.ebi.subs.ena.helper.DateConverter.convertDateStr;

@Service
@Slf4j
public class ReleaseDateActionService implements ActionService<String> {

    public SubmissionType.ACTIONS.ACTION createActionXML(String releaseDate) {
        Calendar holdUntilDate;

        try {
            holdUntilDate = convertDateStr(releaseDate);
        } catch (ParseException e) {
            log.warn("Date conversion has failed for this date: {}. A release date can not be set. It will be set by the archive(s) to their default value.", releaseDate);
            return null;
        }

        final SubmissionType.ACTIONS.ACTION action = SubmissionType.ACTIONS.ACTION.Factory.newInstance();

        final SubmissionType.ACTIONS.ACTION.HOLD holdAction = SubmissionType.ACTIONS.ACTION.HOLD.Factory.newInstance();

        holdAction.setHoldUntilDate(holdUntilDate);
        action.setHOLD(holdAction);

        return action;
    }

    @Override
    public Class<String> getSubmittableClass() {
        return String.class;
    }

}
