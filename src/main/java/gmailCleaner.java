import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class gmailCleaner {
    private static Gmail service;
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_MODIFY);
    private static final String user = "me";
    private static final int numOfDays = 30; //no. of days to keep the promotional emails for

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        service = new Gmail.Builder(httpTransport, jsonFactory, getCredentials(httpTransport, jsonFactory))
                .setApplicationName("clean-up-gmail")
                .build();
        getPromotionsMails();
    }

    public static void getPromotionsMails() throws IOException { //get Promotional mails older than numOfDays
        List<String> labelIds = new ArrayList<>();
        labelIds.add("CATEGORY_PROMOTIONS");
        LocalDate deleteDate = LocalDate.now().minusDays(numOfDays);
        String query = "before:" + deleteDate;
        ListMessagesResponse msgList = service.users().messages().list(user).setLabelIds(labelIds).setQ(query)
                                       .setMaxResults(500L).execute(); //get emails with the above criteria
        trashEmails(msgList);

        String nextPageToken = msgList.getNextPageToken(); //get next page token
        while(nextPageToken != null){
            msgList = service.users().messages().list(user).setLabelIds(labelIds).setQ(query)
                      .setAccessToken(nextPageToken).setMaxResults(500L).execute(); //get next set of emails with next page token
            trashEmails(msgList);
            nextPageToken = msgList.getNextPageToken(); //get next page token
        }
    }

    public static void trashEmails(ListMessagesResponse msgList) throws IOException { //Send emails to trash folder
        for(Message msg: msgList.getMessages()) {
            String msgId = msg.getId();
            service.users().messages().trash(user,msgId).execute();
        }
    }

    private static Credential getCredentials(final NetHttpTransport httpTransport, JsonFactory jsonFactory)
            throws IOException {
        // Load client secrets.
        InputStream in = gmailCleaner.class.getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + "/credentials.json");
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("Tokens")))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }
}
