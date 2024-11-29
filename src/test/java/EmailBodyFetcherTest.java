import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.Gmail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailBodyFetcherTest {
    private EmailBodyFetcher emailBodyFetcher;
    Message mockMsg;
    String mockMsgId = "";

    @BeforeEach
    public void setUp() throws IOException {
        Gmail mockService = mock(Gmail.class);
        Gmail.Users mockUser = mock(Gmail.Users.class);
        Gmail.Users.Messages mockMessages = mock(Gmail.Users.Messages.class);
        Gmail.Users.Messages.Get mockGetRequest = mock(Gmail.Users.Messages.Get.class);
        emailBodyFetcher = new EmailBodyFetcher(mockService);
        mockMsg = new Message();

        when(mockService.users()).thenReturn(mockUser);
        when(mockUser.messages()).thenReturn(mockMessages);
        when(mockMessages.get("me",mockMsgId)).thenReturn(mockGetRequest);
        when(mockGetRequest.execute()).thenReturn(mockMsg);
    }

    @Test
    @DisplayName("Text/Plain is the only mime type")
    public void getEmailBodyTest1() {

            MessagePart mockMsgPayload = new MessagePart();
            MessagePartBody mockBody = new MessagePartBody();
            String mockData = "cGxhaW4gdGV4dCBwYXJ0";
            mockMsgPayload.setMimeType("text/plain");
            mockBody.setData(mockData);
            mockMsgPayload.setBody(mockBody);
            mockMsg.setPayload(mockMsgPayload);

            String result = emailBodyFetcher.getEmailBody(mockMsgId);
            Assertions.assertEquals(result,"plain text part");
    }
}