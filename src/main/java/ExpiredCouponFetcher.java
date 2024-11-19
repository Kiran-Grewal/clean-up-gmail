import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.util.*;

public class ExpiredCouponFetcher {
    private Gmail service;
    private EmailTrasher emailTrasher;
    private EmailBodyFetcher emailBodyFetcher;
    private EmailExpiryChecker emailExpiryChecker;
    private static final String user = "me";

    private final List<String> labelIds = new ArrayList<>(); //to filter promotional emails
    private String nextPageToken;
    private boolean hasMoreEmails;
    int expiredEmailCount;

    public ExpiredCouponFetcher(Gmail service,EmailTrasher emailTrasher,
                                EmailBodyFetcher emailBodyFetcher, EmailExpiryChecker emailExpiryChecker) {
        this.service = service;
        this.emailTrasher = emailTrasher;
        this.emailBodyFetcher = emailBodyFetcher;
        this.emailExpiryChecker = emailExpiryChecker;
        labelIds.add("CATEGORY_PROMOTIONS"); //to filter promotional emails
        expiredEmailCount = 0;
        hasMoreEmails = false;
    }

    public void trashEmails() throws IOException{
        ListMessagesResponse msgList = getPromotionalEmails();  //list of first 500 promotional mails

        if(msgList.getMessages() != null){                //if there are any messages
            trashExpiredEmails(msgList);
            while(hasMoreEmails){
                msgList = getPromotionalEmails();           //get next 500 promotional emails with page token
                trashExpiredEmails(msgList);
            }
            System.out.println(expiredEmailCount + " emails moved to the trash folder.");
        }
        else {
            System.out.println("No expired emails were found.");
        }

    }

    private void trashExpiredEmails(ListMessagesResponse msgList) throws IOException {
            String emailBody = "";
            boolean isExpired;
            for (Message msg : msgList.getMessages()) {     //for every msg get emailBody
//        Message msg = msgList.getMessages().get(2);
                String msgId = msg.getId();
                emailBody = emailBodyFetcher.getEmailBody(msgId);            //returns emailBody for the msgId
                isExpired = emailExpiryChecker.checkExpiry(emailBody);
                if(isExpired){
                    emailTrasher.trashEmails(msgId);
                    expiredEmailCount++;
                }
            }
    }

    private ListMessagesResponse getPromotionalEmails() throws IOException { //returns first 500 promotional emails
        Gmail.Users.Messages.List request = service.users().messages().list(user)
                                                .setLabelIds(labelIds)
                                                .setMaxResults(500L);

        if(hasMoreEmails){
            request.setPageToken(nextPageToken);
        }

        ListMessagesResponse msgList = request.execute();   //returns emails with the above criteria
        nextPageToken = msgList.getNextPageToken(); //get next page token
        hasMoreEmails = (nextPageToken != null);

        return msgList;
    }

}