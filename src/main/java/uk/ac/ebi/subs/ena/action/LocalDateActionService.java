package uk.ac.ebi.subs.ena.action;

import org.springframework.stereotype.Service;
import uk.ac.ebi.ena.sra.xml.SubmissionType;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

@Service
public class LocalDateActionService implements ActionService<LocalDate> {

    @Override
    public SubmissionType.ACTIONS.ACTION createActionXML(LocalDate localDate) {
        final SubmissionType.ACTIONS.ACTION action = SubmissionType.ACTIONS.ACTION.Factory.newInstance();
        final SubmissionType.ACTIONS.ACTION.HOLD holdAction = SubmissionType.ACTIONS.ACTION.HOLD.Factory.newInstance();
        Calendar calendar = Calendar.getInstance();

        if (localDate != null) {
            calendar.setTime(
                    Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            holdAction.setHoldUntilDate(calendar);
            action.setHOLD(holdAction);
            return action;
        } else {
            return null;
        }
    }

    @Override
    public Class<LocalDate> getSubmittableClass() {
        return LocalDate.class;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
