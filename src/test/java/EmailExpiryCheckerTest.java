import org.junit.jupiter.api.*;

class EmailExpiryCheckerTest {
    private EmailExpiryChecker emailExpiryChecker;
    private String emailBody;

    @BeforeEach
    public void setUp(){
        emailExpiryChecker = new EmailExpiryChecker();
        emailBody = "";
    }

    @Test
    @DisplayName("mdyDateFormat")
    public void checkExpiryTest1() {
        emailBody = "Discount offer begins 25/10/2024 at 12:00 a.m. CT and ends 21/11/2024 at 11:59 p.m. CT online";
        boolean result = emailExpiryChecker.checkExpiry(emailBody);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("dmyDateFormat")
    public void checkExpiryTest2() {
        emailBody = "***20% offer valid for Rouge members 29/10/24 - 15/11/24 at 11:59pm";
        boolean result = emailExpiryChecker.checkExpiry(emailBody);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("shortMonthDateFormat")
    public void checkExpiryTest3() {
        emailBody = "Valid in-store and online between Nov 22, 2024 and Nov 23, 2024 ";
        boolean result = emailExpiryChecker.checkExpiry(emailBody);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("longMonthDateFormat")
    public void checkExpiryTest4() {
        emailBody = "OFFER IS VALID IN-STORE AND ONLINE for a limited time," +
                " from Friday, November 29, 2024, 7:00 AM EST, until Saturday, November 30, 2024, 7:00 AM EST.";
        boolean result = emailExpiryChecker.checkExpiry(emailBody);
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("shortMonthDayDateFormat")
    public void checkExpiryTest5() {
        emailBody = "Offer ends Friday, Sep 5 at midnight.";
        boolean result = emailExpiryChecker.checkExpiry(emailBody);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("longMonthDayDateFormat")
    public void checkExpiryTest6() {
        emailBody = "Offer ends Friday, November 28 at 11:59PM PT";
        boolean result = emailExpiryChecker.checkExpiry(emailBody);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("2 different date formats")
    public void checkExpiryTest7() {
        emailBody = "Offers and pricing listed in this email are valid Friday November 29 – Sunday, December 1, 2024";
        boolean result = emailExpiryChecker.checkExpiry(emailBody);
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("No date formats found")
    public void checkExpiryTest8() {
        emailBody = "Offers and pricing listed in this email are valid until tomorrow";
        boolean result = emailExpiryChecker.checkExpiry(emailBody);
        Assertions.assertFalse(result);
    }
}