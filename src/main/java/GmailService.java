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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GmailService {
    private Gmail service; // the gmail API client service.
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_MODIFY); //scope of Application

    public GmailService() throws IOException, GeneralSecurityException{
        service = createService();
    }

    public Gmail getGmailService() {
        return service;
    }

    private Gmail createService() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return service = new Gmail.Builder(httpTransport, jsonFactory, getCredentials(httpTransport, jsonFactory))
                .setApplicationName("clean-up-gmail")
                .build();
    }

    private static Credential getCredentials(final NetHttpTransport httpTransport, JsonFactory jsonFactory)
            throws IOException {
        // Load client secrets.
        InputStream in = GmailService.class.getResourceAsStream("/credentials.json");
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

