import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class EmailFetcher {
    protected final Gmail service;
    protected final EmailTrasher emailTrasher;
    protected static final String user = "me";

    protected final List<String> labelIds = new ArrayList<>(); //to filter promotional emails
    protected String query = "";                               //the query to get emails older than numOfDays
    protected LocalDate deleteDate;
    protected ListMessagesResponse msgList = null;
    protected String nextPageToken;
    protected boolean hasMoreEmails;

    protected EmailFetcher(Gmail service, EmailTrasher emailTrasher) {
        this.service = service;
        this.emailTrasher = emailTrasher;
        labelIds.add("CATEGORY_PROMOTIONS");    //to filter promotional emails
        hasMoreEmails = false;
    }

    protected void trashEmails() {      //trashes promotional emails
        do {
            msgList = getPromotionalEmails();

            if (msgList.getMessages() != null) {
                trashProcedure();       //specific actions for subclasses to trash emails
            }

        } while(hasMoreEmails);         //get next set of emails if there are any

        logOutput();                    //print output

    }

    protected abstract void trashProcedure();               //specific actions for subclasses to trash emails

    protected abstract void logOutput();                    //print output

    protected ListMessagesResponse getPromotionalEmails() { //return promotional emails

        try{
            //API request to fetch emails with the following criteria
            Gmail.Users.Messages.List request = service.users().messages().list(user)
                                                    .setLabelIds(labelIds)
                                                    .setMaxResults(500L);

            if(!query.isEmpty()){                           //for oldEmailFetcher return emails older than a month
                request.setQ(query);
            }

            if(hasMoreEmails){                              //if another set of emails was fetched before
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
