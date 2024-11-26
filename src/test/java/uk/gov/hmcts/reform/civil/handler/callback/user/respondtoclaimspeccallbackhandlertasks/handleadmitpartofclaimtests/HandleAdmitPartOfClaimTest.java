package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.HandleAdmitPartOfClaim;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.HandleAdmitPartOfClaimCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC;

@ExtendWith(MockitoExtension.class)
class HandleAdmitPartOfClaimTest {

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private PaymentDateValidator paymentDateValidator;

    @Mock
    private List<HandleAdmitPartOfClaimCaseUpdater> handleAdmitPartOfClaimCaseUpdaters;

    private HandleAdmitPartOfClaim handleAdmitPartOfClaim;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        handleAdmitPartOfClaim = new HandleAdmitPartOfClaim(
                objectMapper,
                toggleService,
                paymentDateValidator,
                handleAdmitPartOfClaimCaseUpdaters
        );
    }

    @Test
    void shouldReturnErrorResponseWhenPaymentDateIsInvalid() {
        CaseData caseData = CaseData.builder().build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        List<String> errors = Collections.singletonList("Invalid payment date");

        when(paymentDateValidator.validate(any(RespondToClaim.class))).thenReturn(errors);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handleAdmitPartOfClaim.execute(callbackParams);

        assertEquals(errors, response.getErrors());
        verify(paymentDateValidator).validate(any(RespondToClaim.class));
        verifyNoInteractions(handleAdmitPartOfClaimCaseUpdaters);
    }

    @Test
    void shouldUpdateCaseDataWhenPaymentDateIsValid() {
        CaseData caseData = CaseData.builder().totalClaimAmount(BigDecimal.valueOf(1000)).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        List<String> errors = Collections.emptyList();

        when(paymentDateValidator.validate(any(RespondToClaim.class))).thenReturn(errors);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handleAdmitPartOfClaim.execute(callbackParams);

        assertEquals(errors, response.getErrors() != null ? response.getErrors() : Collections.emptyList());
        verify(paymentDateValidator).validate(any(RespondToClaim.class));
        verify(handleAdmitPartOfClaimCaseUpdaters).forEach(any());
    }

    @Test
    void shouldUpdateResponseClaimTrackWhenEventIsDefendantResponseSpec() {
        CaseData caseData = CaseData.builder().totalClaimAmount(BigDecimal.valueOf(1000)).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        List<String> errors = Collections.emptyList();

        when(paymentDateValidator.validate(any(RespondToClaim.class))).thenReturn(errors);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handleAdmitPartOfClaim.execute(callbackParams);

        assertEquals(errors, response.getErrors() != null ? response.getErrors() : Collections.emptyList());
        verify(paymentDateValidator).validate(any(RespondToClaim.class));
        verify(handleAdmitPartOfClaimCaseUpdaters).forEach(any());
        assertEquals(AllocatedTrack.SMALL_CLAIM.name(), response.getData().get("responseClaimTrack"));
    }
}