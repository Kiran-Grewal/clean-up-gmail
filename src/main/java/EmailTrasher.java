import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;

public class EmailTrasher {
    private final Gmail service;
    private static final String user = "me";

    public EmailTrasher(Gmail service){
        this.service = service;
    }

    public void trashEmails(ListMessagesResponse msgList) {  //Send emails to trash folder
        for(Message msg: msgList.getMessages()) {
            String msgId = msg.getId();
            try {
                service.users().messages().trash(user, msgId).execute();
            }
            catch(IOException exp){
                System.out.println("Failed to trash email. Error : " + exp.getMessage());
            }
        }
    }

    public void trashEmails(String msgId) {      //Send email to trash folder
        try {
            service.users().messages().trash(user, msgId).execute();
        }
        catch(IOException exp){
            System.out.println("Failed to trash email. Error : " + exp.getMessage());
        }
    }

}
