package uk.gov.hmcts.reform.civil.controllers.fees;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.exceptions.InternalServerErrorException;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.service.AuthorisationService;
import uk.gov.hmcts.reform.civil.service.PaymentRequestUpdateCallbackService;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceRequestUpdateCallbackControllerUnitTest {

    private static final String S2S_TOKEN = "s2s-token";

    @Mock
    private PaymentRequestUpdateCallbackService requestUpdateCallbackService;

    @Mock
    private AuthorisationService authorisationService;

    @InjectMocks
    private ServiceRequestUpdateCallbackController controller;

    @Test
    void shouldProcessAuthorisedServiceRequestUpdate() {
        ServiceRequestUpdateDto request = request();
        when(authorisationService.isPaymentCallbackServiceAuthorized(S2S_TOKEN)).thenReturn(true);

        assertThatCode(() -> controller.serviceRequestUpdate(S2S_TOKEN, request))
            .doesNotThrowAnyException();

        verify(requestUpdateCallbackService).processCallback(request, FeeType.HEARING.name());
    }

    @Test
    void shouldThrowInternalServerErrorWhenServiceIsNotAuthorised() {
        ServiceRequestUpdateDto request = request();
        when(authorisationService.isPaymentCallbackServiceAuthorized(S2S_TOKEN)).thenReturn(false);

        assertThatThrownBy(() -> controller.serviceRequestUpdate(S2S_TOKEN, request))
            .isInstanceOf(InternalServerErrorException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .cause()
            .hasMessage("Invalid Client");

        verify(requestUpdateCallbackService, never()).processCallback(request, FeeType.HEARING.name());
    }

    @Test
    void shouldWrapExceptionThrownWhileProcessingCallback() {
        ServiceRequestUpdateDto request = request();
        RuntimeException exception = new RuntimeException("Callback failed");
        when(authorisationService.isPaymentCallbackServiceAuthorized(S2S_TOKEN)).thenReturn(true);
        doThrow(exception).when(requestUpdateCallbackService)
            .processCallback(request, FeeType.HEARING.name());

        assertThatThrownBy(() -> controller.serviceRequestUpdate(S2S_TOKEN, request))
            .isInstanceOf(InternalServerErrorException.class)
            .hasCause(exception);
    }

    private ServiceRequestUpdateDto request() {
        return new ServiceRequestUpdateDto().setCcdCaseNumber("1234");
    }
}
