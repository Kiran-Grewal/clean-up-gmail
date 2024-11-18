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
        if (mimeType.equals("text/plain")) {
            msgBody = msgPayload.getBody().getData();
        }
        else if (mimeType.equals("text/html")) {                    //if no "text/plain" is there
            msgBody = "";
        }
        else if (mimeType.startsWith("multipart/")){
            msgBody = findEmailBody(msgPayload);
        }
        byte[] decodedBodyBytes = Base64.getUrlDecoder().decode(msgBody);       //decoding msgBody to Bytes
        String actualBody = new String(decodedBodyBytes);                       //converting bytes to String
        return actualBody;
    }

    private String findEmailBody(MessagePart msgPayload) {
        List<MessagePart> parts = msgPayload.getParts();        //List of Parts
        boolean found = false;                                  //is "Text/plain" part found
        String msgBody = "";

        for(MessagePart msgPart: parts) {
            if (msgPart.getMimeType().equals("text/plain")) {
                msgBody = msgPart.getBody().getData();     // the email text body as base64URL encoded String
                return msgBody;
            } else if (msgPart.getMimeType().equals("multipart/alternative")) {
                //if one part is "multipart/alternative" in itself then it would probably have "text/plain" part inside it
                //call recursive method findEmailBody for parts of multipart/alternative
                msgBody = findEmailBody(msgPart);
            }
        }
        return msgBody;
    }

}
