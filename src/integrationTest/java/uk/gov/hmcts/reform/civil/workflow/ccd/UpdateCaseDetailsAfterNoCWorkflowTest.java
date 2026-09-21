package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.UpdateCaseDetailsAfterNoCFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("java:S5960")
class UpdateCaseDetailsAfterNoCWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private OrganisationService organisationService;

    @BeforeEach
    void setUp() {
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(false);
    }

    @Test
    void shouldUpdateRespondent1DetailsAfterNoC() throws Exception {
        CaseData fixture = UpdateCaseDetailsAfterNoCFixtures.respondent1SolicitorReplaced();

        startWorkflow(fixture)
            .eventId(CaseEvent.UPDATE_CASE_DETAILS_AFTER_NOC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData caseData = result.caseData();
                assertThat(caseData.getRespondent1OrgRegistered()).isEqualTo(YesOrNo.YES);
                assertThat(caseData.getRespondent1Represented()).isEqualTo(YesOrNo.YES);
                assertThat(caseData.getDefendant1LIPAtClaimIssued()).isEqualTo(YesOrNo.NO);
                assertThat(caseData.getAnyRepresented()).isEqualTo(YesOrNo.YES);

                assertThat(caseData.getRespondentSolicitor1EmailAddress())
                    .isEqualTo("newsolicitor@example.com");

                assertThat(caseData.getSolicitorReferences().getRespondentSolicitor1Reference()).isNull();
                assertThat(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
                    .isEqualTo("APP-REF-001");

                assertThat(caseData.getRespondent1OrganisationPolicy().getOrgPolicyReference()).isNull();

                assertThat(result.response().getData()).doesNotContainKey("changeOrganisationRequestField");
            });
    }

    @Test
    void shouldUpdateApplicantDetailsAfterNoC() throws Exception {
        CaseData fixture = UpdateCaseDetailsAfterNoCFixtures.applicantSolicitorReplaced();

        startWorkflow(fixture)
            .eventId(CaseEvent.UPDATE_CASE_DETAILS_AFTER_NOC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData caseData = result.caseData();
                assertThat(caseData.getApplicantSolicitor1PbaAccounts()).isNull();
                assertThat(caseData.getApplicantSolicitor1PbaAccountsIsEmpty()).isEqualTo(YesOrNo.YES);
                assertThat(caseData.getAnyRepresented()).isEqualTo(YesOrNo.YES);

                assertThat(caseData.getApplicantSolicitor1UserDetails().getEmail())
                    .isEqualTo("newsolicitor@example.com");

                assertThat(caseData.getSolicitorReferences().getApplicantSolicitor1Reference()).isNull();
                assertThat(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
                    .isEqualTo("RES1-REF-001");

                assertThat(caseData.getApplicant1OrganisationPolicy().getOrgPolicyReference()).isNull();
            });
    }

    @Test
    void shouldUnassignLipDefendantRole() throws Exception {
        CaseData fixture = UpdateCaseDetailsAfterNoCFixtures.lipDefendantGainingSolicitor();

        startWorkflow(fixture)
            .eventId(CaseEvent.UPDATE_CASE_DETAILS_AFTER_NOC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                verify(coreCaseUserService).unassignCase(
                    eq(fixture.getCcdCaseReference().toString()),
                    eq("def-user-id"),
                    eq(null),
                    eq(CaseRole.DEFENDANT)
                );
            });
    }

    @Test
    void shouldUnassignLipClaimantRoleAndSetRepresented() throws Exception {
        CaseData fixture = UpdateCaseDetailsAfterNoCFixtures.lipClaimantGainingSolicitor();

        startWorkflow(fixture)
            .eventId(CaseEvent.UPDATE_CASE_DETAILS_AFTER_NOC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                verify(coreCaseUserService).unassignCase(
                    eq(fixture.getCcdCaseReference().toString()),
                    eq("claimant-user-id"),
                    eq(null),
                    eq(CaseRole.CLAIMANT)
                );

                assertThat(result.caseData().getApplicant1Represented()).isEqualTo(YesOrNo.YES);
            });
    }

    @Test
    void shouldReturnErrorWhenChangeOfRepresentationMissing() throws Exception {
        CaseData fixture = UpdateCaseDetailsAfterNoCFixtures.missingChangeOfRepresentation();

        startWorkflow(fixture)
            .eventId(CaseEvent.UPDATE_CASE_DETAILS_AFTER_NOC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors())
                    .containsExactly("No Notice of Change events recorded");
                verifyNoInteractions(coreCaseUserService);
            });
    }

    @Test
    void shouldUpdateRespondent1DetailsForUnspecClaim() throws Exception {
        CaseData fixture = UpdateCaseDetailsAfterNoCFixtures.respondent1SolicitorReplacedUnspec();

        startWorkflow(fixture)
            .eventId(CaseEvent.UPDATE_CASE_DETAILS_AFTER_NOC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData caseData = result.caseData();
                assertThat(caseData.getRespondentSolicitor1ServiceAddressRequired()).isEqualTo(YesOrNo.NO);
                assertThat(caseData.getRespondent1OrganisationIDCopy()).isEqualTo("NEW-ORG-001");
            });
    }
}
