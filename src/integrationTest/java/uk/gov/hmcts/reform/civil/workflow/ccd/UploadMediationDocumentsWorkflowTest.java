package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.MediationWorkflowFixtures;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.NON_ATTENDANCE_STATEMENT;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.REFERRED_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.CLAIMANTS_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.DEFENDANTS_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.UploadMediationDocumentsUtils.DEFENDANT_TWO_ID;

@SuppressWarnings({"java:S5960", "java:S6813"})
class UploadMediationDocumentsWorkflowTest extends WorkflowIntegrationTest {

    private static final String APPLICANT_SOLICITOR_ROLE = "[APPLICANTSOLICITORONE]";
    private static final String RESPONDENT_SOLICITOR_ONE_ROLE = "[RESPONDENTSOLICITORONE]";
    private static final String RESPONDENT_SOLICITOR_TWO_ROLE = "[RESPONDENTSOLICITORTWO]";

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUpUploadTest() {
        when(userService.getUserInfo(anyString()))
            .thenReturn(UserInfo.builder()
                            .uid("solicitor-uid")
                            .sub("solicitor@example.com")
                            .roles(List.of("caseworker-civil-solicitor"))
                            .build());
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
    }

    @Nested
    class PartyOptionPopulation {

        @Test
        void shouldPopulateClaimantOptionsForApplicantSolicitor_1v1() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(APPLICANT_SOLICITOR_ROLE));

