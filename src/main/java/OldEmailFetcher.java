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
    private static final int numOfDays = 30; //no. of days to keep the promotional emails for

    private final List<String> labelIds = new ArrayList<>(); //to filter promotional emails
    private final LocalDate deleteDate;
    private final String query; //the query to get emails older than numOfDays
    private String nextPageToken;
    private boolean hasMoreEmails;

    public OldEmailFetcher(Gmail service, EmailTrasher emailTrasher) {
        this.service = service;
        this.emailTrasher = emailTrasher;
        labelIds.add("CATEGORY_PROMOTIONS"); //to filter promotional emails
        deleteDate = LocalDate.now().minusDays(numOfDays);
        query = "before:" + deleteDate; //the query to get emails older than numOfDays
        hasMoreEmails = false;
    }

    public void trashOldEmails() throws IOException { //trashes promotional emails older than a month
        ListMessagesResponse msgList = getOldEmails();

        if(msgList.getMessages() != null) {
            int trashedEmailsCount = msgList.getMessages().size();  //num of emails deleted
            emailTrasher.trashEmails(msgList);   //trashes all the emails in msgList

            while(hasMoreEmails) {
                msgList = getMoreOldEmails();       // get next set of emails
                trashedEmailsCount += msgList.getMessages().size();
                emailTrasher.trashEmails(msgList);   //trashes all the emails in msgList
            }
            System.out.println(trashedEmailsCount + " emails moved to the trash folder.");
        }
        else {
            System.out.println("No emails older than " + deleteDate+ " were found.");
        }
    }

    private ListMessagesResponse getOldEmails() throws IOException { //return first 500 promotional emails
        ListMessagesResponse msgList = service.users().messages().list(user)
                                                .setLabelIds(labelIds)
                                                .setQ(query)
                                                .setMaxResults(500L)
                                                .execute();         //get emails with the above criteria

        nextPageToken = msgList.getNextPageToken(); //get next page token
        hasMoreEmails = (nextPageToken != null);

        return msgList;
    }

    private ListMessagesResponse getMoreOldEmails() throws IOException { //return next set of emails with next page token
        ListMessagesResponse msgList = service.users().messages().list(user)
                                                .setLabelIds(labelIds)
                                                .setQ(query)
                                                .setPageToken(nextPageToken)
                                                .setMaxResults(500L)
                                                .execute();         //get next set of emails with next page token

        nextPageToken = msgList.getNextPageToken(); //get next page token
        hasMoreEmails = (nextPageToken != null);

        return msgList;
    }
}