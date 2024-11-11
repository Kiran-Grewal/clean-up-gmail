import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpiredCouponFetcher {
    private Gmail service;
    private EmailTrasher emailTrasher;
    private static final String user = "me";

    private final List<String> labelIds = new ArrayList<>(); //to filter promotional emails
    private String nextPageToken;
    private boolean hasMoreEmails;

    public ExpiredCouponFetcher(Gmail service,EmailTrasher emailTrasher) {
        this.service = service;
        this.emailTrasher = emailTrasher;
        labelIds.add("CATEGORY_PROMOTIONS"); //to filter promotional emails
    }

    public void trashExpiredEmails() throws IOException{
        ListMessagesResponse msgList = getPromotionalEmails();  //list of first 500 promotional mails
        int expiredEmailCount = 0;

        do {
            if(hasMoreEmails){                                  //hasMoreEmails is false for the first run
                msgList = getMorePromotionalEmails();           //get next 500 promotional emails with page token
            }
            if (msgList.getMessages() != null) {                //if there are any messages
                String emailBody = "";
                boolean isExpired;
                for (Message msg : msgList.getMessages()) {     //for every msg get emailBody
//                    Message msg = msgList.getMessages().get(337);
                    String msgId = msg.getId();
                    emailBody = getEmailBody(msgId);            //returns emailBody for the msgId
                    isExpired = checkExpiry(emailBody);
                    if(isExpired){
                        emailTrasher.trashEmails(msgId);
                        expiredEmailCount++;
                    }
                }
            }
            hasMoreEmails = (nextPageToken != null);
        }
        while(hasMoreEmails);

        System.out.println(expiredEmailCount + " emails moved to the trash folder.");
    }

    private String getEmailBody(String msgId) throws IOException {              //returns emailBody for the msgId
        Message msg = service.users().messages().get(user, msgId).execute();    //proper message object with all details
        MessagePart msgPayload = msg.getPayload();                  //parsed email structure
        String mimeType = msgPayload.getMimeType();                 //the mime type
        String msgBody = "";                                        // the email text body as base64URL encoded String
        if (mimeType.equals("multipart/alternative") || mimeType.equals("multipart/mixed")) {
            List<MessagePart> parts = msgPayload.getParts();        //List of Parts
            boolean found = false;                                  //is "Text/plain" part found
            int i = 0;
            while (!found && i < parts.size()) {
                if (parts.get(i).getMimeType().equals("text/plain")) {
                    found = true;
                    msgBody = "";     // the email text body as base64URL encoded String
                }
                else if (parts.get(i).getMimeType().equals("multipart/alternative")) {
                    //if one part is "multipart/alternative" in itself then it would probably have "text/plain" part inside it
                    int j = 0;
                    List<MessagePart> partsOfParts = parts.get(i).getParts();
                    while (!found && j < partsOfParts.size()) {
                        if (partsOfParts.get(j).getMimeType().equals("text/plain")) {
                            found = true;
                            msgBody = "";
                        }
                        j++;
                    }
                }
                i++;
            }
        }
        else if(mimeType.equals("text/plain")){
            msgBody = "";
        }
        else if(mimeType.equals("text/html")){                                  //if no "text/plain" is there
            msgBody = msgPayload.getBody().getData();
        }
        byte[] decodedBodyBytes = Base64.getUrlDecoder().decode(msgBody);       //decoding msgBody to Bytes
        String actualBody = new String(decodedBodyBytes);                       //converting bytes to String
        return actualBody;
    }

    private boolean checkExpiry(String emailBody) {
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
        if(isExpired) System.out.println(expiryDate);
        return isExpired;
    }

    private ListMessagesResponse getPromotionalEmails() throws IOException { //returns first 500 promotional emails
        ListMessagesResponse msgList = service.users().messages().list(user)
                                                .setLabelIds(labelIds)
                                                .setMaxResults(500L)
                                                .execute(); //get emails with the above criteria

        nextPageToken = msgList.getNextPageToken(); //get next page token

        return msgList;
    }

    private ListMessagesResponse getMorePromotionalEmails() throws IOException { //returns emails with next page token
        ListMessagesResponse msgList = service.users().messages().list(user)
                                                .setLabelIds(labelIds)
                                                .setPageToken(nextPageToken)
                                                .setMaxResults(500L)
                                                .execute(); //get emails with the above criteria

        nextPageToken = msgList.getNextPageToken(); //get next page token

        return msgList;
    }

}