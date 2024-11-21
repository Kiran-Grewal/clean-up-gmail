import com.google.api.services.gmail.Gmail;

import java.time.LocalDate;

public class OldEmailFetcher extends EmailFetcher {
    private static final int emailRetentionDays = 30; //no. of days to keep the promotional emails for

    public OldEmailFetcher(Gmail service, EmailTrasher emailTrasher) {
        super(service, emailTrasher);
        deleteDate = LocalDate.now().minusDays(emailRetentionDays);
        query = "before:" + deleteDate;     //the query to get emails older than numOfDays
    }

    public void trashProcedure() {
        oldEmailsCount += msgList.getMessages().size();
        emailTrasher.trashEmails(msgList);   //move all the emails in msgList to trash
    }

}