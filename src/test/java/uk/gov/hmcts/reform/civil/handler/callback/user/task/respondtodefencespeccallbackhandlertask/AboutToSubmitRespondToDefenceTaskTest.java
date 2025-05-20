package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.DocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class AboutToSubmitRespondToDefenceTaskTest {

    @InjectMocks
    private AboutToSubmitRespondToDefenceTask task;

    @Mock
    private Time time;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    @Mock
    private CourtLocationUtils courtLocationUtils;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private DetermineNextState determineNextState;

    @Mock
    private LocationHelper locationHelper;

    @Mock
    private CaseFlagsInitialiser caseFlagsInitialiser;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private JudgmentByAdmissionOnlineMapper judgmentByAdmissionOnlineMapper;

    @Mock
    private FrcDocumentsUtils frcDocumentsUtils;

    @Mock
    private DQResponseDocumentUtils dqResponseDocumentUtils;

    @Mock
    private UpdateWaCourtLocationsService updateWaCourtLocationsService;

    @Mock
    private RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;

    private final LocalDateTime localDateTime = now();

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        task = new AboutToSubmitRespondToDefenceTask(objectMapper, time, locationRefDataService,
                                                     courtLocationUtils, featureToggleService,
                                                     locationHelper, caseFlagsInitialiser,
                                                     caseDetailsConverter, frcDocumentsUtils,
                                                     dqResponseDocumentUtils, determineNextState,
                                                     Optional.of(updateWaCourtLocationsService),
                                                     requestedCourtForClaimDetailsTab
        );

        Address address = Address.builder()
            .postCode("E11 5BB")
            .build();

        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
            .applicant2(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
            .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
            .respondent2(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
            .build();

        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(oldCaseData);
        when(time.now()).thenReturn(localDateTime);

    }

    @Test
    void shouldSetClaimantResponseDocs() {
        Document document = DocumentBuilder.builder().build();
        CaseData fullDefenceData = CaseDataBuilder.builder().atState(FULL_DEFENCE_PROCEED).build();
        CaseData caseData = fullDefenceData.toBuilder()
            .applicant1DQ(fullDefenceData.getApplicant1DQ().toBuilder()
                              .applicant1DQDraftDirections(document)
                              .build())
            .build();

        List<Element<CaseDocument>> expectedResponseDocuments = List.of(
            Element.<CaseDocument>builder()
                .id(UUID.randomUUID())
                .value(CaseDocument.builder()
                           .documentLink(document)
                           .documentName("doc-name")
                           .createdBy("Claimant")
                           .createdDatetime(LocalDateTime.now())
                           .build())
                .build());

        when(dqResponseDocumentUtils.buildClaimantResponseDocuments(any(CaseData.class))).thenReturn(expectedResponseDocuments);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
        verify(dqResponseDocumentUtils, times(1)).buildClaimantResponseDocuments(any(CaseData.class));
    }

    @Test
    void shouldAddEventAndDateToApplicantExperts() {
        CaseData fullDefenceData = CaseDataBuilder.builder().atState(FULL_DEFENCE_PROCEED).build();
        CaseData caseData = fullDefenceData.toBuilder()
            .applicant1DQ(fullDefenceData.getApplicant1DQ().toBuilder()
                              .applicant1RespondToClaimExperts(
                              ExpertDetails.builder().build())
                              .build())
            .applicant1ResponseDate(LocalDateTime.now())
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
    }

    @Test
    void shouldAddEventAndDateToApplicantWitnesses() {
        Witnesses witnesses = Witnesses.builder()
            .witnessesToAppear(YES)
            .details(wrapElements(Witness.builder().name("John Smith").reasonForWitness("reason").build()))
            .build();

        CaseData fullDefenceData = CaseDataBuilder.builder().atState(FULL_DEFENCE_PROCEED).build();
        CaseData caseData = fullDefenceData.toBuilder()
            .applicant1DQ(fullDefenceData.getApplicant1DQ().toBuilder()
                              .applicant1DQWitnesses(witnesses)
                              .build())
            .applicant1ResponseDate(LocalDateTime.now())
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
    }

    @Test
    void shouldPopulateDQPartyIds() {

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1DQWithExperts()
            .applicant1DQWithWitnesses()
            .atState(FULL_DEFENCE_PROCEED)
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
        assertThat(getCaseData(response)).extracting("applicant1").hasFieldOrProperty("partyID");
        assertThat(getCaseData(response)).extracting("respondent1").hasFieldOrProperty("partyID");
    }

    @Test
    void shouldCallUpdateWaCourtLocationsServiceWhenPresent_AndMintiEnabled() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1DQWithExperts()
            .applicant1DQWithWitnesses()
            .atState(FULL_DEFENCE_PROCEED)
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        verify(updateWaCourtLocationsService).updateCourtListingWALocations(any(), any());
    }

    @Test
    void shouldNotCallUpdateWaCourtLocationsServiceWhenNotPresent_AndMintiEnabled() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        task = new AboutToSubmitRespondToDefenceTask(objectMapper, time, locationRefDataService,
                                                     courtLocationUtils, featureToggleService,
                                                     locationHelper, caseFlagsInitialiser,
                                                     caseDetailsConverter, frcDocumentsUtils,
                                                     dqResponseDocumentUtils, determineNextState,
                                                     Optional.empty(),
                                                     requestedCourtForClaimDetailsTab
        );

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1DQWithExperts()
            .applicant1DQWithWitnesses()
            .atState(FULL_DEFENCE_PROCEED)
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        verifyNoInteractions(updateWaCourtLocationsService);
    }

    @Test
    void shouldSetRespondOptionWhenImmediatePaymentPlanSelected_ApplicantConfirmsNotToProceed1v1() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondent1Represented(NO)
            .respondent1(PartyBuilder.builder().individual().build())
            .specRespondent1Represented(NO)
            .applicant1Represented(YES)
            .applicant1(PartyBuilder.builder().individual().build())
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
        assertThat(getCaseData(response)).extracting("respondForImmediateOption").asString().isEqualTo("YES");
    }

    @Test
    void shouldSetRespondOptionWhenImmediatePartPaymentPlanSelected_ApplicantConfirmsNotToProceed1v1() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .applicant1AcceptAdmitAmountPaidSpec(YES)
            .applicant1DQWithExperts()
            .applicant1DQWithWitnesses()
            .respondent1Represented(NO)
            .respondent1(PartyBuilder.builder().individual().build())
            .specRespondent1Represented(NO)
            .applicant1Represented(YES)
            .applicant1(PartyBuilder.builder().individual().build())
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
        assertThat(getCaseData(response)).extracting("respondForImmediateOption").asString().isEqualTo("YES");
    }

    @Test
    void shouldRemoveNextDeadlin_whenRespondedToDefence() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1DQWithExperts()
            .applicant1DQWithWitnesses()
            .atState(FULL_DEFENCE_PROCEED)
            .nextDeadline(LocalDate.now())
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
        assertThat(getCaseData(response)).extracting("nextDeadline").isNull();
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }

    private CallbackParams callbackParams(CaseData caseData) {

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

        return CallbackParams.builder()
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .caseDetailsBefore(caseDetails)
                         .build())
            .params(Map.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }
}

