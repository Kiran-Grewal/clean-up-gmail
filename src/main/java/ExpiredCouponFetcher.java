import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class ExpiredCouponFetcher extends EmailFetcher {
    private final EmailBodyFetcher emailBodyFetcher;
    private final EmailExpiryChecker emailExpiryChecker;
    private int expiredEmailCount;  //Counter for the number of emails with expired offers

    public ExpiredCouponFetcher(Gmail service,EmailTrasher emailTrasher,
                                EmailBodyFetcher emailBodyFetcher, EmailExpiryChecker emailExpiryChecker) {
        super(service,emailTrasher);
        this.emailBodyFetcher = emailBodyFetcher;
        this.emailExpiryChecker = emailExpiryChecker;
        expiredEmailCount = 0;
    }

    public void trashProcedure() {
        findExpiredEmails(msgList);         //finds emails with expired offers and trashes them
    }

    public void logOutput(){
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

}