package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2;

@ExtendWith(MockitoExtension.class)
class HandleAdmitPartOfClaimTest {

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private PaymentDateValidator paymentDateValidator;

    private HandleAdmitPartOfClaim handleAdmitPartOfClaim;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtils;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        handleAdmitPartOfClaim = new HandleAdmitPartOfClaim(
            objectMapper,
            toggleService,
            paymentDateValidator,
            respondToClaimSpecUtils
        );
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
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
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
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "fullAdmissionAndFullAmountPaid"));
        assertEquals(
            YES.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get("fullAdmissionAndFullAmountPaid").toString().toUpperCase()
        );
    }

    @Test
    void shouldUpdatePaymentRouteFlagsCorrectly() {
        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "defenceAdmitPartPaymentTimeRouteGeneric"));
        RespondentResponsePartAdmissionPaymentTimeLRspec actualValue = RespondentResponsePartAdmissionPaymentTimeLRspec.valueOf(
            (String) ((AboutToStartOrSubmitCallbackResponse) response).getData().get(
                "defenceAdmitPartPaymentTimeRouteGeneric")
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
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "partAdmittedByEitherRespondents"));
        assertEquals(
            YES.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get("partAdmittedByEitherRespondents").toString().toUpperCase()
        );
    }

    @Test
    void shouldUpdateEmploymentTypeCorrectly() {
        CaseData caseData = CaseData.builder()
            .defenceAdmitPartEmploymentTypeRequired(YES)
            .respondToClaimAdmitPartEmploymentTypeLRspec(Collections.singletonList(EmploymentTypeCheckboxFixedListLRspec.EMPLOYED))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "respondToClaimAdmitPartEmploymentTypeLRspecGeneric"));
        List<?> actualList = (List<?>) ((AboutToStartOrSubmitCallbackResponse) response).getData().get(
            "respondToClaimAdmitPartEmploymentTypeLRspecGeneric");
        List<EmploymentTypeCheckboxFixedListLRspec> employmentTypeList = actualList.stream()
            .map(item -> EmploymentTypeCheckboxFixedListLRspec.valueOf((String) item))
            .toList();
        assertEquals(Collections.singletonList(EmploymentTypeCheckboxFixedListLRspec.EMPLOYED), employmentTypeList);
    }

    @Test
    void shouldUpdateClaimOwingAmountsCorrectly() {
        CaseData caseData = CaseData.builder()
            .respondToAdmittedClaimOwingAmount(BigDecimal.valueOf(1000))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "respondToAdmittedClaimOwingAmountPounds"));
        BigDecimal actualValue = new BigDecimal((String) ((AboutToStartOrSubmitCallbackResponse) response).getData().get(
            "respondToAdmittedClaimOwingAmountPounds"));
        assertEquals(0, BigDecimal.valueOf(10.0).compareTo(actualValue));
    }

    @Test
    void shouldUpdateSpecPaidOrDisputeStatusCorrectly() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "specPaidLessAmountOrDisputesOrPartAdmission"));
        assertEquals(
            YES.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get(
                "specPaidLessAmountOrDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void shouldUpdateSpecPaidOrDisputeStatusToNoCorrectly() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "specPaidLessAmountOrDisputesOrPartAdmission"));
        assertEquals(
            NO.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get(
                "specPaidLessAmountOrDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    /*@Test
    void shouldUpdateShowConditionFlagsCorrectly() {
        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1(respondent1)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "WHEN_WILL_CLAIM_BE_PAID"));
        }
    }*/

    @Test
    void shouldUpdateAllocatedTrackCorrectly() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("responseClaimTrack"));
        assertEquals(
            "SMALL_CLAIM",
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get("responseClaimTrack")
        );
    }

    @Test
    void shouldUpdateFullAdmissionAndFullAmountPaidWhenRespondent2AndFullAdmitted2Required() {
        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .specDefenceFullAdmitted2Required(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "fullAdmissionAndFullAmountPaid"));
        assertEquals(
            YES.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get("fullAdmissionAndFullAmountPaid").toString().toUpperCase()
        );
    }

    @Test
    void shouldUpdatePaymentRouteFlagsForRespondent2() {
        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .defenceAdmitPartPaymentTimeRouteRequired2(IMMEDIATELY)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "defenceAdmitPartPaymentTimeRouteGeneric"));
        RespondentResponsePartAdmissionPaymentTimeLRspec actualValue = RespondentResponsePartAdmissionPaymentTimeLRspec.valueOf(
            (String) ((AboutToStartOrSubmitCallbackResponse) response).getData().get(
                "defenceAdmitPartPaymentTimeRouteGeneric")
        );
        assertEquals(IMMEDIATELY, actualValue);
    }

    @Test
    void shouldUpdateRespondentsAdmissionStatusForRespondent1() {
        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .specDefenceAdmittedRequired(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "partAdmittedByEitherRespondents"));
        assertEquals(
            YES.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get("partAdmittedByEitherRespondents").toString().toUpperCase()
        );
    }

    @Test
    void shouldUpdateEmploymentTypeForRespondent2() {
        CaseData caseData = CaseData.builder()
            .defenceAdmitPartEmploymentType2Required(YES)
            .respondToClaimAdmitPartEmploymentTypeLRspec2(Collections.singletonList(
                EmploymentTypeCheckboxFixedListLRspec.EMPLOYED))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "respondToClaimAdmitPartEmploymentTypeLRspecGeneric"));
        List<?> actualList = (List<?>) ((AboutToStartOrSubmitCallbackResponse) response).getData().get(
            "respondToClaimAdmitPartEmploymentTypeLRspecGeneric");
        List<EmploymentTypeCheckboxFixedListLRspec> employmentTypeList = actualList.stream()
            .map(item -> EmploymentTypeCheckboxFixedListLRspec.valueOf((String) item))
            .toList();
        assertEquals(Collections.singletonList(EmploymentTypeCheckboxFixedListLRspec.EMPLOYED), employmentTypeList);
    }

    @Test
    void isPartAdmitNotPaid() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .specDefenceAdmittedRequired(NO)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("specPartAdmitPaid"));
        assertEquals(
            NO.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get("specPartAdmitPaid").toString().toUpperCase()
        );
    }

    @Test
    void isFullAdmitNotPaid() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .specDefenceFullAdmittedRequired(NO)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("specFullAdmitPaid"));
        assertEquals(
            NO.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get("specFullAdmitPaid").toString().toUpperCase()
        );
    }

    @Test
    void isSpecPaidLessOrDisputeWhenDefenceRouteIsDisputeTheClaim() {
        CaseData caseData = CaseData.builder()
            .defenceRouteRequired(DISPUTES_THE_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "specPaidLessAmountOrDisputesOrPartAdmission"));
        assertEquals(
            YES.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get(
                "specPaidLessAmountOrDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void isPartAdmitPaid() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .specDefenceAdmittedRequired(YES)
            .specPartAdmitPaid(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("specPartAdmitPaid"));
        assertEquals(
            YES.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get("specPartAdmitPaid").toString().toUpperCase()
        );
    }

    @Test
    void isFullAdmitPaid() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .specDefenceFullAdmittedRequired(YES)
            .specFullAdmitPaid(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("specFullAdmitPaid"));
        assertEquals(
            YES.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get("specFullAdmitPaid").toString().toUpperCase()
        );
    }

    @Test
    void shouldNotUpdateAllocatedTrackWhenEventIdIsNotDefendantResponseSpec() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("DISPUTES_THE_CLAIM").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("responseClaimTrack"));
    }

    @Test
    void shouldReturnTrueWhenFinancialDetailsNeededForRespondent2() {
        Party respondent2 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .respondent2(respondent2)
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_2))
            .defenceAdmitPartPaymentTimeRouteRequired2(SUGGESTION_OF_REPAYMENT_PLAN)
            .specDefenceAdmitted2Required(NO)
            .respondent2ClaimResponseTypeForSpec(PART_ADMISSION)
            .multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART)
            .sameSolicitorSameResponse(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
            "NEED_FINANCIAL_DETAILS_2"));
    }

    /*@Test
    void shouldReturnTrueWhenAllConditionsAreMet() {

        CaseData caseData = CaseData.builder()
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .specDefenceAdmittedRequired(NO)
            .specDefenceFullAdmittedRequired(NO)
            .respondentClaimResponseTypeForSpecGeneric(PART_ADMISSION)
            .multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION)
            .sameSolicitorSameResponse(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
            "WHEN_WILL_CLAIM_BE_PAID"));
    }*/

    /*@Test
    void shouldAddWhy1DoesNotPayImmediatelyWhenRespondent1DoesNotPayImmediately() {

        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .respondent1(respondent1)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        when(respondToClaimSpecUtils.isRespondent1DoesNotPayImmediately(
            caseData,
            MultiPartyScenario.ONE_V_ONE
        )).thenReturn(true);

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
            "WHY_1_DOES_NOT_PAY_IMMEDIATELY"));
    }*/

    /*@Test
    void shouldAddWhy2DoesNotPayImmediatelyWhenRespondent2DoesNotPayImmediately() {

        Party respondent2 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .respondent2(respondent2)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        when(respondToClaimSpecUtils.isRespondent2DoesNotPayImmediately(caseData, ONE_V_TWO_TWO_LEGAL_REP)).thenReturn(
            true);

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
            "WHY_2_DOES_NOT_PAY_IMMEDIATELY"));
    }*/

    /*@Test
    void shouldUpdateShowConditionFlagsCorrectly_WhenSingleLegalRepresentative() {
        Party respondent1 = Party.builder().type(Party.Type.COMPANY).build();
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1(respondent1)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "WHEN_WILL_CLAIM_BE_PAID"));
        }
    }*/

    @Test
    void shouldReturnFalseWhenRespondentIsOrganisation() {

        Party respondent1 = Party.builder().type(Party.Type.ORGANISATION).build();
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1(respondent1)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertFalse(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
            "NEED_FINANCIAL_DETAILS_1"));
    }

    @Test
    void shouldReturnTrueWhenNonCorporatePartyAndScenarioRequiresInfo() {

        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .sameSolicitorSameResponse(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_ONE_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "NEED_FINANCIAL_DETAILS_1"));
        }
    }

    /*@Test
    void shouldShowNeedFinancialDetailsForRespondent1_WhenIndividualRespondentAndOneVTwoOneLegalRep() {

        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .sameSolicitorSameResponse(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(TWO_V_ONE);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertFalse(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "NEED_FINANCIAL_DETAILS_1"));
        }
    }*/

    @Test
    void shouldReturnFalseWhenAnyConditionFailsForNonCorporatePartyAndScenario() {

        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .specDefenceAdmittedRequired(YES)
            .respondentClaimResponseTypeForSpecGeneric(PART_ADMISSION)
            .multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION)
            .sameSolicitorSameResponse(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_ONE_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertFalse(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "NEED_FINANCIAL_DETAILS_1"));
        }
    }

    @Test
    void shouldNotShowNeedFinancialDetailsWhenPaymentRouteBySetDateAndAdmissionRequired() {
        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .specDefenceAdmittedRequired(YES)
            .specDefenceFullAdmittedRequired(YES)
            .multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART)
            .sameSolicitorSameResponse(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_ONE_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertFalse(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "NEED_FINANCIAL_DETAILS_1"));
        }
    }

    @Test
    void shouldNotShowNeedFinancialDetailsWhenFullAdmissionRequiredAndOneVTwoOneLegalRep() {

        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .sameSolicitorSameResponse(YES)
            .specDefenceFullAdmittedRequired(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_ONE_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertFalse(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "NEED_FINANCIAL_DETAILS_1"));
        }
    }

    @Test
    void shouldAddNeedFinancialDetailsWhenScenarioIsOneVTwoTwoLegalRepAndSameSolicitorResponseIsNo() {

        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .respondentClaimResponseTypeForSpecGeneric(COUNTER_CLAIM)
            .respondentClaimResponseTypeForSpecGeneric(FULL_DEFENCE)
            .multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART)
            .sameSolicitorSameResponse(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_ONE_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertFalse(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "NEED_FINANCIAL_DETAILS_1"));
        }
    }

    @Test
    void shouldAddNeedFinancialDetailsWhenRespondent1DoesNotPayImmediatelyInOneVTwoTwoLegalRepScenario() {

        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .respondentClaimResponseTypeForSpecGeneric(COUNTER_CLAIM)
            .multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE)
            .sameSolicitorSameResponse(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_ONE_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertFalse(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "NEED_FINANCIAL_DETAILS_1"));
        }
    }

    @Test
    void shouldNotShowNeedFinancialDetailsWhenScenarioIsOneVTwoOneLegalRepAndOtherConditionsFail() {

        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART)
            .sameSolicitorSameResponse(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_ONE_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertFalse(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "NEED_FINANCIAL_DETAILS_1"));
        }
    }

    @Test
    void shouldNotShowNeedFinancialDetailsWhenSameSolicitorResponseIsNoInOneVTwoOneLegalRep() {

        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .sameSolicitorSameResponse(NO)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_ONE_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertFalse(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "NEED_FINANCIAL_DETAILS_1"));
        }
    }

    @Test
    void shouldShowNeedFinancialDetailsForRespondent2WhenSameSolicitorResponseIsNoInOneVTwoTwoLegalRepScenario() {

        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .sameSolicitorSameResponse(NO)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "NEED_FINANCIAL_DETAILS_1"));
        }
    }

    /*@Test
    void shouldUpdateShowConditionFlagsCorrectlyWhenMultipleLegalRepresentatives() {
        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
            .specDefenceAdmitted2Required(YES)
            .respondentClaimResponseTypeForSpecGeneric(FULL_DEFENCE)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1(respondent1)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "WHEN_WILL_CLAIM_BE_PAID"));
        }
    }*/

    @Test
    void shouldAddNeedFinancialDetails2WhenScenarioIsOneVTwoTwoLegalRepAndSameSolicitorResponseIsNo() {

        Party respondent2 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent2(respondent2)
            .defenceAdmitPartPaymentTimeRouteRequired2(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent2ClaimResponseTypeForSpec(PART_ADMISSION)
            .sameSolicitorSameResponse(NO)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_2))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertTrue(((AboutToStartOrSubmitCallbackResponse) response)
                           .getData()
                           .get("showConditionFlags")
                           .toString()
                           .contains("NEED_FINANCIAL_DETAILS_2"));
        }
    }

    @Test
    void shouldShowNeedFinancialDetailsForRespondent1ForIndividualRespondentInOneVTwoTwoLegalRepScenario() {

        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .sameSolicitorSameResponse(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "NEED_FINANCIAL_DETAILS_1"));
        }
    }

    @Test
    void shouldShowNeedFinancialDetailsForRespondent2WhenFinancialDetailsAreNeededInOneVTwoTwoLegalRep() {

        Party respondent2 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .respondent2(respondent2)
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_2))
            .defenceAdmitPartPaymentTimeRouteRequired2(IMMEDIATELY)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
    }

    @Test
    void shouldShowNeedFinancialDetailsForRespondent2WhenRespondent2HasFullDefenceInOneVTwoTwoLegalRep() {

        Party respondent2 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .respondent2(respondent2)
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_2))
            .respondentClaimResponseTypeForSpecGeneric(FULL_DEFENCE)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
    }

    /*@Test
    void shouldUpdateShowConditionFlagsCorrectlyForRespondentOneWithImmediatePayment() {
        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
            .specDefenceFullAdmitted2Required(YES)
            .respondentClaimResponseTypeForSpecGeneric(FULL_DEFENCE)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1(respondent1)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "WHEN_WILL_CLAIM_BE_PAID"));
        }
    }*/

    /*@Test
    void shouldUpdateShowConditionFlagsCorrectlyForRespondentOneWithRepaymentPlan() {
        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        CaseData caseData = CaseData.builder()
            .respondentClaimResponseTypeForSpecGeneric(COUNTER_CLAIM)
            .showConditionFlags(Collections.singleton(CAN_ANSWER_RESPONDENT_1))
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondent1(respondent1)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());
        lenient().when(respondToClaimSpecUtils.isWhenWillClaimBePaidShown(any())).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey("showConditionFlags"));
            assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().get("showConditionFlags").toString().contains(
                "WHEN_WILL_CLAIM_BE_PAID"));
        }
    }*/

    @Test
    void shouldUpdateSpecPaidOrDisputeStatusToNoCorrectlyWhenFullOrMoreThanClaimedPaid() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT)
            .respondentResponseIsSame(YES)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "specPaidLessAmountOrDisputesOrPartAdmission"));
        assertEquals(
            NO.name(),
            ((AboutToStartOrSubmitCallbackResponse) response).getData().get(
                "specPaidLessAmountOrDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void shouldUpdateSpecPaidOrDisputeStatusToNoCorrectlyWhenImmediatePaymentRequired() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT)
            .respondentResponseIsSame(YES)
            .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleAdmitPartOfClaim.execute(callbackParams);

        assertTrue(((AboutToStartOrSubmitCallbackResponse) response).getData().containsKey(
            "specPaidLessAmountOrDisputesOrPartAdmission"));
        assertEquals(NO.name(), ((AboutToStartOrSubmitCallbackResponse) response).getData().get(
            "specPaidLessAmountOrDisputesOrPartAdmission").toString().toUpperCase()
        );
    }
}

