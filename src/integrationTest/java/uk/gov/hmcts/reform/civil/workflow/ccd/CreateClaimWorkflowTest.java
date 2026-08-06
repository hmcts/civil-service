package uk.gov.hmcts.reform.civil.workflow.ccd;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.repositories.CasemanReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.CreateClaimFixtures;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.UNREPRESENTED_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.UNREPRESENTED_DEFENDANT_TWO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.CALLBACK_TIME;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.CLAIM_REFERENCE;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.RESPONDENT_ONE_ORGANISATION;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.RESPONDENT_TWO_ORGANISATION;

@SuppressWarnings({"java:S5960", "java:S6813"})
class CreateClaimWorkflowTest extends WorkflowIntegrationTest {

    private static final String PREFERRED_LOCATION_LABEL =
        "Central London County Court - Thomas More Building - WC2A 2LL";
    private static final LocationRefData PREFERRED_LOCATION = new LocationRefData()
        .setCourtLocationCode("312")
        .setEpimmsId("214320")
        .setRegionId("10")
        .setSiteName("Central London County Court")
        .setCourtAddress("Thomas More Building")
        .setPostcode("WC2A 2LL");

    @MockBean
    private TelemetryClient telemetryClient;

    @MockBean
    private Time time;

    @MockBean
    private CasemanReferenceNumberRepository referenceNumberRepository;

    @MockBean
    private LocationReferenceDataService locationReferenceDataService;

    @MockBean
    private CourtLocationUtils courtLocationUtils;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private IStateFlowEngine stateFlowEngine;

    @Autowired
    private ToggleConfiguration toggleConfiguration;

