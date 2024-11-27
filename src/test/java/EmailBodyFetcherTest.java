import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.google.api.services.gmail.Gmail;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class EmailBodyFetcherTest {
    Gmail mockService;
    Gmail.Users.Messages.Get mockGetRequest;

    @BeforeEach
    void setUp(){
        mockService = mock(Gmail.class);
        mockGetRequest = mock(Gmail.Users.Messages.Get.class);
        EmailBodyFetcher emailBodyFetcher = new EmailBodyFetcher(mockService);
    }

    @Test
    @DisplayName("Text/Plain is the only mime type")
    void getEmailBodyTest1(){
        
    }
}