package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.service.UserService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GaCoreCaseDataServiceNullQueryTest {

    @Mock
    private SystemUpdateUserConfiguration userConfig;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private UserService userService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private GaCoreCaseDataService service;

    @Test
    void shouldThrowNullPointerExceptionWithoutCallingCcdWhenQueryIsNull() {
        when(authTokenGenerator.generate()).thenReturn("Bearer service-xyz");

        assertThatThrownBy(() -> service.searchGeneralApplication(null, "Bearer user-xyz"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("query must not be null");

        verify(authTokenGenerator).generate();
        verifyNoInteractions(coreCaseDataApi);
    }

    @Test
    void shouldRetryOnceAndThenThrowNullPointerExceptionWhenQueryIsNull() {
        when(userService.getAccessToken(any(), any())).thenReturn("Bearer user-xyz");
        when(authTokenGenerator.generate()).thenReturn("Bearer service-xyz");

        assertThatThrownBy(() -> service.searchGeneralApplication(null))
            .isInstanceOf(NullPointerException.class);

        verify(userService, times(2)).getAccessToken(any(), any());
        verify(authTokenGenerator, times(2)).generate();
        verifyNoInteractions(coreCaseDataApi);
    }
}
