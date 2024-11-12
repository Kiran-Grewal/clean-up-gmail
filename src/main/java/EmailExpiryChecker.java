import com.google.api.services.gmail.Gmail;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailExpiryChecker {
    private Gmail service;
    private static final String user = "me";

    public EmailExpiryChecker(Gmail service) {
        this.service = service;
    }

    public boolean checkExpiry(String emailBody) {
        String keywords = "offer|coupon|valid|expire|promotion";
        Pattern keywordsPattern = Pattern.compile(keywords, Pattern.CASE_INSENSITIVE);
        String shortMonths = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
        String longMonths = "(January|February|March|April|May|June|July|August|September|October|November|December)";
        String daySyntax = "((0*[1-9])|([1-3][0-9]))";
        String monthSyntax = "((0*[1-9])|([1-2][0-2]))";
        String yearSyntax = "((20[0-9]{2})|\\d{2})";
        String mdyDateFormat = monthSyntax + "/" + daySyntax + "/" + yearSyntax;
        String dmyDateFormat = daySyntax + "/" + monthSyntax + "/" + yearSyntax;
        String shortEngDateFormat = shortMonths + "\\s+" +daySyntax +",\\s"+yearSyntax;
        String longEngDateFormat = longMonths + "\\s" + daySyntax+ ",\\s" +yearSyntax;
        Pattern datePattern = Pattern.compile(mdyDateFormat);
        Matcher dateMatcher = datePattern.matcher(emailBody);
        DateTimeFormatter formatter;
        Matcher keywordsMatcher = keywordsPattern.matcher(emailBody);
        String StringDate;
        LocalDate expiryDate = LocalDate.of(2000, 1, 1);
        LocalDate today = LocalDate.now();
        boolean isExpired;

        if (keywordsMatcher.find()) {
            if (dateMatcher.find()) {
//                String sameDate = dateMatcher.group();

                datePattern = Pattern.compile(dmyDateFormat);
                dateMatcher = datePattern.matcher(emailBody);

//                if(dateMatcher.find()){
//                    dateMatcher = datePattern.matcher(sameDate);
//                    if(!dateMatcher.find()){
//                        datePattern = Pattern.compile(mdyDateFormat);
//                        dateMatcher = datePattern.matcher(emailBody);
//                        formatter = DateTimeFormatter.ofPattern("M/d/y");
//                    }
//                }
                if(!dateMatcher.find()) {
                    datePattern = Pattern.compile(mdyDateFormat);
                    dateMatcher = datePattern.matcher(emailBody);
                    formatter = DateTimeFormatter.ofPattern("M/d/y");
                }
                else {
                    return false;
                }
            }
            else {
                datePattern = Pattern.compile(dmyDateFormat);
                dateMatcher = datePattern.matcher(emailBody);
                if (dateMatcher.find()) {
                    formatter = DateTimeFormatter.ofPattern("d/M/y");
                }
                else {
                    datePattern = Pattern.compile(shortEngDateFormat);
                    dateMatcher = datePattern.matcher(emailBody);
                    if (dateMatcher.find()) {
                        formatter = DateTimeFormatter.ofPattern("MMM d, y");
                    }
                    else {
                        datePattern = Pattern.compile(longEngDateFormat);
                        dateMatcher = datePattern.matcher(emailBody);
                        formatter = DateTimeFormatter.ofPattern("MMMM d, y");
                    }
                }
            }
            while (dateMatcher.find()) {
                StringDate = dateMatcher.group().replaceAll("\\s+"," ");
                LocalDate checkDate = LocalDate.parse(StringDate, formatter);
                if (checkDate.isAfter(expiryDate)) {
                    expiryDate = checkDate;
                }
            }
        }
        isExpired = expiryDate.isBefore(today);
        if(expiryDate.equals(LocalDate.of(2000, 1, 1))){
            isExpired = false;
        }
        return isExpired;
    }
}
