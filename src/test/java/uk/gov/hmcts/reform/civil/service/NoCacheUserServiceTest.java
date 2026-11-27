package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503, 504})
    void shouldRetryServerErrorsForBundleTokens(int status) {
        FeignException failure = mock(FeignException.class);
        when(failure.status()).thenReturn(status);
        when(idamClient.getAccessToken(SUB, PASSWORD)).thenThrow(failure);

        assertThat(assertThrows(FeignException.class, () -> noCacheUserService.getAccessToken(SUB, PASSWORD)))
            .isSameAs(failure);
        verify(idamClient, times(3)).getAccessToken(SUB, PASSWORD);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404, 429})
    void shouldNotRetryClientErrorsForBundleTokens(int status) {
        FeignException failure = mock(FeignException.class);
        when(failure.status()).thenReturn(status);
        when(idamClient.getAccessToken(SUB, PASSWORD)).thenThrow(failure);

        assertThat(assertThrows(FeignException.class, () -> noCacheUserService.getAccessToken(SUB, PASSWORD)))
            .isSameAs(failure);
        verify(idamClient).getAccessToken(SUB, PASSWORD);
    }

    @Test
    void shouldRetryAndStillFetchFreshBundleTokensWithCachingEnabled() {
        FeignException failure = mock(FeignException.class);
        when(failure.status()).thenReturn(502);
        when(idamClient.getAccessToken(SUB, PASSWORD))
            .thenThrow(failure).thenReturn("first-token", "second-token");
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(UserServiceTest.TokenCacheConfiguration.class);
            context.registerBean("cacheManager", ConcurrentMapCacheManager.class,
                                 () -> new ConcurrentMapCacheManager("accessTokenCache"));
            context.registerBean(NoCacheUserService.class, () -> new NoCacheUserService(idamClient));
            context.refresh();
            NoCacheUserService service = context.getBean(NoCacheUserService.class);

            assertThat(service.getAccessToken(SUB, PASSWORD)).isEqualTo("first-token");
            assertThat(service.getAccessToken(SUB, PASSWORD)).isEqualTo("second-token");
            verify(idamClient, times(3)).getAccessToken(SUB, PASSWORD);
        }
    }
}
