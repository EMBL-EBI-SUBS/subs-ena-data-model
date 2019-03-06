package uk.ac.ebi.subs.ena.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateConverter {

    public static Calendar convertDateStr(String releaseDate) throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(releaseDate);
        cal.setTime(date);

        return cal;
    }
}
