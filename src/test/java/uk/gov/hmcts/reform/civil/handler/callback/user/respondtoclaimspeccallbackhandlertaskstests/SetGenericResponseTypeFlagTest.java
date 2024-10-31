package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler.RespondToClaimSpecUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler.SetGenericResponseTypeFlag;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.EnumSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SetGenericResponseTypeFlagTest {

    private static final String BEARER_TOKEN_VALUE = "Bearer token";
    private static final String MULTI_PARTY_RESPONSE_TYPE_FLAGS = "multiPartyResponseTypeFlags";
    private static final String RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC = "respondentClaimResponseTypeForSpecGeneric";
    private static final String SPEC_FULL_ADMISSION_OR_PART_ADMISSION = "specFullAdmissionOrPartAdmission";
    private static final String DEFENCE_ADMIT_PART_PAYMENT_TIME_ROUTE_GENERIC = "defenceAdmitPartPaymentTimeRouteGeneric";
    private static final String RESPONDENT_RESPONSE_IS_SAME = "respondentResponseIsSame";
    private static final String SAME_SOLICITOR_SAME_RESPONSE = "sameSolicitorSameResponse";

    private SetGenericResponseTypeFlag setGenericResponseTypeFlag;

    @Mock
    private UserService userService;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtilsDisputeDetails;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        setGenericResponseTypeFlag = new SetGenericResponseTypeFlag(objectMapper, userService, coreCaseUserService, respondToClaimSpecUtilsDisputeDetails);
    }

    @Test
    void shouldSetGenericResponseTypeFlagForOneVOneScenario() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_ONE);

        assertEquals("FULL_DEFENCE", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
        assertEquals("FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetGenericResponseTypeFlagForTwoVOneScenario() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, TWO_V_ONE);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
        assertEquals("Yes", response.getData().get(SPEC_FULL_ADMISSION_OR_PART_ADMISSION));
    }

    /*@Test
    void shouldSetGenericResponseTypeFlagForOneVTwoOneLegalRepScenario() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("No", response.getData().get(RESPONDENT_RESPONSE_IS_SAME));
        assertEquals("No", response.getData().get(SAME_SOLICITOR_SAME_RESPONSE));
        assertEquals("FULL_DEFENCE", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
    }*/

    @Test
    void shouldSetGenericResponseTypeFlagForOneVTwoTwoLegalRepScenario() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .isRespondent2(YES)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .ccdCaseReference(1234L)
            .build();

        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_TWO_LEGAL_REP);

        assertEquals("PART_ADMISSION", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
        assertEquals("Yes", response.getData().get(SPEC_FULL_ADMISSION_OR_PART_ADMISSION));
    }

    @Test
    void shouldSetCounterAdmitOrAdmitPartFlagWhenBothClaimantsHaveAdmissionOrCounterClaim() {
        CaseData caseData = CaseData.builder()
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetCounterAdmitOrAdmitPartFlagWhenBothClaimantsHaveFullAdmission() {
        CaseData caseData = CaseData.builder()
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetCounterAdmitOrAdmitPartFlagWhenBothClaimantsHavePartAdmission() {
        CaseData caseData = CaseData.builder()
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetCounterAdmitOrAdmitPartFlagWhenBothClaimantsHaveCounterClaim() {
        CaseData caseData = CaseData.builder()
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetFullDefenceFlagWhenResponseTypeIsFullDefence() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetCounterClaimFlagWhenResponseTypeIsCounterClaim() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetFullAdmissionFlagWhenResponseTypeIsFullAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("FULL_ADMISSION", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetSpecFullAdmissionOrPartAdmissionWhenTwoVOneAndBothRespondentsHaveAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, TWO_V_ONE);

        assertEquals("Yes", response.getData().get(SPEC_FULL_ADMISSION_OR_PART_ADMISSION));
    }

    @Test
    void shouldSetFullDefenceFlagWhenRespondent1HasFullDefence() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetFullDefenceFlagWhenRespondent1HasCounterClaim() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    /*@Test
    void shouldHandleDifferentResponseForOneVTwoOneLegalRep() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("FULL_DEFENCE", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
    }*/

    @Test
    void shouldNotSetCounterAdmitOrAdmitPartFlagWhenOnlyOneClaimantHasCounterClaim() {
        CaseData caseData = CaseData.builder()
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .claimant2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldNotSetFullAdmissionFlagWhenRespondent2DoesNotHaveFullAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertNotEquals("FULL_ADMISSION", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
        assertEquals("No", response.getData().get(SPEC_FULL_ADMISSION_OR_PART_ADMISSION));
    }

    @Test
    void shouldNotSetFullAdmissionFlagWhenRespondent2ResponseTypeIsNull() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(null)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("FULL_ADMISSION", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
        assertEquals("No", response.getData().get(SPEC_FULL_ADMISSION_OR_PART_ADMISSION));
    }

    @Test
    void shouldNotSetFullAdmissionFlagWhenRespondent2ClaimResponseTypeIsNull() {
        CaseData caseData = CaseData.builder()
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertNotEquals("FULL_ADMISSION", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
        assertEquals("No", response.getData().get(SPEC_FULL_ADMISSION_OR_PART_ADMISSION));
    }

    @Test
    void shouldNotSetSpecFullAdmissionOrPartAdmissionWhenRespondent2DoesNotHaveAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, TWO_V_ONE);

        assertEquals("FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
        assertEquals("No", response.getData().get(SPEC_FULL_ADMISSION_OR_PART_ADMISSION));
    }

    /*@Test
    void shouldSetFullDefenceFlagWhenRespondent1HasCounterClaimInOneVTwoOneLegalRepScenario() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("NOT_FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }*/

    /*@Test
    void shouldSetFullDefenceFlagWhenBothRespondentsHaveFullDefenceInOneVTwoOneLegalRepScenario() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }*/

    /*@Test
    void shouldSetFullDefenceFlagWhenBothRespondentsHaveCounterClaimInOneVTwoOneLegalRepScenario() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }*/

    /*@Test
    void shouldHandleDifferentResponsesForRespondentsInOneVTwoOneLegalRepScenario() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("FULL_DEFENCE", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
    }*/

    @Test
    void shouldSetGenericResponseTypeFlagWhenBothRespondentsHavePartAdmissionInOneVTwoTwoLegalRepScenario() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .isRespondent2(YES)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .ccdCaseReference(1234L)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_TWO_LEGAL_REP);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetGenericResponseTypeFlagWhenUserHasRespondentSolicitorTwoRoleInOneVTwoTwoLegalRepScenario() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .isRespondent2(YES)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .ccdCaseReference(1234L)
            .build();

        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_TWO_LEGAL_REP);

        assertEquals("PART_ADMISSION", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    /*@Test
    void shouldSetGenericResponseTypeFlagWhenRespondent2HasPartAdmissionInOneVTwoTwoLegalRepScenario() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .ccdCaseReference(1234L)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_TWO_LEGAL_REP);

        assertEquals("PART_ADMISSION", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }*/

    /*@Test
    void shouldSetGenericResponseTypeFlagWhenRespondent1HasFullAdmissionAndRespondent2HasPartAdmissionInOneVTwoTwoLegalRepScenario() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .isRespondent2(YES)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .ccdCaseReference(1234L)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_TWO_LEGAL_REP);

        assertEquals("PART_ADMISSION", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }*/

    @Test
    void shouldNotSetCounterAdmitOrAdmitPartFlagWhenClaimant1HasFullDefence() {
        CaseData caseData = CaseData.builder()
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertEquals("FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetGenericResponseTypeFlagWhenBothRespondentsHaveFullDefenceInOneVTwoTwoLegalRepScenario() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .isRespondent2(YES)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .ccdCaseReference(1234L)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_TWO_LEGAL_REP);

        assertEquals("FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetGenericResponseTypeFlagWhenRespondent1HasFullDefenceAndRespondent2HasFullAdmissionInOneVTwoTwoLegalRepScenario() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .isRespondent2(YES)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .ccdCaseReference(1234L)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_TWO_LEGAL_REP);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetGenericResponseTypeFlagWhenRespondent1HasFullDefenceAndImmediatePaymentRouteInOneVTwoTwoLegalRepScenario() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .isRespondent2(YES)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .ccdCaseReference(1234L)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_TWO_LEGAL_REP);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
        assertEquals("IMMEDIATELY", response.getData().get(DEFENCE_ADMIT_PART_PAYMENT_TIME_ROUTE_GENERIC));
    }

    @Test
    void shouldSetGenericResponseTypeFlagWhenRespondent2HasFullAdmissionAndImmediatePaymentRouteInOneVTwoTwoLegalRepScenario() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CaseData caseData = CaseData.builder()
            .isRespondent2(YES)
            .defenceAdmitPartPaymentTimeRouteRequired2(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .isRespondent1(YES)
            .ccdCaseReference(1234L)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_TWO_LEGAL_REP);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
        assertEquals("IMMEDIATELY", response.getData().get(DEFENCE_ADMIT_PART_PAYMENT_TIME_ROUTE_GENERIC));
    }

    /*@Test
    void shouldSetGenericResponseTypeFlagWhenRespondent1HasFullAdmissionAndSameResponseInOneVTwoOneLegalRepScenario() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CaseData caseData = CaseData.builder()
            .showConditionFlags(EnumSet.of(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondentResponseIsSame(YES)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("FULL_ADMISSION", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
        assertEquals("NOT_FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }*/

    @Test
    void shouldSetGenericResponseTypeFlagWhenRespondent1HasDifferentResponseInOneVTwoOneLegalRepScenario() {
        CaseData caseData = CaseData.builder()
            .showConditionFlags(EnumSet.of(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1))
            .respondentResponseIsSame(NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("No", response.getData().get(RESPONDENT_RESPONSE_IS_SAME));
        assertEquals("No", response.getData().get(SAME_SOLICITOR_SAME_RESPONSE));
        assertEquals("FULL_DEFENCE", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
        assertEquals("FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    @Test
    void shouldSetGenericResponseTypeFlagWhenRespondent2HasFullAdmissionAndSameResponseInOneVTwoOneLegalRepScenario() {
        CaseData caseData = CaseData.builder()
            .showConditionFlags(EnumSet.of(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2))
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondentResponseIsSame(YES)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    /*@Test
    void shouldSetGenericResponseTypeFlagWhenRespondent2HasFullDefenceAndSameResponseInOneVTwoOneLegalRepScenario() {
        CaseData caseData = CaseData.builder()
            .showConditionFlags(EnumSet.of(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2))
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondentResponseIsSame(YES)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("FULL_DEFENCE", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
        assertEquals("FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }*/

    @Test
    void shouldNotSetSpecFullAdmissionOrPartAdmissionWhenRespondent2HasNoAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, TWO_V_ONE);

        assertEquals("FULL_DEFENCE", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
        assertEquals("No", response.getData().get(SPEC_FULL_ADMISSION_OR_PART_ADMISSION));
    }

    /*@Test
    void shouldNotSetFullDefenceFlagWhenRespondent1ClaimResponseTypeIsNotCounterClaim() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }*/

    @Test
    void shouldSetGenericResponseTypeFlagForOneVTwoTwoLegalRepScenarioWithRespondent2NotResponding() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CaseData caseData = CaseData.builder()
            .isRespondent1(YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .isRespondent2(NO)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .ccdCaseReference(1234L)
            .build();

        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_TWO_LEGAL_REP);

        assertEquals("PART_ADMISSION", response.getData().get(RESPONDENT_CLAIM_RESPONSE_TYPE_GENERIC));
        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
        assertEquals("Yes", response.getData().get(SPEC_FULL_ADMISSION_OR_PART_ADMISSION));
    }

    @Test
    void shouldSetGenericResponseTypeFlagWhenRespondent1HasFullAdmissionAndResponsesAreSameInOneVTwoOneLegalRepScenario() {
        CaseData caseData = CaseData.builder()
            .showConditionFlags(EnumSet.of(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1))
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondentResponseIsSame(YES)
            .build();

        AboutToStartOrSubmitCallbackResponse response = executeWithMockedScenario(caseData, ONE_V_TWO_ONE_LEGAL_REP);

        assertEquals("COUNTER_ADMIT_OR_ADMIT_PART", response.getData().get(MULTI_PARTY_RESPONSE_TYPE_FLAGS));
    }

    /*@Test
    void shouldAddSomeoneDisputesWhenIsSomeoneDisputesReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        when(respondToClaimSpecUtilsDisputeDetails.isSomeoneDisputes(caseData)).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(callbackParams);

        assertTrue(((List<?>) response.getData().get("showConditionFlags")).contains("SOMEONE_DISPUTES"));
    }*/

    private CallbackParams buildCallbackParams(CaseData caseData) {
        return CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, BEARER_TOKEN_VALUE))
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse executeWithMockedScenario(CaseData caseData, MultiPartyScenario scenario) {
        try (MockedStatic<MultiPartyScenario> mockedScenarioStatic = mockStatic(MultiPartyScenario.class)) {
            mockedScenarioStatic.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(scenario);
            return (AboutToStartOrSubmitCallbackResponse) setGenericResponseTypeFlag.execute(buildCallbackParams(caseData));
        }
    }
}
