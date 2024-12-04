import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OldEmailFetcherTest {
    Gmail mockService;
    Gmail.Users mockUser;
    Gmail.Users.Messages mockMessages;
    Gmail.Users.Messages.List mockRequest;
    EmailTrasher mockEmailTrasher;
    private OldEmailFetcher oldEmailFetcher;

    @BeforeEach
    public void startUp() throws IOException {
        mockService = mock(Gmail.class);
        mockUser = mock(Gmail.Users.class);
        mockMessages = mock(Gmail.Users.Messages.class);
        mockRequest = mock(Gmail.Users.Messages.List.class);
//        List<String> mockLabelIds = new ArrayList<>();
        mockEmailTrasher = mock(EmailTrasher.class);
        oldEmailFetcher = new OldEmailFetcher(mockService,mockEmailTrasher);

        when(mockService.users()).thenReturn(mockUser);
        when(mockUser.messages()).thenReturn(mockMessages);
        when(mockMessages.list("me")).thenReturn(mockRequest);
        when(mockRequest.setLabelIds(anyList())).thenReturn(mockRequest);
        when(mockRequest.setMaxResults(anyLong())).thenReturn(mockRequest);
        when(mockRequest.setQ(anyString())).thenReturn(mockRequest);
//        when(mockRequest.setPageToken(anyString())).thenReturn(mockRequest.setPageToken(nextPageToken));

    }

    @Test
    @DisplayName("When emails to fetcher = 10")
    public void trashProcedure() throws IOException {
        ListMessagesResponse msgListResponse = new ListMessagesResponse();
        List<Message> msgList = new ArrayList<>();
        Message msg1 = new Message();
        msg1.setId("abcd");
        msgList.add(msg1);

        msgListResponse.setMessages(msgList);
        msgListResponse.setNextPageToken(null);

        when(mockRequest.execute()).thenReturn(msgListResponse);
        doNothing().when(mockEmailTrasher).trashEmails(msgListResponse);

        oldEmailFetcher.trashEmails();
        Assertions.assertEquals(re);
    }
}