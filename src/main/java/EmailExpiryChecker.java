import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailExpiryChecker {
    private static final String shortMonths = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
    private static final String longMonths = "(January|February|March|April|May|June|July|August|September|October|November|December)";
    private static final String daySyntax = "((0*[1-9])|([1-3][0-9]))";
    private static final String monthSyntax = "((0*[1-9])|([1-2][0-2]))";
    private static final String yearSyntax = "((20[0-9]{2})|\\d{2})";

    private static final String mdyDateFormat = monthSyntax + "/" + daySyntax + "/" + yearSyntax;
    private static final String dmyDateFormat = daySyntax + "/" + monthSyntax + "/" + yearSyntax;
    private static final String shortEngDateFormat = shortMonths + "\\s+" +daySyntax +",\\s"+yearSyntax;
    private static final String longEngDateFormat = longMonths + "\\s" + daySyntax+ ",\\s" +yearSyntax;

    //Hashmap for different datePatterns and their corresponding dateFormatters
    private static final HashMap<Pattern,DateTimeFormatter> datePatterns = new HashMap<Pattern,DateTimeFormatter>();

    private static final String keywords = "offer|coupon|valid|expire|promotion"; //keywords to look for in an email

    public EmailExpiryChecker() {
        datePatterns.put(Pattern.compile(dmyDateFormat),DateTimeFormatter.ofPattern("d/M/y"));
        datePatterns.put(Pattern.compile(mdyDateFormat),DateTimeFormatter.ofPattern("M/d/y"));
        datePatterns.put(Pattern.compile(shortEngDateFormat),DateTimeFormatter.ofPattern("MMM d, y"));
        datePatterns.put(Pattern.compile(longEngDateFormat),DateTimeFormatter.ofPattern("MMMM d, y"));
    }

    public boolean checkExpiry(String emailBody){
        Pattern keywordsPattern = Pattern.compile(keywords, Pattern.CASE_INSENSITIVE);
        Matcher keywordsMatcher = keywordsPattern.matcher(emailBody);
        LocalDate expiryDate = null;
        LocalDate today = LocalDate.now();                      //today's date
        boolean isExpired;                                      //is true if an email has expired coupon/offer

        if(keywordsMatcher.find()){                             //if a keyword is found in an email
            Pattern datePattern = findDateFormat(emailBody);    //returns the dateFormat found in the email

            if(datePattern != null){
                Matcher dateMatcher = datePattern.matcher(emailBody);
                //setting the corresponding formatter to datePattern
                DateTimeFormatter formatter = datePatterns.get(datePattern);

                while (dateMatcher.find()) {                    //while the matcher finds a date in email
                    LocalDate checkDate;
                    String StringDate = dateMatcher.group().replaceAll("\\s+"," ");
                    try{
                        checkDate = LocalDate.parse(StringDate, formatter);     //change the string to date format
                        //if no other date is found before or if this date is after the date found before
                        if (expiryDate == null || checkDate.isAfter(expiryDate)) {
                            expiryDate = checkDate;
                        }
                    } catch (Exception exp) {
                        System.out.println("Unable to format the date. Error: " + exp.getMessage());
                    }
                }
            }
        }
        if(expiryDate == null){
            isExpired = false;
        }
        else {
            isExpired = expiryDate.isBefore(today);         //if expiryDate is before today.
        }
        return isExpired;
    }

    public Pattern findDateFormat(String emailBody) {       //finds dateFormat in an email
        Pattern pattern = null;
        Matcher dateMatcher;
        for(Pattern pat: datePatterns.keySet()) {           //for every datePattern pat in datePatterns keys
            dateMatcher = pat.matcher(emailBody);
            if(dateMatcher.find()) {                        //if matcher finds a date with pattern pat
                if(pattern == null) {                       //if there is no other pattern found before
                    pattern = pat;
                }
                else {                                      //if there are more than 1 datePattern found in an email
                    return null;
                }
            }
        }
        return pattern;
    }

//    public boolean checExpiry(String emailBody) {
//        String keywords = "offer|coupon|valid|expire|promotion";
//        Pattern keywordsPattern = Pattern.compile(keywords, Pattern.CASE_INSENSITIVE);
//
//        Pattern datePattern = Pattern.compile(mdyDateFormat);
//        Matcher dateMatcher = datePattern.matcher(emailBody);
//        DateTimeFormatter formatter;
//        Matcher keywordsMatcher = keywordsPattern.matcher(emailBody);
//        String StringDate;
//        LocalDate expiryDate = LocalDate.of(2000, 1, 1);
//        LocalDate today = LocalDate.now();
//        boolean isExpired;
//
//        if (keywordsMatcher.find()) {
//            if (dateMatcher.find()) {
//                String sameDate = dateMatcher.group();
//
//                datePattern = Pattern.compile(dmyDateFormat);
//                dateMatcher = datePattern.matcher(emailBody);
//
//                if(dateMatcher.find()){
//                    dateMatcher = datePattern.matcher(sameDate);
//                    if(!dateMatcher.find()){
//                        datePattern = Pattern.compile(mdyDateFormat);
//                        dateMatcher = datePattern.matcher(emailBody);
//                        formatter = DateTimeFormatter.ofPattern("M/d/y");
//                    }
//                }
//                if(!dateMatcher.find()) {
//                    datePattern = Pattern.compile(mdyDateFormat);
//                    dateMatcher = datePattern.matcher(emailBody);
//                    formatter = DateTimeFormatter.ofPattern("M/d/y");
//                }
//                else {
//                    return false;
//                }
//            }
//            else {
//                datePattern = Pattern.compile(dmyDateFormat);
//                dateMatcher = datePattern.matcher(emailBody);
//                if (dateMatcher.find()) {
//                    formatter = DateTimeFormatter.ofPattern("d/M/y");
//                }
//                else {
//                    datePattern = Pattern.compile(shortEngDateFormat);
//                    dateMatcher = datePattern.matcher(emailBody);
//                    if (dateMatcher.find()) {
//                        formatter = DateTimeFormatter.ofPattern("MMM d, y");
//                    }
//                    else {
//                        datePattern = Pattern.compile(longEngDateFormat);
//                        dateMatcher = datePattern.matcher(emailBody);
//                        formatter = DateTimeFormatter.ofPattern("MMMM d, y");
//                    }
//                }
//            }
//            while (dateMatcher.find()) {
//                StringDate = dateMatcher.group().replaceAll("\\s+"," ");
//                LocalDate checkDate = LocalDate.parse(StringDate, formatter);
//                if (checkDate.isAfter(expiryDate)) {
//                    expiryDate = checkDate;
//                }
//            }
//        }
//        isExpired = expiryDate.isBefore(today);
//        if(expiryDate.equals(LocalDate.of(2000, 1, 1))){
//            isExpired = false;
//        }
//        return isExpired;
//    }
}
