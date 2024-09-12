package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class HandleDefendAllClaimTest {

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private PaymentDateValidator paymentDateValidator;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtilsDisputeDetails;

    @InjectMocks
    private HandleDefendAllClaim handleDefendAllClaim;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        handleDefendAllClaim = new HandleDefendAllClaim(objectMapper, toggleService, paymentDateValidator, respondToClaimSpecUtilsDisputeDetails);
    }

    @Test
    void shouldReturnErrorResponseWhenPaymentsAreInvalid() {
        CaseData caseData = CaseData.builder().build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        when(paymentDateValidator.validate(any())).thenReturn(List.of("Error"));

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        assertEquals(AboutToStartOrSubmitCallbackResponse.class, response.getClass());
        assertEquals(Collections.singletonList("Error"), ((AboutToStartOrSubmitCallbackResponse) response).getErrors());
    }

    @Test
    void shouldReturnSuccessResponseWhenPaymentsAreValid() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("DEFENDANT_RESPONSE_SPEC").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        assertEquals(AboutToStartOrSubmitCallbackResponse.class, response.getClass());
        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getErrors() == null || ((AboutToStartOrSubmitCallbackResponse) response).getErrors().isEmpty());
    }

    @Test
    void shouldUpdateSpecPaidOrDisputeStatusCorrectly() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("DEFENDANT_RESPONSE_SPEC").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("specPaidLessAmountOrDisputesOrPartAdmission"));
        assertEquals(YES.name(), ((AboutToStartOrSubmitCallbackResponse) response).getData().get("specPaidLessAmountOrDisputesOrPartAdmission").toString().toUpperCase());
    }
}
