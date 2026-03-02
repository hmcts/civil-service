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
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
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
import uk.gov.hmcts.reform.civil.service.PaymentDateService;
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

    @Mock
    private PaymentDateService paymentDateService;

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
                                                     requestedCourtForClaimDetailsTab,
                                                     paymentDateService
        );

        Address address = new Address();
        address.setPostCode("E11 5BB");
        Party party = new Party();
        party.setPartyName("name");
        party.setType(INDIVIDUAL);
        party.setPrimaryAddress(address);

        CaseData oldCaseData = CaseDataBuilder.builder()
            .applicant1(party)
            .applicant2(party)
            .respondent1(party)
            .respondent2(party)
            .build();

        when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(oldCaseData);
        when(time.now()).thenReturn(localDateTime);

    }

    @Test
    void shouldSetClaimantResponseDocs() {
        Document document = DocumentBuilder.builder().build();
        CaseData caseData = CaseDataBuilder.builder().atState(FULL_DEFENCE_PROCEED).build();
        caseData.getApplicant1DQ().setApplicant1DQDraftDirections(document);

        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentLink(document);
        caseDocument.setDocumentName("doc-name");
        caseDocument.setCreatedBy("Claimant");
        caseDocument.setCreatedDatetime(LocalDateTime.now());
        Element<CaseDocument> element = new Element<>();
        element.setId(UUID.randomUUID());
        element.setValue(caseDocument);

        List<Element<CaseDocument>> expectedResponseDocuments = List.of(element);

        when(dqResponseDocumentUtils.buildClaimantResponseDocuments(any(CaseData.class))).thenReturn(expectedResponseDocuments);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
        verify(dqResponseDocumentUtils, times(1)).buildClaimantResponseDocuments(any(CaseData.class));
    }

    @Test
    void shouldAddEventAndDateToApplicantExperts() {
        CaseData caseData = CaseDataBuilder.builder().atState(FULL_DEFENCE_PROCEED).build();
        caseData.getApplicant1DQ().setApplicant1RespondToClaimExperts(new ExpertDetails());
        caseData.setApplicant1ResponseDate(LocalDateTime.now());

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
    }

    @Test
    void shouldAddEventAndDateToApplicantWitnesses() {
        Witnesses witnesses = new Witnesses();
        witnesses.setWitnessesToAppear(YES);
        Witness witness = new Witness();
        witness.setName("John Smith");
        witness.setReasonForWitness("reason");
        witnesses.setDetails(wrapElements(witness));

        CaseData caseData = CaseDataBuilder.builder().atState(FULL_DEFENCE_PROCEED).build();
        caseData.getApplicant1DQ().setApplicant1DQWitnesses(witnesses);
        caseData.setApplicant1ResponseDate(LocalDateTime.now());

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
            .applicant1ResponseDate(LocalDateTime.now())
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
            .applicant1ResponseDate(LocalDateTime.now())
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
                                                     requestedCourtForClaimDetailsTab,
                                                     paymentDateService
        );

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1DQWithExperts()
            .applicant1DQWithWitnesses()
            .atState(FULL_DEFENCE_PROCEED)
            .applicant1ResponseDate(LocalDateTime.now())
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        verifyNoInteractions(updateWaCourtLocationsService);
    }

    @Test
    void shouldSetRespondOptionWhenImmediatePaymentPlanSelected_ApplicantConfirmsNotToProceed1v1() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
        caseLocationCivil.setBaseLocation("0123");
        caseLocationCivil.setRegion("0321");
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondent1Represented(NO)
            .respondent1(PartyBuilder.builder().individual().build())
            .specRespondent1Represented(NO)
            .applicant1Represented(YES)
            .applicant1(PartyBuilder.builder().individual().build())
            .caseManagementLocation(caseLocationCivil)
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
        assertThat(getCaseData(response)).extracting("respondForImmediateOption").asString().isEqualTo("YES");
    }

    @Test
    void shouldSetRespondOptionWhenImmediatePartPaymentPlanSelected_ApplicantConfirmsNotToProceed1v1() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(paymentDateService.calculatePaymentDeadline()).thenReturn(LocalDate.now().plusDays(5));

        CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
        caseLocationCivil.setBaseLocation("0123");
        caseLocationCivil.setRegion("0321");
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
            .caseManagementLocation(caseLocationCivil)
            .applicant1ResponseDate(LocalDateTime.now())
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
            .applicant1ResponseDate(LocalDateTime.now())
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
        assertThat(getCaseData(response)).extracting("nextDeadline").isNull();
    }

    @Test
    void shouldSetPaymentDeadlineWhenDefendantProposesImmediatePartPaymentPlanAndClaimantAcceptsIt() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(paymentDateService.calculatePaymentDeadline()).thenReturn(LocalDate.now().plusDays(5));

        CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
        caseLocationCivil.setBaseLocation("0123");
        caseLocationCivil.setRegion("0321");
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
            .caseManagementLocation(caseLocationCivil)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YES)
            .applicant1ResponseDate(LocalDateTime.now())
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        assertNotNull(response);
        CaseData caseData1 = getCaseData(response);
        assertThat(caseData1.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid()).isNotNull();
    }

    @Test
    void shouldSetApplicant1DefenceResponseDocumentCategory() {
        // Given
        Document document = new Document()
            .setDocumentUrl("http://dm-store/documents/123")
            .setDocumentBinaryUrl("http://dm-store/documents/123/binary")
            .setDocumentFileName("defence-response.pdf");

        ResponseDocument responseDocument = new ResponseDocument(document);

        CaseData caseData = CaseData.builder()
            .applicant1DefenceResponseDocumentSpec(responseDocument)
            .build();

        caseData.setApplicant1DefenceResponseDocumentSpec(responseDocument);

        // When
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) task.execute(callbackParams(caseData));

        // Then
        assertNotNull(response);

        @SuppressWarnings("unchecked")
        Map<String, Object> responseDocumentData =
            (Map<String, Object>) response.getData().get("applicant1DefenceResponseDocumentSpec");
        assertThat(responseDocumentData).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> file = (Map<String, Object>) responseDocumentData.get("file");
        assertThat(file.get("categoryID")).isEqualTo("directionsQuestionnaire");
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }

    private CallbackParams callbackParams(CaseData caseData) {

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

        return new CallbackParams()
            .caseData(caseData)
            .request(CallbackRequest.builder()
                         .caseDetailsBefore(caseDetails)
                         .build())
            .params(Map.of(BEARER_TOKEN, BEARER_TOKEN));
    }
}
