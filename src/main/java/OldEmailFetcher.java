import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OldEmailFetcher {
    private final Gmail service;
    private final EmailTrasher emailTrasher;
    private static final String user = "me";
    private static final int emailRetentionDays = 30; //no. of days to keep the promotional emails for

    private final List<String> labelIds = new ArrayList<>(); //to filter promotional emails
    private final LocalDate deleteDate;
    private final String query; //the query to get emails older than numOfDays
    private String nextPageToken;
    private boolean hasMoreEmails;

    public OldEmailFetcher(Gmail service, EmailTrasher emailTrasher) {
        this.service = service;
        this.emailTrasher = emailTrasher;
        labelIds.add("CATEGORY_PROMOTIONS");    //to filter promotional emails
        deleteDate = LocalDate.now().minusDays(emailRetentionDays);
        query = "before:" + deleteDate;     //the query to get emails older than numOfDays
        hasMoreEmails = false;
    }

    public void trashOldEmails() {   //trashes promotional emails older than a month
        ListMessagesResponse msgList;
        int trashedEmailsCount = 0;     //Counter for the number of deleted emails
        do {
            msgList = getOldEmails();

            if (msgList.getMessages() != null) {
                trashedEmailsCount += msgList.getMessages().size();
                emailTrasher.trashEmails(msgList);   //move all the emails in msgList to trash
            }

        } while(hasMoreEmails);     //get next set of emails if there are any

        if(trashedEmailsCount > 0) {
            System.out.println(trashedEmailsCount + " emails moved to the trash folder.");
        }
        else {
            System.out.println("No emails older than " + deleteDate + " were found.");
        }
    }

    private ListMessagesResponse getOldEmails(){    //return promotional emails older than a month
        ListMessagesResponse msgList = null;

        try {
            //API request to fetch emails with the following criteria
            Gmail.Users.Messages.List request = service.users().messages().list(user)
                                                    .setLabelIds(labelIds)
                                                    .setQ(query)
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