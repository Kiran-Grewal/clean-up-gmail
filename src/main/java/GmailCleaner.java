import com.google.api.services.gmail.Gmail;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GmailCleaner {

    public static void main(String[] args) throws IOException, GeneralSecurityException {

        GmailService gmailService = new GmailService();
        Gmail service = gmailService.getGmailService(); //gmail API client service.

        EmailTrasher emailTrasher = new EmailTrasher(service);

//        OldEmailFetcher oldEmailFetcher = new OldEmailFetcher(service,emailTrasher);
//        oldEmailFetcher.trashEmails();

        EmailBodyFetcher emailBodyFetcher = new EmailBodyFetcher(service);
        EmailExpiryChecker emailExpiryChecker = new EmailExpiryChecker();
        ExpiredCouponFetcher expiredCouponFetcher = new ExpiredCouponFetcher(service,emailTrasher,
                                                            emailBodyFetcher,emailExpiryChecker);
        expiredCouponFetcher.trashEmails();
    }
}
