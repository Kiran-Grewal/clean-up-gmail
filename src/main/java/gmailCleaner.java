import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class GmailCleaner {

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        GmailService gmailservice = new GmailService();
        Gmail service = gmailservice.getGmailService(); //gmail API client service.

        OldEmailFetcher oldEmailFetcher = new OldEmailFetcher(service);
        ListMessagesResponse msgList = oldEmailFetcher.getOldEmails();

        if(msgList.getMessages() != null) {
            int trashedEmailsCount = msgList.getMessages().size(); //num of emails deleted
            EmailTrasher emailTrasher = new EmailTrasher(service);
            emailTrasher.trashEmails(msgList); //trashes the emails in msgList

            while(oldEmailFetcher.hasMoreEmails()) {
                msgList = oldEmailFetcher.getMoreOldEmails();
                trashedEmailsCount += msgList.getMessages().size();
                emailTrasher.trashEmails(msgList); //trashes the emails in msgList
            }
            System.out.println(trashedEmailsCount + " emails moved to the trash folder.");
        }
        else {
            System.out.println("No emails older than " + oldEmailFetcher.getDeleteDate() + " were found.");
        }
    }
}
