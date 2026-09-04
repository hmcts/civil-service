package uk.gov.hmcts.reform.civil.workflow.ga;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.service.GaEventEmitterService;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.GeneralAppsDeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.LocationService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.GaCreationFixtures;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("java:S5960")
class InitiateGeneralApplicationWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CrossAccessUserConfiguration crossAccessUserConfiguration;

    @MockBean
    private LocationReferenceDataService locationReferenceDataService;

    @MockBean
    private LocationService locationService;

    @MockBean
    private GeneralAppsDeadlinesCalculator deadlinesCalculator;

    @MockBean
    private GeneralAppFeesService feesService;

    @MockBean
    private GaEventEmitterService gaEventEmitterService;

    @BeforeEach
    void setUpExternalDependencies() {
        when(userService.getUserDetails(BEARER_TOKEN)).thenReturn(UserDetails.builder()
                                                                 .id(GaCreationFixtures.APPLICANT_USER_ID)
                                                                 .email(GaCreationFixtures.APPLICANT_EMAIL)
                                                                 .forename("Alex")
                                                                 .surname("Applicant")
                                                                 .build());
        when(authTokenGenerator.generate()).thenReturn("service-auth-token");
        when(caseAssignmentApi.getUserRoles(anyString(), anyString(), anyList()))
            .thenReturn(CaseAssignmentUserRolesResource.builder()
                            .caseAssignmentUserRoles(List.of(
                                assignment(
                                    GaCreationFixtures.APPLICANT_USER_ID,
                                    CaseRole.APPLICANTSOLICITORONE
                                ),
                                assignment(
                                    GaCreationFixtures.RESPONDENT_USER_ID,
                                    CaseRole.RESPONDENTSOLICITORONE
                                )
                            ))
                            .build());

        LocationRefData court = new LocationRefData()
            .setSiteName("Central London County Court")
            .setCourtAddress("Thomas More Building")
            .setPostcode("EC4A 3TR")
            .setEpimmsId("123456")
            .setRegionId("1");
        when(locationReferenceDataService.getCourtLocationsForGeneralApplication(anyString(), anyString()))
            .thenReturn(List.of(court));
        when(locationService.getWorkAllocationLocation(any(), anyString())).thenReturn(Pair.of(
            new CaseLocationCivil()
                .setRegion("1")
                .setBaseLocation("123456")
                .setSiteName("Central London County Court")
                .setAddress("Thomas More Building")
                .setPostcode("EC4A 3TR"),
            false
        ));
        when(deadlinesCalculator.calculateApplicantResponseDeadline(any(LocalDateTime.class), anyInt()))
            .thenReturn(GaCreationFixtures.APPLICATION_DEADLINE.minusDays(1));
        when(deadlinesCalculator.calculateApplicantResponseDeadlineWithWeekendCheck(
            any(LocalDateTime.class),
            anyInt()
        )).thenReturn(GaCreationFixtures.APPLICATION_DEADLINE);
        when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(anyInt(), any(LocalDate.class)))
            .thenReturn(GaCreationFixtures.CLAIM_DISMISSED_DEADLINE);
        when(feesService.getFeeForGA(any(uk.gov.hmcts.reform.civil.model.CaseData.class)))
            .thenReturn(GaCreationFixtures.APPLICATION_FEE);
        when(feesService.isFreeGa(any(GeneralApplication.class))).thenReturn(false);
    }

    @Test
    void shouldValidateAssignmentAndInitialiseCourtLocations() throws Exception {
        startWorkflow(GaCreationFixtures.applicationInput())
            .eventId(CaseEvent.INITIATE_GENERAL_APPLICATION)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getGeneralAppHearingDetails()
                               .getHearingPreferredLocation().getListItems())
                    .extracting("label")
                    .containsExactly("Central London County Court - Thomas More Building - EC4A 3TR");
            });
    }

    @Test
    void shouldCreateTheApplicationAndReturnPaymentConfirmation() throws Exception {
        startWorkflow(GaCreationFixtures.applicationInput())
            .eventId(CaseEvent.INITIATE_GENERAL_APPLICATION)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getGeneralApplications()).singleElement().satisfies(element -> {
                    GeneralApplication application = element.getValue();
                    assertThat(application.getGeneralAppType().getTypes())
                        .containsExactly(GeneralApplicationTypes.EXTEND_TIME);
                    assertThat(application.getGeneralAppDetailsOfOrder())
                        .isEqualTo("Extend the deadline by 14 days");
                    assertThat(application.getGeneralAppReasonsOfOrder())
                        .isEqualTo("The parties require additional time");
                    assertThat(application.getBusinessProcess())
                        .extracting("status", "camundaEvent")
                        .containsExactly(BusinessProcessStatus.READY, CaseEvent.INITIATE_GENERAL_APPLICATION.name());
                    assertThat(application.getGeneralAppDateDeadline())
                        .isEqualTo(GaCreationFixtures.APPLICATION_DEADLINE);
                    assertThat(application.getGeneralAppSubmittedDateGAspec()).isNotNull();
                    assertThat(application.getCaseManagementLocation())
                        .extracting("region", "baseLocation", "siteName", "postcode")
                        .containsExactly("1", "123456", "Central London County Court", "EC4A 3TR");
                    assertThat(application.getCaseManagementCategory().getValue())
                        .extracting("code", "label")
                        .containsExactly("Civil", "Civil");
                    assertThat(application.getGeneralAppSuperClaimType()).isEqualTo("UNSPEC_CLAIM");
                    assertThat(application.getGaWaTrackLabel()).isEqualTo(" - Fast Track");
                    assertThat(application.getApplicantPartyName()).isEqualTo("Applicant Limited");
                    assertThat(application.getLitigiousPartyID()).isEqualTo("001");
                    assertThat(application.getParentClaimantIsApplicant()).isEqualTo(YesOrNo.YES);
                    assertThat(application.getGeneralAppApplnSolicitor())
                        .extracting("id", "email", "organisationIdentifier")
                        .containsExactly(
                            GaCreationFixtures.APPLICANT_USER_ID,
                            GaCreationFixtures.APPLICANT_EMAIL,
                            GaCreationFixtures.APPLICANT_ORGANISATION
                        );
                    assertThat(application.getGeneralAppRespondentSolicitors()).singleElement().satisfies(solicitor ->
                        assertThat(solicitor.getValue())
                            .extracting("id", "email", "organisationIdentifier")
                            .containsExactly(
                                GaCreationFixtures.RESPONDENT_USER_ID,
                                GaCreationFixtures.RESPONDENT_EMAIL,
                                GaCreationFixtures.RESPONDENT_ORGANISATION
                            )
                    );
                    assertThat(application.getGeneralAppEvidenceDocument()).singleElement().satisfies(document ->
                        assertThat(document.getValue().getDocumentFileName()).isEqualTo("supporting-evidence.pdf")
                    );
                });

                assertThat(result.caseData().getClaimDismissedDeadline())
                    .isEqualTo(GaCreationFixtures.CLAIM_DISMISSED_DEADLINE);
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .isEqualTo("# You have submitted an application");
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains("Your application fee of", "275.00 is now due for payment")
                    .contains("/cases/case-details/" + GaCreationFixtures.CASE_ID + "#Applications");
            });

        verify(gaEventEmitterService).emitBusinessProcessCamundaEvent(
            eq(GaCreationFixtures.CASE_ID),
            any(GeneralApplication.class),
            eq(false)
        );
    }

    private CaseAssignmentUserRole assignment(String userId, CaseRole role) {
        return CaseAssignmentUserRole.builder()
            .caseDataId(Long.toString(GaCreationFixtures.CASE_ID))
            .userId(userId)
            .caseRole(role.getFormattedName())
            .build();
    }
}
