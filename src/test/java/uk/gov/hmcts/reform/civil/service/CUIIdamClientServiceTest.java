package uk.gov.hmcts.reform.civil.service;

import feign.Request;
import feign.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.CMCPinVerifyConfiguration;
import uk.gov.hmcts.reform.civil.service.pininpost.CUIIdamClientService;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CUIIdamClientServiceTest {

    private CUIIdamClientService cuiIdamClientService;

    @Mock
    private IdamApi idamApi;

    @Mock
    CMCPinVerifyConfiguration cmcPinVerifyConfiguration;
    @Mock
    Request request;


    @BeforeEach
    public void setup() {
        cuiIdamClientService = new CUIIdamClientService(idamApi, cmcPinVerifyConfiguration);
    }

    @Test
    void ShouldAuthenticatePinUser() {
        when(cmcPinVerifyConfiguration.getRedirectUrl()).thenReturn("dummy_redirect_url");
        when(cmcPinVerifyConfiguration.getClientId()).thenReturn("dummy_client_id");
        when(idamApi.authenticatePinUser(anyString(), anyString(), anyString(), anyString())).thenReturn(Response.builder().request(request).status(
            HttpStatus.SC_OK).build());

        int response = cuiIdamClientService.authenticatePinUser("12345678", "000MC001");
        assertThat(response).isEqualTo(HttpStatus.SC_OK);
    }

}
