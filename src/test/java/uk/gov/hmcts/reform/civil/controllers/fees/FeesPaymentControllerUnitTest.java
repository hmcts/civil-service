package uk.gov.hmcts.reform.civil.controllers.fees;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.ga.service.GaFeesPaymentService;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.service.FeesPaymentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeesPaymentControllerUnitTest {

    private static final String AUTH = "Bearer token";
    private static final String CASE_REFERENCE = "123";
    private static final String PAYMENT_REFERENCE = "RC-1701-0909-0602-0418";

    @Mock
    private FeesPaymentService feesPaymentService;

    @Mock
    private GaFeesPaymentService gaFeesPaymentService;

    @InjectMocks
    private FeesPaymentController controller;

    @ParameterizedTest
    @ValueSource(strings = {"undefined", "null", "UNDEFINED", "NULL", " ", ""})
    void shouldRejectUnusablePaymentReferenceForFeeStatus(String paymentReference) {
        assertThatThrownBy(() ->
            controller.getGovPaymentRequestStatus(AUTH, FeeType.HEARING, CASE_REFERENCE, paymentReference)
        )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(feesPaymentService, gaFeesPaymentService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"undefined", "null", "UNDEFINED", "NULL", " ", ""})
    void shouldRejectUnusablePaymentReferenceForGaStatus(String paymentReference) {
        assertThatThrownBy(() ->
            controller.getGaGovPaymentRequestStatus(AUTH, CASE_REFERENCE, paymentReference)
        )
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(feesPaymentService, gaFeesPaymentService);
    }

    @Test
    void shouldDelegateValidPaymentReferenceForFeeStatus() {
        CardPaymentStatusResponse response = new CardPaymentStatusResponse().setStatus("Success");
        when(feesPaymentService.getGovPaymentRequestStatus(
            FeeType.HEARING, CASE_REFERENCE, PAYMENT_REFERENCE, AUTH
        )).thenReturn(response);

        assertThat(controller.getGovPaymentRequestStatus(
            AUTH, FeeType.HEARING, CASE_REFERENCE, PAYMENT_REFERENCE
        ).getBody()).isEqualTo(response);

        verify(feesPaymentService).getGovPaymentRequestStatus(
            FeeType.HEARING, CASE_REFERENCE, PAYMENT_REFERENCE, AUTH
        );
        verifyNoInteractions(gaFeesPaymentService);
    }

    @Test
    void shouldDelegateValidPaymentReferenceForGaStatus() {
        CardPaymentStatusResponse response = new CardPaymentStatusResponse().setStatus("Success");
        when(gaFeesPaymentService.getGovPaymentRequestStatus(CASE_REFERENCE, PAYMENT_REFERENCE, AUTH))
            .thenReturn(response);

        assertThat(controller.getGaGovPaymentRequestStatus(AUTH, CASE_REFERENCE, PAYMENT_REFERENCE).getBody())
            .isEqualTo(response);

        verify(gaFeesPaymentService).getGovPaymentRequestStatus(CASE_REFERENCE, PAYMENT_REFERENCE, AUTH);
        verifyNoInteractions(feesPaymentService);
    }
}
