package uk.gov.hmcts.reform.civil.controllers.fees;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.exceptions.InternalServerErrorException;
import uk.gov.hmcts.reform.civil.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.civil.exceptions.UpstreamUnavailableException;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.service.AuthorisationService;
import uk.gov.hmcts.reform.civil.service.PaymentRequestUpdateCallbackService;

import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceRequestUpdateCallbackControllerTest {

    private static final String S2S_TOKEN = "s2s-token";
    private static final String CASE_ID = "1234";

    @Mock
    private PaymentRequestUpdateCallbackService requestUpdateCallbackService;

    @Mock
    private AuthorisationService authorisationService;

    private ServiceRequestUpdateCallbackController controller;

    @BeforeEach
    void setUp() {
        controller = new ServiceRequestUpdateCallbackController(requestUpdateCallbackService, authorisationService);
    }

    @Test
    void shouldProcessHearingPaymentCallback_whenServiceIsAuthorised() {
        when(authorisationService.isServiceAuthorized(S2S_TOKEN)).thenReturn(true);

        controller.serviceRequestUpdate(S2S_TOKEN, serviceRequestUpdateDto());

        verify(requestUpdateCallbackService).processCallback(serviceRequestUpdateDto(), FeeType.HEARING.name());
    }

    @Test
    void shouldThrowInvalidTokenException_whenServiceIsNotAuthorised() {
        ServiceRequestUpdateDto serviceRequestUpdateDto = serviceRequestUpdateDto();
        when(authorisationService.isServiceAuthorized(S2S_TOKEN)).thenReturn(false);

        assertThatThrownBy(() -> controller.serviceRequestUpdate(S2S_TOKEN, serviceRequestUpdateDto))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessage("Invalid S2S token");

        verify(requestUpdateCallbackService, never()).processCallback(any(), any());
    }

    @Test
    void shouldThrowUpstreamUnavailableException_whenDownstreamIsUnavailable() {
        ServiceRequestUpdateDto serviceRequestUpdateDto = serviceRequestUpdateDto();
        FeignException gatewayTimeout = new FeignException.GatewayTimeout(
            "Gateway Timeout",
            request(),
            new byte[]{},
            Map.of()
        );
        when(authorisationService.isServiceAuthorized(S2S_TOKEN)).thenReturn(true);
        doThrow(gatewayTimeout).when(requestUpdateCallbackService).processCallback(any(), any());

        assertThatThrownBy(() -> controller.serviceRequestUpdate(S2S_TOKEN, serviceRequestUpdateDto))
            .isInstanceOf(UpstreamUnavailableException.class)
            .hasCause(gatewayTimeout);
    }

    @Test
    void shouldThrowInternalServerErrorException_whenUnexpectedErrorOccurs() {
        ServiceRequestUpdateDto serviceRequestUpdateDto = serviceRequestUpdateDto();
        RuntimeException unexpectedError = new RuntimeException("Unexpected error");
        when(authorisationService.isServiceAuthorized(S2S_TOKEN)).thenReturn(true);
        doThrow(unexpectedError).when(requestUpdateCallbackService).processCallback(any(), any());

        assertThatThrownBy(() -> controller.serviceRequestUpdate(S2S_TOKEN, serviceRequestUpdateDto))
            .isInstanceOf(InternalServerErrorException.class)
            .hasCause(unexpectedError);
    }

    private ServiceRequestUpdateDto serviceRequestUpdateDto() {
        return new ServiceRequestUpdateDto().setCcdCaseNumber(CASE_ID);
    }

    private Request request() {
        return Request.create(GET, "url", Map.of(), null, null, null);
    }
}
