import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ExpiredCouponsFetcher {
    private Gmail service;
    private static final String user = "me";

    private final List<String> labelIds = new ArrayList<>(); //to filter promotional emails
    private String nextPageToken;
    private boolean hasMoreEmails;

    public ExpiredCouponsFetcher(Gmail service) {
        this.service = service;
        labelIds.add("CATEGORY_PROMOTIONS"); //to filter promotional emails
    }

    public void getExpiredEmails() throws IOException {
        ListMessagesResponse msgList = getPromotionalEmails(); //list of first 500 promotional mails
        //not 100% proper
        do {
            if(hasMoreEmails){
                msgList = getMorePromotionalEmails();
            }
            if (msgList.getMessages() != null) {
                String emailBody = "";
                for (Message msg : msgList.getMessages()) {
                    String msgId = msg.getId();
                    emailBody = getEmailBody(msgId);
                }
            }
            hasMoreEmails = (nextPageToken != null);
        }
        while(hasMoreEmails);
    }

    private String getEmailBody(String msgId) throws IOException {
        Message msg = service.users().messages().get(user, msgId).execute();    //proper message object with all details
        MessagePart msgPayload = msg.getPayload();                  //parsed email structure
        String mimeType = msgPayload.getMimeType();                 //the mime type
        String msgBody = "";                                        // the email text body as base64URL encoded String
        if (mimeType.equals("multipart/alternative") || mimeType.equals("multipart/mixed")) {
            List<MessagePart> parts = msgPayload.getParts();        //List of Parts
            boolean found = false;                                  //is "Text/plain" part found
            int i = 0;
            while (!found && i < parts.size()) {
                if (parts.get(i).getMimeType().equals("text/plain")) {
                    found = true;
                    msgBody = parts.get(i).getBody().getData();     // the email text body as base64URL encoded String
                }
                else if (parts.get(i).getMimeType().equals("multipart/alternative")) {
                    //if one part is "multipart/alternative" in itself then it would probably have "text/plain" part inside it
                    int j = 0;
                    List<MessagePart> partsOfParts = parts.get(i).getParts();
                    while (!found && j < partsOfParts.size()) {
                        if (partsOfParts.get(j).getMimeType().equals("text/plain")) {
                            found = true;
                            msgBody = partsOfParts.get(j).getBody().getData();
                        }
                        j++;
                    }
                }
                i++;
            }
        }
        else if(mimeType.equals("text/plain")){
            msgBody = msgPayload.getBody().getData();
        }
        else if(mimeType.equals("text/html")){      //if no "text/plain" is there
            msgBody = "";
        }
        byte[] decodedBodyBytes = Base64.getUrlDecoder().decode(msgBody);       //decoding msgBody to Bytes
        String actualBody = new String(decodedBodyBytes);                       //converting bytes to String
        return actualBody;
    }

    private ListMessagesResponse getPromotionalEmails() throws IOException { //returns first 500 promotional emails
        ListMessagesResponse msgList = service.users().messages().list(user)
                                                .setLabelIds(labelIds)
                                                .setMaxResults(500L)
                                                .execute(); //get emails with the above criteria

        nextPageToken = msgList.getNextPageToken(); //get next page token

        return msgList;
    }

    private ListMessagesResponse getMorePromotionalEmails() throws IOException { //returns emails with next page token
        ListMessagesResponse msgList = service.users().messages().list(user)
                                                .setLabelIds(labelIds)
                                                .setPageToken(nextPageToken)
                                                .setMaxResults(500L)
                                                .execute(); //get emails with the above criteria

        nextPageToken = msgList.getNextPageToken(); //get next page token

        return msgList;
    }

}
