import com.google.api.services.gmail.Gmail;

import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTrasherTest {
    Gmail mockService;
    Gmail.Users mockUser;
    Gmail.Users.Messages mockMessages;
    Gmail.Users.Messages.Trash mockTrashRequest;
    private EmailTrasher emailTrasher;

    @BeforeEach
    public void setUp(){
        mockService = mock(Gmail.class);
        mockUser = mock(Gmail.Users.class);
        mockMessages = mock(Gmail.Users.Messages.class);
        mockTrashRequest = mock(Gmail.Users.Messages.Trash.class);
        emailTrasher = new EmailTrasher(mockService);

        when(mockService.users()).thenReturn(mockUser);
        when(mockUser.messages()).thenReturn(mockMessages);
    }

    @Test
    @DisplayName("Trashing single email")
    public void trashEmailsTest1() throws IOException {
        String mockMsgId = "1234";

        when(mockMessages.trash("me",mockMsgId)).thenReturn(mockTrashRequest);
        when(mockTrashRequest.execute()).thenReturn(null);

        emailTrasher.trashEmails(mockMsgId);
        verify(mockService.users().messages()).trash("me","1234");
    }

    @Test
    @DisplayName("Trashing multiple emails")
    public void trashEmailsTest2() throws IOException {
        ListMessagesResponse msgListResponse = new ListMessagesResponse();

        List<Message> msgList = new ArrayList<>();

        Message msg1 = new Message();
        msg1.setId("abcd");

        Message msg2 = new Message();
        msg2.setId("efgh");

        Message msg3 = new Message();
        msg3.setId("ijkl");

        msgList.add(msg1);
        msgList.add(msg2);
        msgList.add(msg3);

        msgListResponse.setMessages(msgList);

        when(mockMessages.trash("me","abcd")).thenReturn(mockTrashRequest);
        when(mockMessages.trash("me","efgh")).thenReturn(mockTrashRequest);
        when(mockMessages.trash("me","ijkl")).thenReturn(mockTrashRequest);

        when(mockTrashRequest.execute()).thenReturn(null);

        emailTrasher.trashEmails(msgListResponse);
        verify(mockService.users().messages()).trash("me","abcd");
        verify(mockService.users().messages()).trash("me","efgh");
        verify(mockService.users().messages()).trash("me","ijkl");
    }
    
}