package uk.gov.hmcts.reform.civil.service;

import feign.Feign;
import feign.RequestLine;
import feign.RetryableException;
import feign.Retryer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessTokenTransportRetryTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldNotMultiplyFeignTimeoutAttempts(boolean uncached) {
        AtomicInteger calls = new AtomicInteger();
        // Keep Feign's five attempts, with a shorter delay so the test needs no real timeout.
        TokenApi tokenApi = Feign.builder()
            .retryer(new Retryer.Default(1L, 1L, 5))
            .client((request, options) -> {
                calls.incrementAndGet();
                throw new SocketTimeoutException("Read timed out");
            })
            .target(TokenApi.class, "http://idam.test");
        IdamClient idamClient = mock(IdamClient.class);
        when(idamClient.getAccessToken("user", "password")).thenAnswer(invocation -> tokenApi.fetch());
        UserService service = uncached ? new NoCacheUserService(idamClient) : new UserService(idamClient);

        assertThrows(RetryableException.class, () -> service.getAccessToken("user", "password"));

        assertThat(calls.get()).isEqualTo(5);
    }

    interface TokenApi {
        @RequestLine("POST /token")
        String fetch();
    }
}
