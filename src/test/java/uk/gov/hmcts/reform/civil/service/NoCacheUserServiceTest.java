package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoCacheUserServiceTest {

    @InjectMocks
    private NoCacheUserService noCacheUserService;

    @Mock
    private IdamClient idamClient;

    private static final String SUB = "user-idam@reform.local";
    private static final String AUTHORISATION = "Bearer I am a valid token";
    private static final String PASSWORD = "User password";

    @Test
    void shouldReturnAccessToken_whenValidUserDetailsAreGiven() {
        when(idamClient.getAccessToken(SUB, PASSWORD)).thenReturn(AUTHORISATION);

        String accessToken = noCacheUserService.getAccessToken(SUB, PASSWORD);
        assertEquals(AUTHORISATION, accessToken);
    }
}
