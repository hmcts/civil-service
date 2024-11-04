package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SetApplicantResponseDeadlineTest {

    private SetApplicantResponseDeadline setApplicantResponseDeadline;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private StateFlow mockedStateFlow;

    @Mock
    private UserService userService;

    @Mock
    private Time time;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private CaseFlagsInitialiser caseFlagsInitialiser;

    @Mock
    private DeadlineExtensionCalculatorService deadlineCalculatorService;

    @Mock
    private CourtLocationUtils courtLocationUtils;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtils;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        setApplicantResponseDeadline = new SetApplicantResponseDeadline(
            userService,
            coreCaseUserService,
            toggleService,
            objectMapper,
            caseFlagsInitialiser,
            time,
            deadlineCalculatorService,
            stateFlowEngine,
            deadlinesCalculator,
            courtLocationUtils,
            respondToClaimSpecUtils
        );
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
    }

    private AboutToStartOrSubmitCallbackResponse executeCallback(CallbackParams callbackParams) {
        return (AboutToStartOrSubmitCallbackResponse) setApplicantResponseDeadline.execute(callbackParams);
    }

    @Test
    void shouldSetApplicantResponseDeadlineWhenMultiPartyScenarioOneVOne() {
        Party respondent1Copy = buildParty();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1Copy(respondent1Copy)
                .respondent1DQ(new Respondent1DQ())
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        assertNull(response.getErrors());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldSetApplicantResponseDeadlineWhenSolicitorRepresentsOnlyRespondentOne() {
        when(coreCaseUserService.userHasCaseRole("1594901956117591", "uid", RESPONDENTSOLICITORTWO))
            .thenReturn(true);
        when(time.now()).thenReturn(LocalDateTime.now());

        Party respondent1Copy = buildParty();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1Copy(respondent1Copy)
                .respondent2DQ(new Respondent2DQ())
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        assertNull(response.getErrors());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldSetApplicantResponseDeadlineWhenRespondent2HasDifferentResponseAndExperts() {
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class))).thenReturn(true);
        when(time.now()).thenReturn(LocalDateTime.now());

        Party respondent1Copy = buildParty();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondentResponseIsSame(NO)
                .respondent1Copy(respondent1Copy)
                .respondent2DQ(new Respondent2DQ())
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        assertNull(response.getErrors());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldUpdatePaymentTimeRouteWhenConditionsAreMet() {
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class))).thenReturn(true);
        when(time.now()).thenReturn(LocalDateTime.now());

        Party respondent1Copy = buildParty();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
                .respondent1Copy(respondent1Copy)
                .respondent2DQ(new Respondent2DQ())
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        assertNull(response.getErrors());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldNotUpdatePaymentTimeRouteWhenDefenceAdmitPartPaymentTimeRouteRequiredIsNull() {
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class))).thenReturn(true);
        when(time.now()).thenReturn(LocalDateTime.now());

        Party respondent1Copy = buildParty();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
                .respondent1Copy(respondent1Copy)
                .respondent2DQ(new Respondent2DQ())
                .defenceAdmitPartPaymentTimeRouteRequired(null)
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        assertNull(response.getErrors());
        assertThat(response).isNotNull();
        verify(deadlineCalculatorService, never()).calculateExtendedDeadline(any(LocalDate.class), anyInt());
    }

    @Test
    void shouldNotUpdatePaymentTimeRouteWhenDefenceAdmitPartPaymentTimeRouteRequiredIsNotImmediately() {
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class))).thenReturn(true);
        when(time.now()).thenReturn(LocalDateTime.now());

        Party respondent1Copy = buildParty();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
                .respondent1Copy(respondent1Copy)
                .respondent2DQ(new Respondent2DQ())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        assertNull(response.getErrors());
        assertThat(response).isNotNull();
        verify(deadlineCalculatorService, never()).calculateExtendedDeadline(any(LocalDate.class), anyInt());
    }

    @Test
    void shouldNotUpdatePaymentTimeRouteWhenResponseTypeIsNotPartOrFullAdmission() {
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class))).thenReturn(true);
        when(time.now()).thenReturn(LocalDateTime.now());

        Party respondent1Copy = buildParty();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
                .respondent1Copy(respondent1Copy)
                .respondent2DQ(new Respondent2DQ())
                .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        assertNull(response.getErrors());
        assertThat(response).isNotNull();
        verify(deadlineCalculatorService, never()).calculateExtendedDeadline(any(LocalDate.class), anyInt());
    }

    @Test
    void shouldNotUpdateRespondent2ClaimResponseTypeAndResponseDateWhenRespondentResponseIsSameIsNull() {
        LocalDateTime responseDate = LocalDateTime.now();

        when(time.now()).thenReturn(responseDate);

        Party respondent1Copy = buildParty();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondentResponseIsSame(null)
                .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
                .respondent1Copy(respondent1Copy)
                .respondent1DQ(new Respondent1DQ())
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getRespondent2ClaimResponseTypeForSpec()).isNull();
        assertThat(updatedCaseData.getRespondent2ResponseDate()).isNull();
    }

    @Test
    void shouldNotUpdateRespondent2ClaimResponseTypeAndResponseDateWhenRespondentResponseIsSameIsNo() {
        LocalDateTime responseDate = LocalDateTime.now();

        when(time.now()).thenReturn(responseDate);

        Party respondent1Copy = buildParty();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondentResponseIsSame(NO)
                .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
                .respondent1Copy(respondent1Copy)
                .respondent1DQ(new Respondent1DQ())
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getRespondent2ClaimResponseTypeForSpec()).isNull();
        assertThat(updatedCaseData.getRespondent2ResponseDate()).isNull();
    }

    @Test
    void shouldUpdateRespondent1PrimaryAddressWhenSpecAoSApplicantCorrespondenceAddressRequiredIsNo() {
        Address correspondenceAddress = buildAddress("123 Test Street", "AB12 3CD");

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                .atSpecAoSApplicantCorrespondenceAddressDetails(correspondenceAddress)
                .respondent1DQ(new Respondent1DQ())
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getRespondent1().getPrimaryAddress()).isEqualTo(correspondenceAddress);
    }

    @Test
    void shouldUpdateRespondent2WhenBothRespondent2AndRespondent2CopyArePresent() {
        Address address = buildAddress("123 Test Street", "AB12 3CD");

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        Party respondent2 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        Party respondent2Copy = Party.builder()
            .primaryAddress(address)
            .flags(new Flags())
            .build();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1Copy(respondent1)
                .respondent1DQ(new Respondent1DQ())
                .respondent2(respondent2)
                .respondent2Copy(respondent2Copy)
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getRespondent2().getPrimaryAddress()).isEqualTo(address);
        assertThat(updatedCaseData.getRespondent2().getFlags()).isEqualTo(new Flags());
        assertNull(updatedCaseData.getRespondent2Copy());
        assertThat(updatedCaseData.getRespondent2DetailsForClaimDetailsTab().getPrimaryAddress()).isEqualTo(address);
        assertNull(updatedCaseData.getRespondent2DetailsForClaimDetailsTab().getFlags());
    }

    @Test
    void shouldNotUpdateRespondent2WhenRespondent2CopyIsNotPresent() {
        Address address = buildAddress("123 Test Street", "AB12 3CD");

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        Party respondent2 = Party.builder()
            .primaryAddress(address)
            .type(Party.Type.INDIVIDUAL)
            .flags(new Flags())
            .build();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1Copy(respondent1)
                .respondent1DQ(new Respondent1DQ())
                .respondent2(respondent2)
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getRespondent2().getPrimaryAddress()).isEqualTo(address);
        assertThat(updatedCaseData.getRespondent2().getFlags()).isEqualTo(new Flags());
        assertNull(updatedCaseData.getRespondent2Copy());
        assertThat(updatedCaseData.getRespondent2DetailsForClaimDetailsTab().getPrimaryAddress()).isEqualTo(address);
        assertNull(updatedCaseData.getRespondent2DetailsForClaimDetailsTab().getFlags());
    }

    @Test
    void shouldUpdateRespondent2PrimaryAddressWhenSpecAoSRespondent2HomeAddressRequiredIsNo() {
        Address expectedAddress = buildAddress("123 Test Street", "AB1 2CD");

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        Party respondent2 = Party.builder()
            .primaryAddress(expectedAddress)
            .type(Party.Type.INDIVIDUAL)
            .flags(new Flags())
            .build();

        CaseData caseData = CaseData.builder()
            .specAoSRespondent2HomeAddressRequired(NO)
            .specAoSRespondent2HomeAddressDetails(expectedAddress)
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent1DQ(new Respondent1DQ())
            .respondent2(respondent2)
            .respondent2Copy(respondent2)
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getRespondent2().getPrimaryAddress()).isEqualTo(expectedAddress);
    }

    @Test
    void shouldSetDefenceAdmitPartPaymentTimeRouteRequiredToNullWhenSolicitorTwoAndFullDefence() {
        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        Party respondent2 = Party.builder()
            .primaryAddress(buildAddress("123 Test Street", "AB12 3CD"))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .respondent2ClaimResponseTypeForSpec(FULL_DEFENCE)
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent1DQ(new Respondent1DQ())
            .respondent2(respondent2)
            .respondent2Copy(respondent2)
            .respondent2DQ(new Respondent2DQ())
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertNull(updatedCaseData.getDefenceAdmitPartPaymentTimeRouteRequired());
    }

    @Test
    void shouldAddEventAndDateToExpertsAndWitnessesWhenUpdateContactDetailsToggleIsEnabled() {
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class))).thenReturn(true);
        when(time.now()).thenReturn(LocalDateTime.now());
        when(toggleService.isUpdateContactDetailsEnabled()).thenReturn(true);

        Party respondent1Copy = buildParty();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
                .respondent1Copy(respondent1Copy)
                .respondent1ResponseDate(LocalDateTime.now())
                .respondent2DQ(new Respondent2DQ())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);
        assertNull(response.getErrors());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldPopulateDQPartyIdsWhenHmcToggleIsEnabled() {
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class))).thenReturn(true);
        when(time.now()).thenReturn(LocalDateTime.now());
        when(toggleService.isHmcEnabled()).thenReturn(true);
        Party respondent1Copy = buildParty();
        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
                .respondent1Copy(respondent1Copy)
                .respondent2DQ(new Respondent2DQ())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
        );
        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        assertNull(response.getErrors());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldUpdateWitnessesWhenRespondent1DQWitnessesSmallClaimIsNotNull() {
        Witnesses respondent1Witnesses = Witnesses.builder().build();
        Witnesses respondent2Witnesses = Witnesses.builder().build();

        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().build();

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent1DQWitnessesSmallClaim(respondent1Witnesses)
            .respondent2DQWitnessesSmallClaim(respondent2Witnesses)
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedCaseData.getRespondent1DQ().getRespondent1DQWitnesses()).isEqualTo(respondent1Witnesses);
        assertThat(updatedCaseData.getRespondent2DQ().getRespondent2DQWitnesses()).isEqualTo(respondent2Witnesses);
    }

    @Test
    void shouldUpdateRespondent1ExpertsWhenExpertRequiredIsYesAndDetailsPresent() {
        Witnesses respondent1Witnesses = Witnesses.builder().build();

        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondToClaimExperts(ExpertDetails.builder().build()).build();

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .responseClaimExpertSpecRequired(YES)
            .respondent1DQ(respondent1DQ)
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent1DQWitnessesSmallClaim(respondent1Witnesses)
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        Map<String, Object> responseData = response.getData();

        Witnesses actualWitnesses = objectMapper.convertValue(
            responseData.get("respondent1DQWitnesses"),
            Witnesses.class
        );

        assertThat(actualWitnesses).isEqualTo(respondent1Witnesses);
    }

    @Test
    void shouldUpdateRespondent1ExpertsWhenExpertRequiredIsNo() {
        Witnesses respondent1Witnesses = Witnesses.builder().build();

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .responseClaimExpertSpecRequired(NO)
            .respondent1DQ(new Respondent1DQ())
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent1DQWitnessesSmallClaim(respondent1Witnesses)
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        Map<String, Object> responseData = response.getData();

        Experts actualExperts = objectMapper.convertValue(responseData.get("respondent1DQExperts"), Experts.class);

        assertThat(actualExperts.getExpertRequired()).isEqualTo(NO);
        assertThat(actualExperts.getDetails()).isNull();
    }

    @Test
    void shouldUpdateRespondent2ExpertsWhenExpertRequiredIsYesAndDetailsPresent() {
        Witnesses respondent2Witnesses = Witnesses.builder().build();

        Respondent2DQ respondent2DQ = Respondent2DQ.builder().respondToClaimExperts2(ExpertDetails.builder().build()).build();

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        Party respondent2 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .responseClaimExpertSpecRequired2(YES)
            .respondent1DQ(new Respondent1DQ())
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent2DQ(respondent2DQ)
            .respondent2(respondent2)
            .respondent2Copy(respondent2)
            .respondent2DQWitnessesSmallClaim(respondent2Witnesses)
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        Map<String, Object> responseData = response.getData();

        Witnesses actualWitnesses = objectMapper.convertValue(
            responseData.get("respondent2DQWitnesses"),
            Witnesses.class
        );

        assertThat(actualWitnesses).isEqualTo(respondent2Witnesses);
    }

    @Test
    void shouldUpdateRespondent2ExpertsWhenExpertRequiredIsNo() {
        Witnesses respondent2Witnesses = Witnesses.builder().build();

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        Party respondent2 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .responseClaimExpertSpecRequired2(NO)
            .respondent1DQ(new Respondent1DQ())
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent2DQ(new Respondent2DQ())
            .respondent2(respondent2)
            .respondent2Copy(respondent2)
            .respondent2DQWitnessesSmallClaim(respondent2Witnesses)
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        Map<String, Object> responseData = response.getData();

        Experts actualExperts = objectMapper.convertValue(responseData.get("respondent2DQExperts"), Experts.class);

        assertThat(actualExperts.getDetails()).isNull();
    }

    @Test
    void shouldUpdateRespondent1CorrespondenceAddressWhenSolicitorOnePresentAndCorrespondenceNotRequired() {
        when(coreCaseUserService.userHasCaseRole(eq("1234"), anyString(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(
            eq("1234"),
            anyString(),
            eq(RESPONDENTSOLICITORTWO)
        )).thenReturn(false);

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .specAoSRespondentCorrespondenceAddressRequired(NO)
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent1DQ(new Respondent1DQ())
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNotNull();
        assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isEqualTo(Address.builder().build());
    }

    @Test
    void shouldUpdateRespondent12CorrespondenceAddressWhenSolicitorTwoPresentAndCorrespondenceNotRequired() {
        when(coreCaseUserService.userHasCaseRole(
            eq("1234"),
            anyString(),
            eq(RESPONDENTSOLICITORONE)
        )).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole(eq("1234"), anyString(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .specAoSRespondent2CorrespondenceAddressRequired(NO)
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent1DQ(new Respondent1DQ())
            .respondent2DQ(new Respondent2DQ())
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getSpecAoSRespondent2CorrespondenceAddressdetails()).isNotNull();
        assertThat(updatedCaseData.getSpecAoSRespondent2CorrespondenceAddressdetails()).isEqualTo(Address.builder().build());
    }

    @Test
    void shouldUpdateRespondent1WitnessesWhenExpertRequiredIsYesAndDetailsPresent() {
        Witnesses respondent1Witnesses = Witnesses.builder().build();

        Respondent1DQ respondent1DQ = Respondent1DQ.builder().build();

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .responseClaimExpertSpecRequired(YES)
            .respondent1DQ(respondent1DQ)
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent1DQWitnessesSmallClaim(respondent1Witnesses)
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        Map<String, Object> responseData = response.getData();

        Witnesses actualWitnesses = objectMapper.convertValue(
            responseData.get("respondent1DQWitnesses"),
            Witnesses.class
        );

        assertThat(actualWitnesses).isEqualTo(respondent1Witnesses);
    }

    @Test
    void shouldUpdateRespondent2WitnessesWhenExpertRequiredIsYesAndDetailsPresent() {
        Witnesses respondent2Witnesses = Witnesses.builder().build();

        Respondent2DQ respondent2DQ = Respondent2DQ.builder().build();

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        Party respondent2 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .responseClaimExpertSpecRequired2(YES)
            .respondent1DQ(new Respondent1DQ())
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent2DQ(respondent2DQ)
            .respondent2(respondent2)
            .respondent2Copy(respondent2)
            .respondent2DQWitnessesSmallClaim(respondent2Witnesses)
            .ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        Map<String, Object> responseData = response.getData();

        Witnesses actualWitnesses = objectMapper.convertValue(
            responseData.get("respondent2DQWitnesses"),
            Witnesses.class
        );

        assertThat(actualWitnesses).isEqualTo(respondent2Witnesses);
    }

    @Test
    void shouldNotUpdatePaymentTimeRouteWhenDefenceAdmitPartPaymentTimeRouteRequiredIsNotImmediately1() {
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class))).thenReturn(true);
        when(time.now()).thenReturn(LocalDateTime.now());

        Party respondent1Copy = buildParty();

        CaseData caseData = buildCaseData(
            CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
                .respondent2ClaimResponseTypeForSpec(PART_ADMISSION)
                .respondent1Copy(respondent1Copy)
                .respondent2DQ(new Respondent2DQ())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_TWO_LEGAL_REP);

            AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

            assertNull(response.getErrors());
            assertThat(response).isNotNull();
            verify(deadlineCalculatorService, never()).calculateExtendedDeadline(any(LocalDate.class), anyInt());
        }
    }

    @Test
    void shouldUpdateRespondent1CorrespondenceAddressWhenSolicitorOnePresentAndCorrespondenceNotRequired1() {
        when(coreCaseUserService.userHasCaseRole(eq("1234"), anyString(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(
            eq("1234"),
            anyString(),
            eq(RESPONDENTSOLICITORTWO)
        )).thenReturn(false);

        Party respondent1 = Party.builder()
            .primaryAddress(buildAddress("Old Address", ""))
            .type(Party.Type.INDIVIDUAL)
            .build();

        ResponseDocument testDocument = ResponseDocument.builder()
            .file(Document.builder()
                      .documentUrl("fake-url")
                      .documentFileName("file-name")
                      .documentBinaryUrl("binary-url")
                      .build())
            .build();

        List<Element<CaseDocument>> defendantUploads = new ArrayList<>();

        CaseData caseData = CaseData.builder()
            .specAoSRespondentCorrespondenceAddressRequired(NO)
            .respondent1(respondent1)
            .respondent1Copy(respondent1)
            .respondent1DQ(Respondent1DQ.builder()
                               .respondToCourtLocation(RequestedCourt.builder().build())
                               .build())
            .ccdCaseReference(1234L)
            .defendantResponseDocuments(defendantUploads)
            .courtLocation(CourtLocation.builder().build())
            .respondent1SpecDefenceResponseDocument(testDocument)
            .respondent2SpecDefenceResponseDocument(testDocument)
            .build();

        CallbackParams callbackParams = buildCallbackParams(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                ONE_V_TWO_ONE_LEGAL_REP);

            AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

            CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNotNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isEqualTo(Address.builder().build());
        }
    }

    private Address buildAddress(String line1, String postCode) {
        return Address.builder()
            .addressLine1(line1)
            .postCode(postCode)
            .build();
    }

    private Party buildParty() {
        return Party.builder()
            .primaryAddress(buildAddress("123 Test Street", "AB12 3CD"))
            .build();
    }

    private CaseData buildCaseData(CaseDataBuilder builder) {
        return builder.build();
    }

    private CallbackParams buildCallbackParams(CaseData caseData) {
        return CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "Bearer token"))
            .build();
    }
}
