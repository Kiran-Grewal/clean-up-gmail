import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class EmailBodyFetcher {
    private Gmail service;
    private static final String user = "me";

    public EmailBodyFetcher(Gmail service) {
        this.service = service;
    }

    public String getEmailBody(String msgId) throws IOException {
        Message msg = service.users().messages().get(user, msgId).execute();    //proper message object with all details
        String emailBody = getEmailBody(msg);
        return emailBody;
    }

    private String getEmailBody(Message msg) throws IOException {              //returns emailBody for the msg
        MessagePart msgPayload = msg.getPayload();                  //parsed email structure
        String mimeType = msgPayload.getMimeType();                 //the mime type
        String msgBody = "";                                        // the email text body as base64URL encoded String
        if(mimeType.equals("text/plain")){
            msgBody = msgPayload.getBody().getData();
        }
        else if(mimeType.equals("text/html")){                                  //if no "text/plain" is there
            msgBody = "";
        }
        else if (mimeType.equals("multipart/alternative") || mimeType.equals("multipart/mixed")) {
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
                    List<MessagePart> partsOfParts = parts.get(i).getParts();
                    int j = 0;
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
        byte[] decodedBodyBytes = Base64.getUrlDecoder().decode(msgBody);       //decoding msgBody to Bytes
        String actualBody = new String(decodedBodyBytes);                       //converting bytes to String
        return actualBody;
    }

    private boolean msgBody findEmailBody(List<MessagePart> parts) {
        boolean found = false;                                  //is "Text/plain" part found
        int i = 0;
        while (!found && i < parts.size()) {
            if (parts.get(i).getMimeType().equals("text/plain")) {
                found = true;
                msgBody = parts.get(i).getBody().getData();     // the email text body as base64URL encoded String
            }
        }
    }
}
