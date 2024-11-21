import com.google.api.services.gmail.Gmail;

import java.time.LocalDate;

public class OldEmailFetcher extends EmailFetcher {
    private static final int emailRetentionDays = 30; //no. of days to keep the promotional emails for
    private int trashedEmailCount;                    //Counter for the number of emails trashed


    public OldEmailFetcher(Gmail service, EmailTrasher emailTrasher) {
        super(service, emailTrasher);
        deleteDate = LocalDate.now().minusDays(emailRetentionDays);
        query = "before:" + deleteDate;     //the query to get emails older than numOfDays
        trashedEmailCount = 0;
    }

    public void trashProcedure() {
        trashedEmailCount += msgList.getMessages().size();
        emailTrasher.trashEmails(msgList);   //move all the emails in msgList to trash
    }

    public void logOutput(){
        if(trashedEmailCount > 0) {
            System.out.println(trashedEmailCount + " emails moved to the trash folder.");
        }
        else {
            System.out.println("No emails older than " + deleteDate + " were found.");
        }
    }

}