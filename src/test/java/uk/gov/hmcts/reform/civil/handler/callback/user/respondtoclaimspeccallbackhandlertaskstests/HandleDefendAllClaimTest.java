package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC;

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
        handleDefendAllClaim = new HandleDefendAllClaim(
            objectMapper,
            toggleService,
            paymentDateValidator,
            respondToClaimSpecUtilsDisputeDetails
        );
    }

    @Test
    void shouldReturnErrorResponseWhenPaymentsAreInvalid() {
        CaseData caseData = CaseData.builder().build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        when(paymentDateValidator.validate(any())).thenReturn(List.of("Error"));

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertEquals(List.of("Error"), callbackResponse.getErrors());
    }

    @Test
    void shouldReturnSuccessResponseWhenPaymentsAreValid() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getErrors() == null || callbackResponse.getErrors().isEmpty());
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

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specPaidLessAmountOrDisputesOrPartAdmission"));
        assertEquals(
            YesOrNo.YES.name(),
            callbackResponse.getData().get("specPaidLessAmountOrDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void shouldSetSpecDisputesOrPartAdmissionWhenConditionMet() {
        CaseData caseData = CaseData.builder()
            .isRespondent1(YesOrNo.YES)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
        assertEquals(
            YesOrNo.YES.name(),
            callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void shouldReturnTrueWhenResponseTypeIsPartAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
        assertEquals(
            YesOrNo.YES.name(),
            callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void shouldReturnTrueWhenRespondent2ResponseTypeIsPartAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .isRespondent2(YesOrNo.YES)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
        assertEquals(
            YesOrNo.YES.name(),
            callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void shouldSetSpecDisputesOrPartAdmissionWhenDefenceRouteRequired2IsDispute() {
        CaseData caseData = CaseData.builder()
            .isRespondent2(YesOrNo.YES)
            .defenceRouteRequired2("DISPUTES_THE_CLAIM")
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
        assertEquals(
            YesOrNo.YES.name(),
            callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void shouldSetSpecDisputesOrPartAdmissionWhenDefenceRouteRequired2IsDispute1() {
        CaseData caseData = CaseData.builder()
            .defendantSingleResponseToBothClaimants(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .defenceRouteRequired2("DISPUTES_THE_CLAIM")
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();
        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
        assertEquals(
            YesOrNo.YES.name(),
            callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void shouldHandleTwoVOneWithSeparateResponses() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .defendantSingleResponseToBothClaimants(YesOrNo.NO)
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired2("DISPUTES_THE_CLAIM")
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
        assertEquals(
            YesOrNo.YES.name(),
            callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void shouldHandleTwoVOneWithSingleResponse() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .defendantSingleResponseToBothClaimants(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
        assertEquals(
            YesOrNo.YES.name(),
            callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void shouldAddTagWhenFullDefenceAndPaidLess() {
        CaseData caseData = CaseData.builder()
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .respondToClaim(RespondToClaim.builder().howMuchWasPaid(BigDecimal.valueOf(500)).build())
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
        assertEquals(
            YesOrNo.YES.name(),
            callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
        );
    }

    @Test
    void shouldHandleTwoVOneScenarioCorrectly() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .defendantSingleResponseToBothClaimants(YesOrNo.NO)
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired2("DISPUTES_THE_CLAIM")
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(MultiPartyScenario.TWO_V_ONE);

            CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

            AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
            assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
            assertEquals(
                YesOrNo.YES.name(),
                callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
            );
        }
    }

    @Test
    void shouldHandleOneVOneScenarioWhenDefendantSingleResponseToBothClaimantsIsYes() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .defendantSingleResponseToBothClaimants(YesOrNo.YES)
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(MultiPartyScenario.TWO_V_ONE);

            CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

            AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
            assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
            assertEquals(
                YesOrNo.YES.name(),
                callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
            );
        }
    }

    @Test
    void shouldHandleOneVTwoOneLegalRepScenarioCorrectly() {
        CaseData caseData = CaseData.builder()
            .respondentResponseIsSame(YesOrNo.YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .defendantSingleResponseToBothClaimants(YesOrNo.NO)
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceRouteRequired2("DISPUTES_THE_CLAIM")
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP);

            CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

            AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
            assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
            assertEquals(
                YesOrNo.YES.name(),
                callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
            );
        }
    }

    @Test
    void shouldHandleOneVTwoTwoLegalRepScenarioCorrectly() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .defendantSingleResponseToBothClaimants(YesOrNo.NO)
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceRouteRequired2("DISPUTES_THE_CLAIM")
            .showConditionFlags(Collections.singleton(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

            AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
            assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
            assertEquals(
                YesOrNo.YES.name(),
                callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
            );
        }
    }

    @Test
    void shouldHandleOneVTwoOneLegalRepScenarioWithSingleResponseToBothClaimantsSetToNo() {
        CaseData caseData = CaseData.builder()
            .respondentResponseIsSame(YesOrNo.NO)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .defendantSingleResponseToBothClaimants(YesOrNo.NO)
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceRouteRequired2("DISPUTES_THE_CLAIM")
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP);

            CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

            AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
            assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
            assertEquals(
                YesOrNo.YES.name(),
                callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
            );
        }
    }

    @Test
    void shouldHandleOneVTwoTwoLegalRepScenarioWithCanAnswerRespondent2() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .defendantSingleResponseToBothClaimants(YesOrNo.NO)
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceRouteRequired2("DISPUTES_THE_CLAIM")
            .showConditionFlags(Collections.singleton(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

            AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
            assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
            assertEquals(
                YesOrNo.YES.name(),
                callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
            );
        }
    }

    @Test
    void shouldAddSomeoneDisputesWhenOnlyRespondent1Disputes() {
        CaseData caseData = CaseData.builder()
            .showConditionFlags(Collections.singleton(DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
    }

    @Test
    void shouldAddSomeoneDisputesWhenOnlyRespondent2Disputes() {
        CaseData caseData = CaseData.builder()
            .showConditionFlags(Collections.singleton(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
    }

    @Test
    void shouldAddSomeoneDisputesWhenBothRespondentsDispute() {
        CaseData caseData = CaseData.builder()
            .showConditionFlags(Collections.singleton(DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
    }

    @Test
    void shouldSetPaidStatusToPaidLessThanClaimedAmountWhenPaidLess() {
        CaseData caseData = CaseData.builder()
            .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondToClaim(RespondToClaim.builder()
                                .howMuchWasPaid(BigDecimal.valueOf(50_000))
                                .build())
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .request(callbackRequest)
            .build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specPaidLessAmountOrDisputesOrPartAdmission"));
    }

    @Test
    void shouldAddSpecDisputesOrPartAdmissionWhenBothRespondentsDispute() {
        CaseData caseData = CaseData.builder()
            .showConditionFlags(Collections.singleton(DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE))
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("HAS_PAID_THE_AMOUNT_CLAIMED").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
    }

    @Test
    void shouldSetSpecDisputesOrPartAdmissionForOneVTwoTwoLegalRepScenario() {
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .defendantSingleResponseToBothClaimants(YesOrNo.NO)
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceRouteRequired2("DISPUTES_THE_CLAIM")
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP);

            CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

            AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
            assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
            assertEquals(
                YesOrNo.YES.name(),
                callbackResponse.getData().get("specDisputesOrPartAdmission").toString().toUpperCase()
            );
        }
    }

    @Test
    void shouldReturnPaidLessTagWhenPaidLessThanClaimedAmount() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("HAS_PAID_THE_AMOUNT_CLAIMED")
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .respondToClaim(RespondToClaim.builder().howMuchWasPaid(BigDecimal.valueOf(500)).build())
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specPaidLessAmountOrDisputesOrPartAdmission"));
    }

    @Test
    void shouldNotReturnPaidLessTagWhenPaidEqualOrMoreThanClaimedAmount() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("HAS_PAID_THE_AMOUNT_CLAIMED")
            .respondToClaim(RespondToClaim.builder().howMuchWasPaid(BigDecimal.valueOf(1000)).build())
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specPaidLessAmountOrDisputesOrPartAdmission"));
        assertEquals(
            YesOrNo.NO.name(),
            callbackResponse.getData().get("specPaidLessAmountOrDisputesOrPartAdmission").toString().toUpperCase()
        );
    }
}