            CaseData fixture = MediationWorkflowFixtures.inMediation1v1();

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .mid("populate-party-options")
                .then(result -> {
                    assertThat(result.response().getErrors()).isNullOrEmpty();
                    var partyOptions = result.caseData().getUploadMediationDocumentsForm()
                        .getUploadMediationDocumentsPartyChosen();
                    assertThat(partyOptions).isNotNull();
                    assertThat(partyOptions.getListItems()).hasSize(1);
                    assertThat(partyOptions.getListItems().get(0).getCode()).isEqualTo(CLAIMANT_ONE_ID);
                });
        }

        @Test
        void shouldPopulateDefendant1OptionsForRespondentSolicitor1_1v1() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(RESPONDENT_SOLICITOR_ONE_ROLE));

            CaseData fixture = MediationWorkflowFixtures.inMediation1v1();

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .mid("populate-party-options")
                .then(result -> {
                    assertThat(result.response().getErrors()).isNullOrEmpty();
                    var partyOptions = result.caseData().getUploadMediationDocumentsForm()
                        .getUploadMediationDocumentsPartyChosen();
                    assertThat(partyOptions.getListItems()).hasSize(1);
                    assertThat(partyOptions.getListItems().get(0).getCode()).isEqualTo(DEFENDANT_ONE_ID);
                });
        }

        @Test
        void shouldPopulateClaimantOptionsForApplicantSolicitor_2v1() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(APPLICANT_SOLICITOR_ROLE));

            CaseData fixture = MediationWorkflowFixtures.inMediation2v1();

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .mid("populate-party-options")
                .then(result -> {
                    assertThat(result.response().getErrors()).isNullOrEmpty();
                    var partyOptions = result.caseData().getUploadMediationDocumentsForm()
                        .getUploadMediationDocumentsPartyChosen();
                    assertThat(partyOptions.getListItems()).hasSize(3);
                    assertThat(partyOptions.getListItems())
                        .extracting("code")
                        .containsExactlyInAnyOrder(CLAIMANT_ONE_ID, "CLAIMANT_2", CLAIMANTS_ID);
                });
        }

        @Test
        void shouldPopulateDefendantOptionsFor1v2SameSolicitor() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(RESPONDENT_SOLICITOR_ONE_ROLE, RESPONDENT_SOLICITOR_TWO_ROLE));

            CaseData fixture = MediationWorkflowFixtures.inMediation1v2SameSolicitor();

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .mid("populate-party-options")
                .then(result -> {
                    assertThat(result.response().getErrors()).isNullOrEmpty();
                    var partyOptions = result.caseData().getUploadMediationDocumentsForm()
                        .getUploadMediationDocumentsPartyChosen();
                    assertThat(partyOptions.getListItems()).hasSize(3);
                    assertThat(partyOptions.getListItems())
                        .extracting("code")
                        .containsExactlyInAnyOrder(DEFENDANT_ONE_ID, DEFENDANT_TWO_ID, DEFENDANTS_ID);
                });
        }

        @Test
        void shouldPopulateDefendant1OptionsFor1v2DiffSolicitor() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(RESPONDENT_SOLICITOR_ONE_ROLE));

            CaseData fixture = MediationWorkflowFixtures.inMediation1v2DiffSolicitor();

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .mid("populate-party-options")
                .then(result -> {
                    assertThat(result.response().getErrors()).isNullOrEmpty();
                    var partyOptions = result.caseData().getUploadMediationDocumentsForm()
                        .getUploadMediationDocumentsPartyChosen();
                    assertThat(partyOptions.getListItems()).hasSize(1);
                    assertThat(partyOptions.getListItems().get(0).getCode()).isEqualTo(DEFENDANT_ONE_ID);
                });
        }

        @Test
        void shouldPopulateDefendant2OptionsFor1v2DiffSolicitor() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(RESPONDENT_SOLICITOR_TWO_ROLE));

            CaseData fixture = MediationWorkflowFixtures.inMediation1v2DiffSolicitor();

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .mid("populate-party-options")
                .then(result -> {
                    assertThat(result.response().getErrors()).isNullOrEmpty();
                    var partyOptions = result.caseData().getUploadMediationDocumentsForm()
                        .getUploadMediationDocumentsPartyChosen();
                    assertThat(partyOptions.getListItems()).hasSize(1);
                    assertThat(partyOptions.getListItems().get(0).getCode()).isEqualTo(DEFENDANT_TWO_ID);
                });
        }
    }

    @Nested
    class DateValidation {

        @Test
        void shouldRejectFutureDateOnNonAttendanceStatement() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(APPLICANT_SOLICITOR_ROLE));

            CaseData fixture = MediationWorkflowFixtures.withUploadFormFutureDates(
                MediationWorkflowFixtures.inMediation1v1(),
                CLAIMANT_ONE_ID,
                List.of(NON_ATTENDANCE_STATEMENT)
            );

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .mid("validate-dates")
                .then(result ->
                    assertThat(result.response().getErrors())
                        .isNotEmpty()
                        .contains("Document date cannot be in the future")
                );
        }

        @Test
        void shouldRejectFutureDateOnReferredDocuments() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(APPLICANT_SOLICITOR_ROLE));

            CaseData fixture = MediationWorkflowFixtures.withUploadFormFutureDates(
                MediationWorkflowFixtures.inMediation1v1(),
                CLAIMANT_ONE_ID,
                List.of(REFERRED_DOCUMENTS)
            );

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .mid("validate-dates")
                .then(result ->
                    assertThat(result.response().getErrors())
                        .isNotEmpty()
                        .contains("Document date cannot be in the future")
                );
        }

        @Test
        void shouldAcceptPastDates() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(APPLICANT_SOLICITOR_ROLE));

            CaseData fixture = MediationWorkflowFixtures.withUploadForm(
                MediationWorkflowFixtures.inMediation1v1(),
                CLAIMANT_ONE_ID,
                List.of(NON_ATTENDANCE_STATEMENT, REFERRED_DOCUMENTS)
            );

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .mid("validate-dates")
                .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());
        }
    }

    @Nested
    class DocumentStorage {

        @Test
        void shouldStoreNonAttendanceDocsForClaimant1WithCorrectCategoryId() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(APPLICANT_SOLICITOR_ROLE));

            CaseData fixture = MediationWorkflowFixtures.withUploadForm(
                MediationWorkflowFixtures.inMediation1v1(),
                CLAIMANT_ONE_ID,
                List.of(NON_ATTENDANCE_STATEMENT)
            );

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .aboutToSubmit()
                .then(result -> {
                    assertThat(result.response().getErrors()).isNullOrEmpty();
                    assertThat(result.caseData().getApp1MediationNonAttendanceDocs()).isNotEmpty();
                    assertThat(result.caseData().getApp1MediationNonAttendanceDocs().get(0).getValue()
                                   .getDocument().getCategoryID())
                        .isEqualTo("ClaimantOneMediationDocs");
                });
        }

        @Test
        void shouldStoreReferredDocsForDefendant1WithCorrectCategoryId() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(RESPONDENT_SOLICITOR_ONE_ROLE));

            CaseData fixture = MediationWorkflowFixtures.withUploadForm(
                MediationWorkflowFixtures.inMediation1v1(),
                DEFENDANT_ONE_ID,
                List.of(REFERRED_DOCUMENTS)
            );

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .aboutToSubmit()
                .then(result -> {
                    assertThat(result.response().getErrors()).isNullOrEmpty();
                    assertThat(result.caseData().getRes1MediationDocumentsReferred()).isNotEmpty();
                    assertThat(result.caseData().getRes1MediationDocumentsReferred().get(0).getValue()
                                   .getDocument().getCategoryID())
                        .isEqualTo("DefendantOneMediationDocs");
                });
        }

        @Test
        void shouldStoreBothDocTypesForSelectedParty() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(APPLICANT_SOLICITOR_ROLE));

            CaseData fixture = MediationWorkflowFixtures.withUploadForm(
                MediationWorkflowFixtures.inMediation1v1(),
                CLAIMANT_ONE_ID,
                List.of(NON_ATTENDANCE_STATEMENT, REFERRED_DOCUMENTS)
            );

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .aboutToSubmit()
                .then(result -> {
                    assertThat(result.response().getErrors()).isNullOrEmpty();
                    assertThat(result.caseData().getApp1MediationNonAttendanceDocs()).isNotEmpty();
                    assertThat(result.caseData().getApp1MediationDocumentsReferred()).isNotEmpty();
                });
        }

        @Test
        void shouldClearFormAfterSubmission() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(APPLICANT_SOLICITOR_ROLE));

            CaseData fixture = MediationWorkflowFixtures.withUploadForm(
                MediationWorkflowFixtures.inMediation1v1(),
                CLAIMANT_ONE_ID,
                List.of(NON_ATTENDANCE_STATEMENT)
            );

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .aboutToSubmit()
                .then(result -> {
                    assertThat(result.response().getErrors()).isNullOrEmpty();
                    assertThat(result.response().getData())
                        .doesNotContainKey("uploadMediationDocumentsPartyChosen");
                    assertThat(result.response().getData())
                        .doesNotContainKey("nonAttendanceStatementForm");
                    assertThat(result.response().getData())
                        .doesNotContainKey("documentsReferredForm");
                });
        }

        @Test
        void shouldStoreDocsForBothDefendantsWhen1v2SameSolicitor() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(RESPONDENT_SOLICITOR_ONE_ROLE, RESPONDENT_SOLICITOR_TWO_ROLE));

            CaseData fixture = MediationWorkflowFixtures.withUploadForm(
                MediationWorkflowFixtures.inMediation1v2SameSolicitor(),
                DEFENDANTS_ID,
                List.of(NON_ATTENDANCE_STATEMENT)
            );

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .aboutToSubmit()
                .then(result -> {
                    assertThat(result.response().getErrors()).isNullOrEmpty();
                    assertThat(result.caseData().getRes1MediationNonAttendanceDocs()).isNotEmpty();
                    assertThat(result.caseData().getRes2MediationNonAttendanceDocs()).isNotEmpty();
                });
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnConfirmationResponse() throws Exception {
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(List.of(APPLICANT_SOLICITOR_ROLE));

            CaseData fixture = MediationWorkflowFixtures.withUploadForm(
                MediationWorkflowFixtures.inMediation1v1(),
                CLAIMANT_ONE_ID,
                List.of(NON_ATTENDANCE_STATEMENT)
            );

            startWorkflow(fixture)
                .eventId(CaseEvent.UPLOAD_MEDIATION_DOCUMENTS)
                .aboutToSubmit()
                .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty())
                .submitted()
                .then(result -> {
                    assertThat(result.submittedResponse()).isNotNull();
                    assertThat(result.submittedResponse().get("confirmation_header").asText())
                        .contains("Documents uploaded");
                });
        }
    }
}
