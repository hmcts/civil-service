package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1;

@ExtendWith(MockitoExtension.class)
public class HandleAdmitPartOfClaimTest {

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private PaymentDateValidator paymentDateValidator;

    private HandleAdmitPartOfClaim handleAdmitPartOfClaim;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtilsDisputeDetails;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        handleAdmitPartOfClaim = new HandleAdmitPartOfClaim(objectMapper, toggleService, paymentDateValidator, respondToClaimSpecUtilsDisputeDetails );
    }

    @Test
    void shouldReturnErrorResponseWhenPaymentsAreInvalid() {
        CallbackParams callbackParams = CallbackParams.builder().caseData(CaseData.builder().build()).build();
        when(paymentDateValidator.validate(any())).thenReturn(List.of("Error"));

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

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

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertEquals(AboutToStartOrSubmitCallbackResponse.class, response.getClass());
        assertNull(((AboutToStartOrSubmitCallbackResponse) response).getErrors());
    }

    @Test
    void shouldUpdateAdmissionFlagsCorrectly() {
        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .specDefenceFullAdmittedRequired(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("DEFENDANT_RESPONSE_SPEC").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("fullAdmissionAndFullAmountPaid"));
        assertEquals(YES.name(), ((AboutToStartOrSubmitCallbackResponse) response).getData().get("fullAdmissionAndFullAmountPaid").toString().toUpperCase());
    }

    @Test
    void shouldUpdatePaymentRouteFlagsCorrectly() {
        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("DEFENDANT_RESPONSE_SPEC").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("defenceAdmitPartPaymentTimeRouteGeneric"));
        RespondentResponsePartAdmissionPaymentTimeLRspec actualValue = RespondentResponsePartAdmissionPaymentTimeLRspec.valueOf(
            (String) ((AboutToStartOrSubmitCallbackResponse) response).getData().get("defenceAdmitPartPaymentTimeRouteGeneric")
        );
        assertEquals(IMMEDIATELY, actualValue);
    }

    @Test
    void shouldUpdateRespondentsAdmissionStatusCorrectly() {
        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .specDefenceAdmitted2Required(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("DEFENDANT_RESPONSE_SPEC").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("partAdmittedByEitherRespondents"));
        assertEquals(YES.name(), ((AboutToStartOrSubmitCallbackResponse) response).getData().get("partAdmittedByEitherRespondents").toString().toUpperCase());
    }

    @Test
    void shouldUpdateEmploymentTypeCorrectly() {
        CaseData caseData = CaseData.builder()
            .defenceAdmitPartEmploymentTypeRequired(YES)
            .respondToClaimAdmitPartEmploymentTypeLRspec(Collections.singletonList(EmploymentTypeCheckboxFixedListLRspec.EMPLOYED))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("DEFENDANT_RESPONSE_SPEC").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("respondToClaimAdmitPartEmploymentTypeLRspecGeneric"));
        List<?> actualList = (List<?>) ((AboutToStartOrSubmitCallbackResponse) response).getData().get("respondToClaimAdmitPartEmploymentTypeLRspecGeneric");
        List<EmploymentTypeCheckboxFixedListLRspec> employmentTypeList = actualList.stream()
            .map(item -> EmploymentTypeCheckboxFixedListLRspec.valueOf((String) item))
            .collect(Collectors.toList());
        assertEquals(Collections.singletonList(EmploymentTypeCheckboxFixedListLRspec.EMPLOYED), employmentTypeList);
    }

    @Test
    void shouldUpdateClaimOwingAmountsCorrectly() {
        CaseData caseData = CaseData.builder()
            .respondToAdmittedClaimOwingAmount(BigDecimal.valueOf(1000))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("DEFENDANT_RESPONSE_SPEC").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("respondToAdmittedClaimOwingAmountPounds"));
        BigDecimal actualValue = new BigDecimal((String) ((AboutToStartOrSubmitCallbackResponse) response).getData().get("respondToAdmittedClaimOwingAmountPounds"));
        assertEquals(0, BigDecimal.valueOf(10.0).compareTo(actualValue));
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

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("specPaidLessAmountOrDisputesOrPartAdmission"));
        assertEquals(YES.name(), ((AboutToStartOrSubmitCallbackResponse) response).getData().get("specPaidLessAmountOrDisputesOrPartAdmission").toString().toUpperCase());
    }

    @Test
    void shouldUpdateSpecPaidOrDisputeStatusToNoCorrectly() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("DEFENDANT_RESPONSE_SPEC").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("specPaidLessAmountOrDisputesOrPartAdmission"));
        assertEquals(NO.name(), ((AboutToStartOrSubmitCallbackResponse) response).getData().get("specPaidLessAmountOrDisputesOrPartAdmission").toString().toUpperCase());
    }

    @Test
    void shouldUpdateShowConditionFlagsCorrectly() {
        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1(respondent1)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("DEFENDANT_RESPONSE_SPEC").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        when(respondToClaimSpecUtilsDisputeDetails.mustWhenWillClaimBePaidBeShown(any())).thenReturn(true);

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains("WHEN_WILL_CLAIM_BE_PAID"));
    }

    @Test
    void shouldUpdateAllocatedTrackCorrectly() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("DEFENDANT_RESPONSE_SPEC").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("responseClaimTrack"));
        assertEquals("SMALL_CLAIM", ((AboutToStartOrSubmitCallbackResponse) response).getData().get("responseClaimTrack"));
    }
}
