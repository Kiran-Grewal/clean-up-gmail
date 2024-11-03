import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OldEmailFetcher {
    private Gmail service;
    private static final String user = "me";
    private static final int numOfDays = 30; //no. of days to keep the promotional emails for

    private final List<String> labelIds = new ArrayList<>(); //to filter promotional emails
    private final LocalDate deleteDate;
    private final String query; //the query to get emails older than numOfDays
    private String nextPageToken;
    private boolean hasMoreEmails;

    public OldEmailFetcher(Gmail service) {
        this.service = service;
        labelIds.add("CATEGORY_PROMOTIONS"); //to filter promotional emails
        deleteDate = LocalDate.now().minusDays(numOfDays);
        query = "before:" + deleteDate; //the query to get emails older than numOfDays
        hasMoreEmails = false;
    }

    public boolean hasMoreEmails() {
        return hasMoreEmails;
    }

    public LocalDate getDeleteDate(){
        return deleteDate;
    }

    public ListMessagesResponse getOldEmails() throws IOException {
        ListMessagesResponse msgList = service.users().messages().list(user)
                                            .setLabelIds(labelIds)
                                            .setQ(query)
                                            .setMaxResults(500L)
                                            .execute(); //get emails with the above criteria

        nextPageToken = msgList.getNextPageToken(); //get next page token
        hasMoreEmails = (nextPageToken != null);

        return msgList;
    }

    public ListMessagesResponse getMoreOldEmails() throws IOException {
        ListMessagesResponse msgList = service.users().messages().list(user)
                                                .setLabelIds(labelIds)
                                                .setQ(query)
                                                .setPageToken(nextPageToken)
                                                .setMaxResults(500L)
                                                .execute(); //get next set of emails with next page token

        nextPageToken = msgList.getNextPageToken(); //get next page token
        hasMoreEmails = (nextPageToken != null);

        return msgList;
    }
}

