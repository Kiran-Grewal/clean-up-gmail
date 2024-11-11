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

    public void trashEmails(ListMessagesResponse msgList) throws IOException {  //Send emails to trash folder
        for(Message msg: msgList.getMessages()) {
            String msgId = msg.getId();
            service.users().messages().trash(user,msgId).execute();
        }
    }

    public void trashEmails(String msgId) throws IOException {      //Send emails to trash folder
            service.users().messages().trash(user,msgId).execute();
    }

}
