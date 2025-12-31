package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinetests;

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
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.ExpertsAndWitnessesCaseDataUpdater;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.SetApplicantResponseDeadlineCaseDataUpdater;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.SetApplicantResponseDeadlineSpec;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
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
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SetApplicantResponseDeadlineSpecTest {

    private SetApplicantResponseDeadlineSpec setApplicantResponseDeadline;

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
    private FeatureToggleService featureToggleService;

    @Mock
    private CaseFlagsInitialiser caseFlagsInitialiser;

    @Mock
    private CourtLocationUtils courtLocationUtils;

    @Mock
    private List<SetApplicantResponseDeadlineCaseDataUpdater> setApplicantResponseDeadlineCaseDataUpdaters;

    @Mock
    private List<ExpertsAndWitnessesCaseDataUpdater> expertsAndWitnessesCaseDataUpdaters;

    @Mock
    private RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtils;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        setApplicantResponseDeadline = new SetApplicantResponseDeadlineSpec(
                userService,
                coreCaseUserService,
                objectMapper,
                caseFlagsInitialiser,
                time,
                stateFlowEngine,
                deadlinesCalculator,
                courtLocationUtils,
                respondToClaimSpecUtils,
                setApplicantResponseDeadlineCaseDataUpdaters,
                expertsAndWitnessesCaseDataUpdaters,
                requestedCourtForClaimDetailsTab,
                featureToggleService
        );
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
    }

    private AboutToStartOrSubmitCallbackResponse executeCallback(CallbackParams callbackParams) {
        return (AboutToStartOrSubmitCallbackResponse) setApplicantResponseDeadline.execute(callbackParams);
    }

    @Test
    void shouldUpdateRespondent2WhenBothRespondent2AndRespondent2CopyArePresent() {
        LocalDateTime fixedNow = LocalDateTime.of(2025, 7, 21, 10, 0);
        when(time.now()).thenReturn(fixedNow);

        doReturn(fixedNow.plusMonths(1))
                .when(deadlinesCalculator)
                .calculateApplicantResponseDeadlineSpec(any(LocalDateTime.class));

        Address address = buildAddress("123 Test Street", "AB12 3CD");
        Party respondent2Copy = new Party();
        respondent2Copy.setPrimaryAddress(address);
        respondent2Copy.setFlags(new Flags());

        Party respondent1 = new Party();
        respondent1.setPrimaryAddress(buildAddress("Old Address", ""));
        respondent1.setType(Party.Type.INDIVIDUAL);

        Party respondent2 = new Party();
        respondent2.setPrimaryAddress(buildAddress("Old Address", ""));
        respondent2.setType(Party.Type.INDIVIDUAL);

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
        assertNull(updatedCaseData.getRespondent2().getFlags());
        assertNull(updatedCaseData.getRespondent2Copy());
        assertThat(updatedCaseData.getRespondent2DetailsForClaimDetailsTab().getPrimaryAddress()).isEqualTo(address);
        assertNull(updatedCaseData.getRespondent2DetailsForClaimDetailsTab().getFlags());
    }

    @Test
    void shouldNotUpdateRespondent2WhenRespondent2CopyIsNotPresent() {
        LocalDateTime fixedNow = LocalDateTime.of(2025, 7, 21, 10, 0);
        when(time.now()).thenReturn(fixedNow);

        doReturn(fixedNow.plusMonths(1))
                .when(deadlinesCalculator)
                .calculateApplicantResponseDeadlineSpec(any(LocalDateTime.class));

        Party respondent1 = new Party();
        respondent1.setPrimaryAddress(buildAddress("Old Address", ""));
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setFlags(new Flags());
        Party respondent2 = new Party();

        Address address = buildAddress("123 Test Street", "AB12 3CD");
        respondent2.setPrimaryAddress(address);
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setFlags(new Flags());

        CaseData caseData = buildCaseData(
                CaseDataBuilder.builder()
                        .atStateClaimDetailsNotified()
                        .respondent1(respondent1)
                        .respondent1Copy(respondent1)
                        .respondent1DQ(new Respondent1DQ())
                        .respondent2(respondent2)
        );

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getRespondent1().getFlags()).isEqualTo(new Flags());
        assertThat(updatedCaseData.getRespondent2().getPrimaryAddress()).isEqualTo(address);
        assertThat(updatedCaseData.getRespondent2().getFlags()).isEqualTo(new Flags());
        assertNull(updatedCaseData.getRespondent2Copy());
        assertThat(updatedCaseData.getRespondent2DetailsForClaimDetailsTab().getPrimaryAddress()).isEqualTo(address);
        assertNull(updatedCaseData.getRespondent2DetailsForClaimDetailsTab().getFlags());
    }

    @Test
    void shouldUpdateRespondent2PrimaryAddressWhenSpecAoSRespondent2HomeAddressRequiredIsNo() {
        LocalDateTime fixedNow = LocalDateTime.of(2025, 7, 21, 10, 0);
        when(time.now()).thenReturn(fixedNow);

        doReturn(fixedNow.plusMonths(1))
                .when(deadlinesCalculator)
                .calculateApplicantResponseDeadlineSpec(any(LocalDateTime.class));

        Address expectedAddress = buildAddress("123 Test Street", "AB1 2CD");

        Party respondent1 = new Party();
        respondent1.setPrimaryAddress(buildAddress("Old Address", ""));
        respondent1.setType(Party.Type.INDIVIDUAL);

        Party respondent2 = new Party();
        respondent2.setPrimaryAddress(expectedAddress);
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setFlags(new Flags());

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSpecAoSRespondent2HomeAddressRequired(NO);
        caseData.setSpecAoSRespondent2HomeAddressDetails(expectedAddress);
        caseData.setRespondent1(respondent1);
        caseData.setRespondent1Copy(respondent1);
        caseData.setRespondent1DQ(new Respondent1DQ());
        caseData.setRespondent1ResponseDeadline(LocalDateTime.now());
        caseData.setRespondent2(respondent2);
        caseData.setRespondent2Copy(respondent2);
        caseData.setCcdCaseReference(1234L);

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getRespondent2().getPrimaryAddress()).isEqualTo(expectedAddress);
    }

    @Test
    void shouldSetDefenceAdmitPartPaymentTimeRouteRequiredToNullWhenSolicitorTwoAndFullDefence() {
        Party respondent1 = new Party();
        respondent1.setPrimaryAddress(buildAddress("Old Address", ""));
        respondent1.setType(Party.Type.INDIVIDUAL);

        Party respondent2 = new Party();
        respondent2.setPrimaryAddress(buildAddress("123 Test Street", "AB12 3CD"));
        respondent2.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent2ClaimResponseTypeForSpec(FULL_DEFENCE);
        caseData.setRespondent1(respondent1);
        caseData.setRespondent1Copy(respondent1);
        caseData.setRespondent1DQ(new Respondent1DQ());
        caseData.setRespondent2(respondent2);
        caseData.setRespondent2Copy(respondent2);
        caseData.setRespondent2DQ(new Respondent2DQ());
        caseData.setCcdCaseReference(1234L);

        CallbackParams callbackParams = buildCallbackParams(caseData);

        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertNull(updatedCaseData.getDefenceAdmitPartPaymentTimeRouteRequired());
    }

    @Test
    void shouldUpdateRespondent1ExpertsWhenExpertRequiredIsYesAndDetailsPresent() {
        LocalDateTime fixedNow = LocalDateTime.of(2025, 7, 21, 10, 0);
        when(time.now()).thenReturn(fixedNow);

        doReturn(fixedNow.plusMonths(1))
                .when(deadlinesCalculator)
                .calculateApplicantResponseDeadlineSpec(any(LocalDateTime.class));

        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondToClaimExperts(new ExpertDetails());

        Party respondent1 = new Party();
        respondent1.setPrimaryAddress(buildAddress("Old Address", ""));
        respondent1.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResponseClaimExpertSpecRequired(YES);
        caseData.setApplicant1ResponseDeadline(LocalDateTime.now());
        caseData.setRespondent1DQ(respondent1DQ);
        caseData.setRespondent1(respondent1);
        caseData.setRespondent1Copy(respondent1);
        Witnesses respondent1Witnesses = new Witnesses();
        caseData.setRespondent1DQWitnessesSmallClaim(respondent1Witnesses);
        caseData.setRespondent1ResponseDeadline(LocalDateTime.now());
        caseData.setCcdCaseReference(1234L);

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
    void shouldUpdateRespondent1CorrespondenceAddressWhenSolicitorOnePresentAndCorrespondenceNotRequired() {
        when(coreCaseUserService.userHasCaseRole(eq("1234"), anyString(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(
                eq("1234"),
                anyString(),
                eq(RESPONDENTSOLICITORTWO)
        )).thenReturn(false);

        LocalDateTime fixedNow = LocalDateTime.of(2025, 7, 21, 10, 0);
        when(time.now()).thenReturn(fixedNow);

        doReturn(fixedNow.plusMonths(1))
                .when(deadlinesCalculator)
                .calculateApplicantResponseDeadlineSpec(any(LocalDateTime.class));

        Party respondent1 = new Party();
        respondent1.setPrimaryAddress(buildAddress("Old Address", ""));
        respondent1.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSpecAoSRespondentCorrespondenceAddressRequired(NO);
        caseData.setRespondent1(respondent1);
        caseData.setRespondent1Copy(respondent1);
        caseData.setRespondent1DQ(new Respondent1DQ());
        caseData.setRespondent1ResponseDeadline(LocalDateTime.now());
        caseData.setCcdCaseReference(1234L);

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNotNull();
        assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isEqualTo(new Address());
    }

    @Test
    void shouldUpdateRespondent12CorrespondenceAddressWhenSolicitorTwoPresentAndCorrespondenceNotRequired() {
        when(coreCaseUserService.userHasCaseRole(
                eq("1234"),
                anyString(),
                eq(RESPONDENTSOLICITORONE)
        )).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole(eq("1234"), anyString(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        Party respondent1 = new Party();
        respondent1.setPrimaryAddress(buildAddress("Old Address", ""));
        respondent1.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSpecAoSRespondent2CorrespondenceAddressRequired(NO);
        caseData.setRespondent1(respondent1);
        caseData.setRespondent1Copy(respondent1);
        caseData.setRespondent1DQ(new Respondent1DQ());
        caseData.setRespondent2DQ(new Respondent2DQ());
        caseData.setCcdCaseReference(1234L);

        CallbackParams callbackParams = buildCallbackParams(caseData);

        AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

        CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedCaseData.getSpecAoSRespondent2CorrespondenceAddressdetails()).isNotNull();
        assertThat(updatedCaseData.getSpecAoSRespondent2CorrespondenceAddressdetails()).isEqualTo(new Address());
    }

    @Test
    void shouldUpdateRespondent1WitnessesWhenExpertRequiredIsYesAndDetailsPresent() {
        LocalDateTime fixedNow = LocalDateTime.of(2025, 7, 21, 10, 0);
        when(time.now()).thenReturn(fixedNow);

        doReturn(fixedNow.plusMonths(1))
                .when(deadlinesCalculator)
                .calculateApplicantResponseDeadlineSpec(any(LocalDateTime.class));

        Party respondent1 = new Party();
        respondent1.setPrimaryAddress(buildAddress("Old Address", ""));
        respondent1.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResponseClaimExpertSpecRequired(YES);

        Respondent1DQ respondent1DQ = new Respondent1DQ();
        caseData.setRespondent1DQ(respondent1DQ);
        caseData.setRespondent1(respondent1);
        caseData.setRespondent1Copy(respondent1);
        Witnesses respondent1Witnesses = new Witnesses();
        caseData.setRespondent1DQWitnessesSmallClaim(respondent1Witnesses);
        caseData.setRespondent1ResponseDeadline(LocalDateTime.now());
        caseData.setCcdCaseReference(1234L);

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
    void shouldUpdateRespondent1CorrespondenceAddressWhenSolicitorOnePresentAndCorrespondenceNotRequired1() {
        when(coreCaseUserService.userHasCaseRole(eq("1234"), anyString(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(
                eq("1234"),
                anyString(),
                eq(RESPONDENTSOLICITORTWO)
        )).thenReturn(false);

        LocalDateTime fixedNow = LocalDateTime.of(2025, 7, 21, 10, 0);
        when(time.now()).thenReturn(fixedNow);

        doReturn(fixedNow.plusMonths(1))
                .when(deadlinesCalculator)
                .calculateApplicantResponseDeadlineSpec(any(LocalDateTime.class));

        Party respondent1 = new Party();
        respondent1.setPrimaryAddress(buildAddress("Old Address", ""));
        respondent1.setType(Party.Type.INDIVIDUAL);

        Document document = new Document();
        document.setDocumentUrl("fake-url");
        document.setDocumentFileName("file-name");
        document.setDocumentBinaryUrl("binary-url");
        ResponseDocument testDocument = new ResponseDocument();
        testDocument.setFile(document);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setSpecAoSRespondentCorrespondenceAddressRequired(NO);
        caseData.setRespondent1(respondent1);
        caseData.setRespondent1Copy(respondent1);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondToCourtLocation(new RequestedCourt());
        caseData.setRespondent1DQ(respondent1DQ);
        caseData.setRespondent1ResponseDeadline(LocalDateTime.now());
        caseData.setCcdCaseReference(1234L);
        List<Element<CaseDocument>> defendantUploads = new ArrayList<>();
        caseData.setDefendantResponseDocuments(defendantUploads);
        caseData.setCourtLocation(new CourtLocation());
        caseData.setRespondent1SpecDefenceResponseDocument(testDocument);
        caseData.setRespondent2SpecDefenceResponseDocument(testDocument);

        CallbackParams callbackParams = buildCallbackParams(caseData);

        try (MockedStatic<MultiPartyScenario> mockedScenario = mockStatic(MultiPartyScenario.class)) {
            mockedScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData)).thenReturn(
                    ONE_V_TWO_ONE_LEGAL_REP);

            AboutToStartOrSubmitCallbackResponse response = executeCallback(callbackParams);

            CaseData updatedCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNotNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isEqualTo(new Address());
        }
    }

    private Address buildAddress(String line1, String postCode) {
        Address address = new Address();
        address.setAddressLine1(line1);
        address.setPostCode(postCode);
        return address;
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
