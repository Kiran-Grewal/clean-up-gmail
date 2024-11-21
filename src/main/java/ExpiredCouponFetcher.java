import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class ExpiredCouponFetcher extends EmailFetcher {
    private final EmailBodyFetcher emailBodyFetcher;
    private final EmailExpiryChecker emailExpiryChecker;

    public ExpiredCouponFetcher(Gmail service,EmailTrasher emailTrasher,
                                EmailBodyFetcher emailBodyFetcher, EmailExpiryChecker emailExpiryChecker) {
        super(service,emailTrasher);
        this.emailBodyFetcher = emailBodyFetcher;
        this.emailExpiryChecker = emailExpiryChecker;
    }

    public void trashProcedure() {
        findExpiredEmails(msgList);         //finds emails with expired offers and trashes them
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
                expiredEmailsCount++;
            }
        }
    }

}