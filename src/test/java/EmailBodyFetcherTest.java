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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailBodyFetcherTest {
    private EmailBodyFetcher emailBodyFetcher;
    private Message mockMsg;
    private final String mockMsgId = "";
    private MessagePart mockMsgPayload;

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

        mockMsgPayload = new MessagePart();
    }

    @Test
    @DisplayName("When text/plain is the only mime type")
    public void getEmailBodyTest1() {

        MessagePartBody mockBody = new MessagePartBody();
        String mockData = "dGV4dC9wbGFpbiBwYXJ0";           //base64URL encoded String for "text/plain part"

        mockMsgPayload.setMimeType("text/plain");
        mockBody.setData(mockData);
        mockMsgPayload.setBody(mockBody);
        mockMsg.setPayload(mockMsgPayload);

        String result = emailBodyFetcher.getEmailBody(mockMsgId);
        Assertions.assertEquals(result, "text/plain part");
    }

    @Test
    @DisplayName("When html/plain is the only mime type")
    public void getEmailBodyTest2() {

        MessagePartBody mockBody = new MessagePartBody();
        String mockData = "aHRtbC9wbGFpbiBwYXJ0";               //base64URL encoded String for "html/plain part"

        mockMsgPayload.setMimeType("html/plain");
        mockBody.setData(mockData);
        mockMsgPayload.setBody(mockBody);
        mockMsg.setPayload(mockMsgPayload);

        String result = emailBodyFetcher.getEmailBody(mockMsgId);
        Assertions.assertEquals(result,"");
    }

    @Test
    @DisplayName("When text/plain is inside multipart mime type")
    public void getEmailBodyTest3() {

        mockMsgPayload.setMimeType("multipart/alternative");
        List<MessagePart> mockParts = new ArrayList<>();
        MessagePart mockMsgPart = new MessagePart();      //mockMsgPart inside original mockMsgPayload
        MessagePartBody mockBody = new MessagePartBody();
        mockMsgPart.setMimeType("text/plain");
        String mockData =
              "dGV4dC9wbGFpbiBwYXJ0IGluIG11bHRpcGFydA=="; //base64URL encoded String for "text/plain part in multipart"

        mockBody.setData(mockData);
        mockMsgPart.setBody(mockBody);
        mockParts.add(mockMsgPart);
        mockMsgPayload.setParts(mockParts);
        mockMsg.setPayload(mockMsgPayload);

        String result = emailBodyFetcher.getEmailBody(mockMsgId);
        Assertions.assertEquals(result,"text/plain part in multipart");
    }

    @Test
    @DisplayName("When text/plain and html/plain are both inside multipart/mixed type")
    public void getEmailBodyTest4() {

        mockMsgPayload.setMimeType("multipart/mixed");
        List<MessagePart> mockParts = new ArrayList<>();

        //mockMsgPart1 inside original mockMsgPayload
        MessagePart mockMsgPart1 = new MessagePart();
        mockMsgPart1.setMimeType("html/plain");

        //mockMsgPart2 inside original mockMsgPayload
        MessagePart mockMsgPart2 = new MessagePart();
        MessagePartBody mockBody = new MessagePartBody();
        mockMsgPart2.setMimeType("text/plain");
        String mockData =
                "dGV4dC9wbGFpbiBwYXJ0IGluIG11bHRpcGFydA=="; //base64URL encoded String for "text/plain part in multipart"

        mockBody.setData(mockData);
        mockMsgPart2.setBody(mockBody);             //setting body for text/plain mockMsgPart

        mockParts.add(mockMsgPart1);
        mockParts.add(mockMsgPart2);
        mockMsgPayload.setParts(mockParts);
        mockMsg.setPayload(mockMsgPayload);

        String result = emailBodyFetcher.getEmailBody(mockMsgId);
        Assertions.assertEquals(result,"text/plain part in multipart");
    }


    @Test
    @DisplayName("When text/plain is in multipart/alternative which is in multipart/mixed type")
    public void getEmailBodyTest5() {

        mockMsgPayload.setMimeType("multipart/mixed");
        List<MessagePart> mockParts = new ArrayList<>();

        //mockMsgPart1 inside original mockMsgPayload
        MessagePart mockMsgPart = new MessagePart();
        mockMsgPart.setMimeType("multipart/alternative");
        List<MessagePart> mockPartsOfParts = new ArrayList<>();

        //mockMsgPart2 inside mockMsgPart1 mockMsgPayload
        MessagePart mockNestedMsgPart = new MessagePart();
        MessagePartBody mockBody = new MessagePartBody();
        mockNestedMsgPart.setMimeType("text/plain");
        //base64URL encoded String for "text/plain part in multipart/alternative inside multipart/mixed"
        String mockData =
                "dGV4dC9wbGFpbiBwYXJ0IGluIG11bHRpcGFydC9hbHRlcm5hdGl2ZSBpbnNpZGUgbXVsdGlwYXJ0L21peGVk";

        mockBody.setData(mockData);
        mockNestedMsgPart.setBody(mockBody);             //setting body for text/plain mockMsgPart

        mockParts.add(mockMsgPart);
        mockPartsOfParts.add(mockNestedMsgPart);
        mockMsgPayload.setParts(mockParts);
        mockMsgPart.setParts(mockPartsOfParts);
        mockMsg.setPayload(mockMsgPayload);

        String result = emailBodyFetcher.getEmailBody(mockMsgId);
        Assertions.assertEquals(result,"text/plain part in multipart/alternative inside multipart/mixed");
    }
}