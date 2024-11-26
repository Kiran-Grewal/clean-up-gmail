import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailExpiryChecker {
    private static final String shortMonths = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
    private static final String longMonths = "(January|February|March|April|May|June|July|August|September|October|November|December)";
    private static final String daySyntax = "((0*[1-9])|([1-2][0-9])|(3[0-1]))";
    private static final String monthSyntax = "((0*[1-9])|(1[0-2]))";
    private static final String yearSyntax = "((20[0-9]{2})|\\d{2})";
    private static final String daySuffixes = "(st|nd|rd|th)";

    
    private static final String mdyDateFormat = monthSyntax + "/" + daySyntax + "/" + yearSyntax;
    private static final String dmyDateFormat = daySyntax + "/" + monthSyntax + "/" + yearSyntax;
    private static final String shortMonthDateFormat = shortMonths + "\\s+" +daySyntax +",\\s"+yearSyntax;
    private static final String longMonthDateFormat = longMonths + "\\s" + daySyntax+ ",\\s" +yearSyntax;
    private static final String shortMonthDayDateFormat =  shortMonths + "\\s+" +daySyntax +daySuffixes;
    private static final String longMonthDayDateFormat =  longMonths + "\\s+" +daySyntax +daySuffixes;

    //Format to check if a date is missing a year
    private static final String endsWithYearFormat =  ".*" + yearSyntax + "$";

    //Hashmap for different datePatterns and their corresponding dateFormatters
    private static final HashMap<Pattern,DateTimeFormatter> datePatterns = new HashMap<>();

    //keywords to look for in an email
    private static final String findKeywords = "offer|coupon|valid|expire|promotion";
    //keywords when found ignore the email
    private static final String ignoreKeywords = "valid as of"; //

    public EmailExpiryChecker() {
        datePatterns.put(Pattern.compile(dmyDateFormat),DateTimeFormatter.ofPattern("d/M/y"));
        datePatterns.put(Pattern.compile(mdyDateFormat),DateTimeFormatter.ofPattern("M/d/y"));
        datePatterns.put(Pattern.compile(shortMonthDateFormat),DateTimeFormatter.ofPattern("MMM d, y"));
        datePatterns.put(Pattern.compile(longMonthDateFormat),DateTimeFormatter.ofPattern("MMMM d, y"));
        datePatterns.put(Pattern.compile(shortMonthDayDateFormat),DateTimeFormatter.ofPattern("MMM d, y"));
        datePatterns.put(Pattern.compile(longMonthDayDateFormat),DateTimeFormatter.ofPattern("MMMM d, y"));
    }

    public boolean checkExpiry(String emailBody){
        //Pattern for findKeywords - keywords to find in emails
        Pattern findKeywordsPattern = Pattern.compile(findKeywords, Pattern.CASE_INSENSITIVE);
        Matcher findKeywordsMatcher = findKeywordsPattern.matcher(emailBody);
        //Pattern for ignoreKeywords - keywords to ignore emails
        Pattern ignorekeywordsPattern = Pattern.compile(ignoreKeywords, Pattern.CASE_INSENSITIVE);
        Matcher ignoreKeywordsMatcher = ignorekeywordsPattern.matcher(emailBody);
        LocalDate expiryDate = null;
        LocalDate today = LocalDate.now();                      //today's date
        boolean isExpired;                                      //is true if an email has expired coupon/offer

        //if a findKeyword is found in an email and ignoreKeyword is not found
        if(findKeywordsMatcher.find() && !ignoreKeywordsMatcher.find()){
            Pattern datePattern = findDateFormat(emailBody);    //returns the dateFormat found in the email

            if(datePattern != null){
                Matcher dateMatcher = datePattern.matcher(emailBody);
                //setting the corresponding formatter to datePattern
                DateTimeFormatter formatter = datePatterns.get(datePattern);

                while (dateMatcher.find()) {                    //while the matcher finds a date in email
                    LocalDate checkDate;
                    String StringDate = dateMatcher.group().replaceAll("\\s+"," ");

                    //Pattern to check if the date is missing a year
                    Pattern endsWithYearPattern = Pattern.compile(endsWithYearFormat);
                    //setting the corresponding matcher to endsWithYearPattern
                    Matcher endsWithYearMatcher = endsWithYearPattern.matcher(StringDate);

                    if(!endsWithYearMatcher.find()){            //if the date doesn't end with a year
                        //remove any daySuffixes from StringDate
                        StringDate = dateMatcher.group().replaceAll(daySuffixes,"");
                        //add current year to the StringDate to parse it
                        StringDate = StringDate + ", "+ LocalDate.now().getYear();
                    }

                    try{
                        checkDate = LocalDate.parse(StringDate, formatter);    //change the string to date format
                        //if no other date is found before or if this date is after the date found before
                        if (expiryDate == null || checkDate.isAfter(expiryDate)) {
                            expiryDate = checkDate;
                        }
                    } catch (Exception exp) {
                        System.out.println("Unable to format the date. Error: " + exp.getMessage());
                        return false;
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
}