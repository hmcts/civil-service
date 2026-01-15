package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
class HandleDefendAllClaimTest {

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private PaymentDateValidator paymentDateValidator;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtils;

    @InjectMocks
    private HandleDefendAllClaim handleDefendAllClaim;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        handleDefendAllClaim = new HandleDefendAllClaim(
                objectMapper,
                toggleService,
                paymentDateValidator,
                respondToClaimSpecUtils
        );
    }

    @Test
    void shouldReturnErrorResponseWhenPaymentsAreInvalid() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();
        when(paymentDateValidator.validate(any())).thenReturn(List.of("Error"));

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertEquals(List.of("Error"), callbackResponse.getErrors());
    }

    @Test
    void shouldReturnSuccessResponseWhenPaymentsAreValid() {
        CaseData caseData = CaseDataBuilder.builder()
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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDefenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(BigDecimal.valueOf(50_000));
        caseData.setRespondToClaim(respondToClaim);
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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent1(YesOrNo.YES);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));

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
        CaseData caseData = CaseDataBuilder.builder()
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
        CaseData caseData = CaseDataBuilder.builder()
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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent2(YesOrNo.YES);
        caseData.setDefenceRouteRequired2("DISPUTES_THE_CLAIM");
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");
        caseData.setDefenceRouteRequired2("DISPUTES_THE_CLAIM");
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.NO);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");
        caseData.setClaimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired2("DISPUTES_THE_CLAIM");

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(BigDecimal.valueOf(500));
        caseData.setRespondToClaim(respondToClaim);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.NO);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");
        caseData.setClaimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired2("DISPUTES_THE_CLAIM");

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.YES);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondentResponseIsSame(YesOrNo.YES);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.NO);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");
        caseData.setClaimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceRouteRequired2("DISPUTES_THE_CLAIM");

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.NO);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");
        caseData.setClaimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceRouteRequired2("DISPUTES_THE_CLAIM");
        caseData.setShowConditionFlags(Collections.singleton(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1));

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondentResponseIsSame(YesOrNo.NO);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.NO);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");
        caseData.setClaimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceRouteRequired2("DISPUTES_THE_CLAIM");

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.NO);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");
        caseData.setClaimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceRouteRequired2("DISPUTES_THE_CLAIM");
        caseData.setShowConditionFlags(Collections.singleton(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2));

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setShowConditionFlags(Collections.singleton(DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES));
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
    }

    @Test
    void shouldAddSomeoneDisputesWhenOnlyRespondent2Disputes() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setShowConditionFlags(Collections.singleton(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES));
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
    }

    @Test
    void shouldAddSomeoneDisputesWhenBothRespondentsDispute() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setShowConditionFlags(Collections.singleton(DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE));
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
    }

    @Test
    void shouldSetPaidStatusToPaidLessThanClaimedAmountWhenPaidLess() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDefenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(BigDecimal.valueOf(50_000));
        caseData.setRespondToClaim(respondToClaim);

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setShowConditionFlags(Collections.singleton(DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE));
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId("HAS_PAID_THE_AMOUNT_CLAIMED").build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specDisputesOrPartAdmission"));
    }

    @Test
    void shouldSetSpecDisputesOrPartAdmissionForOneVTwoTwoLegalRepScenario() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        caseData.setDefendantSingleResponseToBothClaimants(YesOrNo.NO);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("DISPUTES_THE_CLAIM");
        caseData.setClaimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefenceRouteRequired2("DISPUTES_THE_CLAIM");

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("HAS_PAID_THE_AMOUNT_CLAIMED");
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(BigDecimal.valueOf(500));
        caseData.setRespondToClaim(respondToClaim);

        CallbackRequest callbackRequest = CallbackRequest.builder().eventId(DEFENDANT_RESPONSE_SPEC).build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).request(callbackRequest).build();

        when(paymentDateValidator.validate(any())).thenReturn(Collections.emptyList());

        CallbackResponse response = handleDefendAllClaim.execute(callbackParams);

        AboutToStartOrSubmitCallbackResponse callbackResponse = (AboutToStartOrSubmitCallbackResponse) response;
        assertTrue(callbackResponse.getData().containsKey("specPaidLessAmountOrDisputesOrPartAdmission"));
    }

    @Test
    void shouldNotReturnPaidLessTagWhenPaidEqualOrMoreThanClaimedAmount() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired("HAS_PAID_THE_AMOUNT_CLAIMED");
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(BigDecimal.valueOf(100_000));
        caseData.setRespondToClaim(respondToClaim);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(1000));

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
