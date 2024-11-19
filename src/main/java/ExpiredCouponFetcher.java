import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.util.*;

public class ExpiredCouponFetcher {
    private final Gmail service;
    private final EmailTrasher emailTrasher;
    private final EmailBodyFetcher emailBodyFetcher;
    private final EmailExpiryChecker emailExpiryChecker;
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
        labelIds.add("CATEGORY_PROMOTIONS");    //to filter promotional emails
        expiredEmailCount = 0;                  //Counter for the number of emails with expired offers
        hasMoreEmails = false;
    }

    public void trashExpiredEmails() {
        ListMessagesResponse msgList;
        do{
            msgList = getPromotionalEmails();

            if(msgList.getMessages() != null) {
                findExpiredEmails(msgList);         //finds emails with expired offers and trashes them
            }

        } while(hasMoreEmails);     //get next set of emails if there are any

        if(expiredEmailCount > 0) {
            System.out.println(expiredEmailCount + " expired emails moved to the trash folder.");
        }
        else {
        System.out.println("No expired emails were found.");
        }
    }

    private void findExpiredEmails(ListMessagesResponse msgList) {  //finds emails with expired offers and trashes them
        String emailBody;
        boolean isExpired;

        for (Message msg : msgList.getMessages()) {
            String msgId = msg.getId();
            emailBody = emailBodyFetcher.getEmailBody(msgId);            //returns emailBody for the msgId
            isExpired = emailExpiryChecker.checkExpiry(emailBody);       //returns true if an expired offer is found
            if (isExpired) {
                emailTrasher.trashEmails(msgId);
                expiredEmailCount++;
            }
        }
    }

    private ListMessagesResponse getPromotionalEmails() {      //return promotional emails
        ListMessagesResponse msgList = null;

        try{
            //API request to fetch emails with the following criteria
            Gmail.Users.Messages.List request = service.users().messages().list(user)
                                                    .setLabelIds(labelIds)
                                                    .setMaxResults(500L);
            if(hasMoreEmails){
                request.setPageToken(nextPageToken);
            }

            msgList = request.execute();                    //returns emails with the above criteria
            nextPageToken = msgList.getNextPageToken();     //get next page token
            hasMoreEmails = (nextPageToken != null);
        }
        catch(IOException exp){
            System.out.println("Failed to retrieve promotional emails. Error : " + exp.getMessage());
        }

        return msgList;
    }

}