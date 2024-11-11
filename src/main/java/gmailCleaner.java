import com.google.api.services.gmail.Gmail;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;


public class GmailCleaner {

    public static void main(String[] args) throws IOException, GeneralSecurityException, ParseException {
        GmailService gmailservice = new GmailService();
        Gmail service = gmailservice.getGmailService(); //gmail API client service.

        EmailTrasher emailTrasher = new EmailTrasher(service);

//        OldEmailFetcher oldEmailFetcher = new OldEmailFetcher(service,emailTrasher);
//        oldEmailFetcher.trashOldEmails();

//        ExpiredCouponFetcher expiredCouponFetcher = new ExpiredCouponsFetcher(service,emailTrasher);
//        expiredCouponsFetcher.getExpiredEmails();

    }
}