    @BeforeEach
    void setUpCreateClaimLifecycle() {
        when(time.now()).thenReturn(CALLBACK_TIME);
        when(referenceNumberRepository.next("unspec")).thenReturn(CLAIM_REFERENCE);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("applicant-solicitor-id")
            .email("applicantsolicitor@example.com")
            .build());
        when(locationReferenceDataService.getCourtLocationsForDefaultJudgments(anyString(), anyString()))
            .thenReturn(List.of(PREFERRED_LOCATION));
        when(courtLocationUtils.findPreferredLocationData(any(), any()))
            .thenReturn(PREFERRED_LOCATION);
        when(courtLocationUtils.getLocationsFromList(any()))
            .thenReturn(DynamicList.fromList(List.of(PREFERRED_LOCATION_LABEL)));
        when(locationReferenceDataService.getCourtLocationsByEpimmsId(anyString(), anyString(), anyString()))
            .thenReturn(List.of(PREFERRED_LOCATION));
    }

    @Test
    void shouldExecuteCreateClaimAboutToStartThenStartClaimMidWorkflow() throws Exception {
        startWorkflow(CreateClaimFixtures.unspecifiedClaimStart())
            .eventId(CaseEvent.CREATE_CLAIM)
            .aboutToStart()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty())
            .mid("start-claim")
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getClaimStarted()).isEqualTo(YES);
                assertThat(result.caseData().getFeatureToggleWA()).isEqualTo(toggleConfiguration.getFeatureToggle());
                assertThat(result.caseData().getCourtLocation().getApplicantPreferredCourtLocationList().getListItems())
                    .extracting("label")
                    .containsExactly(PREFERRED_LOCATION_LABEL);
                assertThat(result.response().getData()).containsKeys(
                    "claimStarted",
                    "featureToggleWA",
                    "courtLocation"
                );
            });
    }

    @Test
    void shouldCreateRepresentedClaimAndReturnPaymentConfirmation() throws Exception {
        CaseData claimDraft = CreateClaimFixtures.representedOneVOneClaimDraft();
        assertThat(claimDraft.getCaseAccessCategory()).isNull();

        startWorkflow(claimDraft)
            .eventId(CaseEvent.CREATE_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData()).containsKeys(
                    "businessProcess",
                    "legacyCaseReference",
                    "submittedDate",
                    "CaseAccessCategory",
                    "respondent1OrganisationIDCopy"
                );
                assertThat(caseData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY, CREATE_SERVICE_REQUEST_CLAIM.name());
                assertThat(caseData.getLegacyCaseReference()).isEqualTo(CLAIM_REFERENCE);
                assertThat(caseData.getSubmittedDate()).isEqualTo(CALLBACK_TIME);
                assertThat(caseData.getClaimType()).isEqualTo(ClaimType.CLINICAL_NEGLIGENCE);
                assertThat(caseData.getAllocatedTrack()).isEqualTo(AllocatedTrack.MULTI_CLAIM);
                assertThat(caseData.getCaseAccessCategory()).isEqualTo(UNSPEC_CLAIM);
                assertThat(result.response().getData()).doesNotContainKeys(
                    "claimStarted",
                    "uiStatementOfTruth",
                    "applicantSolicitor1CheckEmail"
                );
                assertThat(caseData.getApplicantSolicitor1ClaimStatementOfTruth())
                    .extracting("name", "role")
                    .containsExactly("Applicant Solicitor", "Solicitor");
                assertThat(caseData.getApplicantSolicitor1UserDetails())
                    .extracting("id", "email")
                    .containsExactly("applicant-solicitor-id", "hmcts.civil@gmail.com");
                assertThat(caseData.getRespondent1OrganisationIDCopy())
                    .isEqualTo(RESPONDENT_ONE_ORGANISATION);
                assertThat(caseData.getRespondent1OrganisationPolicy().getOrganisation()).isNull();
                assertThat(caseData.getDefendant1LIPAtClaimIssued()).isEqualTo(NO);
                assertThat(caseData.getAnyRepresented()).isEqualTo(YES);
                assertThat(caseData.getCaseManagementLocation())
                    .extracting("region", "baseLocation")
                    .containsExactly("2", "420219");
                assertState(caseData, CLAIM_SUBMITTED.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Please now pay your claim fee", "using the link below");
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains("Your claim will not be issued until payment is confirmed")
                    .doesNotContain("litigant in person");
            });
    }

    @Test
    void shouldCreateRepresentedClaimWithTwoApplicants() throws Exception {
        String expectedCaseName = "John Rambo, Jason Rambo v Sole Trader T/A Sole Trader co";

        startWorkflow(CreateClaimFixtures.representedTwoVOneClaimDraft())
            .eventId(CaseEvent.CREATE_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData()).containsKeys(
                    "applicant2",
                    "allPartyNames",
                    "caseNameHmctsInternal",
                    "caseNamePublic"
                );
                assertThat(caseData.getApplicant2().getPartyID()).isNotBlank().hasSize(16);
                assertThat(caseData.getApplicant2().getFlags()).isNotNull();
                assertThat(caseData.getAllPartyNames())
                    .isEqualTo("Mr. John Rambo, Mr. Jason Rambo V Mr. Sole Trader T/A Sole Trader co");
                assertThat(caseData.getCaseNameHmctsInternal()).isEqualTo(expectedCaseName);
                assertThat(caseData.getCaseNamePublic()).isEqualTo(expectedCaseName);
                assertThat(caseData.getRespondent1OrganisationIDCopy())
                    .isEqualTo(RESPONDENT_ONE_ORGANISATION);
                assertState(caseData, CLAIM_SUBMITTED.fullName());
            });
    }

    @Test
    void shouldCreateRepresentedClaimWithTwoDifferentDefendantSolicitors() throws Exception {
        startWorkflow(CreateClaimFixtures.representedTwoSolicitorClaimDraft())
            .eventId(CaseEvent.CREATE_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(caseData.getRespondent1OrganisationIDCopy())
                    .isEqualTo(RESPONDENT_ONE_ORGANISATION);
                assertThat(caseData.getRespondent2OrganisationIDCopy())
                    .isEqualTo(RESPONDENT_TWO_ORGANISATION);
                assertThat(caseData.getRespondent1OrganisationPolicy().getOrganisation()).isNull();
                assertThat(caseData.getRespondent2OrganisationPolicy().getOrganisation()).isNull();
                assertThat(caseData.getDefendant1LIPAtClaimIssued()).isEqualTo(NO);
                assertThat(caseData.getDefendant2LIPAtClaimIssued()).isEqualTo(NO);
                assertState(caseData, CLAIM_SUBMITTED.fullName());
            });
    }

    @Test
    void shouldCreateRepresentedClaimWithOneSolicitorForBothDefendants() throws Exception {
        CaseData claimDraft = CreateClaimFixtures.representedSameSolicitorClaimDraft();
        assertThat(claimDraft.getRespondentSolicitor1ServiceAddressRequired()).isEqualTo(YES);
        assertThat(claimDraft.getRespondentSolicitor1ServiceAddress()).isNotNull();
        assertThat(claimDraft.getRespondentSolicitor2ServiceAddressRequired()).isNull();
        assertThat(claimDraft.getRespondentSolicitor2ServiceAddress()).isNull();

        startWorkflow(claimDraft)
            .eventId(CaseEvent.CREATE_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(caseData.getRespondent1OrganisationIDCopy())
                    .isEqualTo(RESPONDENT_ONE_ORGANISATION);
                assertThat(caseData.getRespondent2OrganisationIDCopy())
                    .isEqualTo(RESPONDENT_ONE_ORGANISATION);
                assertThat(caseData.getRespondent2OrganisationPolicy().getOrganisation()).isNull();
                assertThat(caseData.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole())
                    .isEqualTo("[RESPONDENTSOLICITORTWO]");
                assertThat(caseData.getRespondent2OrganisationPolicy().getOrgPolicyReference())
                    .isEqualTo(caseData.getRespondent1OrganisationPolicy().getOrgPolicyReference());
                assertThat(caseData.getRespondentSolicitor2EmailAddress())
                    .isEqualTo(caseData.getRespondentSolicitor1EmailAddress());
                assertThat(caseData.getSolicitorReferences().getRespondentSolicitor2Reference())
                    .isEqualTo(caseData.getSolicitorReferences().getRespondentSolicitor1Reference());
                assertThat(caseData.getRespondentSolicitor2ServiceAddressRequired())
                    .isEqualTo(caseData.getRespondentSolicitor1ServiceAddressRequired());
                assertThat(caseData.getRespondentSolicitor2ServiceAddress())
                    .isEqualTo(caseData.getRespondentSolicitor1ServiceAddress());
                assertState(caseData, CLAIM_SUBMITTED.fullName());
            });
    }

    @Test
    void shouldCreateOneVOneLipClaimAndUseSingleUnrepresentedDefendantStateFlow() throws Exception {
        startWorkflow(CreateClaimFixtures.oneVOneLipClaimDraft())
            .eventId(CaseEvent.CREATE_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(caseData.getAddRespondent2()).isEqualTo(NO);
                assertThat(caseData.getRespondent2()).isNull();
                assertThat(caseData.getRespondent1Represented()).isEqualTo(NO);
                assertThat(caseData.getRespondent1OrgRegistered()).isEqualTo(NO);
                assertThat(caseData.getDefendant1LIPAtClaimIssued()).isEqualTo(YES);
                assertThat(caseData.getRespondent1OrganisationIDCopy()).isNull();
                assertThat(caseData.getRespondent1OrganisationPolicy())
                    .extracting("organisation", "orgPolicyCaseAssignedRole")
                    .containsExactly(null, "[RESPONDENTSOLICITORONE]");

                StateFlowDTO stateFlow = assertState(caseData, CLAIM_SUBMITTED.fullName());
                assertThat(stateFlow.isFlagSet(UNREPRESENTED_DEFENDANT_ONE)).isTrue();
                assertThat(stateFlow.isFlagSet(UNREPRESENTED_DEFENDANT_TWO)).isFalse();
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Please now pay your claim fee", "using the link below");
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains(
                        "Your claim will not be issued until payment is confirmed",
                        "litigant in person"
                    );
            });
    }

    @Test
    void shouldCreateMixedRepresentationClaimAndRecordTheLipAtIssue() throws Exception {
        startWorkflow(CreateClaimFixtures.mixedRepresentationClaimDraft())
            .eventId(CaseEvent.CREATE_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData()).containsKeys(
                    "defendant1LIPAtClaimIssued",
                    "defendant2LIPAtClaimIssued",
                    "respondent1OrganisationIDCopy",
                    "respondent2OrganisationPolicy"
                );
                assertThat(caseData.getDefendant1LIPAtClaimIssued()).isEqualTo(NO);
                assertThat(caseData.getDefendant2LIPAtClaimIssued()).isEqualTo(YES);
                assertThat(caseData.getRespondent1OrganisationIDCopy())
                    .isEqualTo(RESPONDENT_ONE_ORGANISATION);
                assertThat(caseData.getRespondent2OrganisationIDCopy()).isNull();
                assertThat(caseData.getRespondent2OrganisationPolicy().getOrganisation()).isNull();
                assertThat(caseData.getRespondent1DetailsForClaimDetailsTab().getFlags()).isNull();
                assertThat(caseData.getRespondent2DetailsForClaimDetailsTab().getFlags()).isNull();
                assertState(caseData, CLAIM_SUBMITTED.fullName());
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse().path("confirmation_body").asText())
                .contains(
                    "Your claim will not be issued until payment is confirmed",
                    "litigant in person"
                ));
    }

    @Test
    void shouldCreateTwoLipClaimAndRecordBothPartiesAtIssue() throws Exception {
        CaseData claimDraft = CreateClaimFixtures.twoLipClaimDraft();
        assertThat(claimDraft.getDefendant1LIPAtClaimIssued()).isNull();
        assertThat(claimDraft.getDefendant2LIPAtClaimIssued()).isNull();
        assertThat(claimDraft.getRespondent2().getPartyID()).isNull();

        startWorkflow(claimDraft)
            .eventId(CaseEvent.CREATE_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(caseData.getDefendant1LIPAtClaimIssued()).isEqualTo(YES);
                assertThat(caseData.getDefendant2LIPAtClaimIssued()).isEqualTo(YES);
                assertThat(caseData.getRespondent2().getPartyID()).isNotBlank().hasSize(16);
                assertThat(caseData.getRespondent1OrganisationIDCopy()).isNull();
                assertThat(caseData.getRespondent2OrganisationIDCopy()).isNull();
                assertState(caseData, CLAIM_SUBMITTED.fullName());
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse().path("confirmation_body").asText())
                .contains(
                    "Your claim will not be issued until payment is confirmed",
                    "litigant in person"
                ));
    }

    private StateFlowDTO assertState(CaseData caseData, String expectedState) {
        StateFlowDTO stateFlow = stateFlowEngine.getStateFlow(caseData);
        assertThat(stateFlow.getState().getName())
            .as("state history: %s", stateFlow.getStateHistory())
            .isEqualTo(expectedState);
        return stateFlow;
    }
}
