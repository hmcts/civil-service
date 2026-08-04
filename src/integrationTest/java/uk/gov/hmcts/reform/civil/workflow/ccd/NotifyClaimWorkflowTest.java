package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.NotifyClaimFixtures;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.NotifyClaimCallbackHandler.WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.CALLBACK_TIME;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.CLAIM_REFERENCE;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.DEFENDANT_ONE_OPTION;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.RESPONDENT_ONE_ORGANISATION;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.RESPONDENT_TWO_ORGANISATION;

@SuppressWarnings({"java:S5960", "java:S6813"})
class NotifyClaimWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private Time time;

    @MockBean
    private WorkingDayIndicator workingDayIndicator;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private IStateFlowEngine stateFlowEngine;

    @BeforeEach
    void setUpNotifyClaimLifecycle() {
        when(time.now()).thenReturn(CALLBACK_TIME);
        when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);
    }

    @Test
    void shouldPrepareOptionsForARepresentedTwoDefendantClaim() throws Exception {
        startWorkflow(NotifyClaimFixtures.issuedTwoSolicitorClaim(null))
            .eventId(NOTIFY_DEFENDANT_OF_CLAIM)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData())
                    .containsKey("defendantSolicitorNotifyClaimOptions");

                List<String> options = result.caseData().getDefendantSolicitorNotifyClaimOptions()
                    .getListItems()
                    .stream()
                    .map(DynamicListElement::getLabel)
                    .toList();

                assertThat(options)
                    .hasSize(3)
                    .first()
                    .isEqualTo("Both");
                assertThat(options).anyMatch(option -> option.startsWith("Defendant One: "));
                assertThat(options).anyMatch(option -> option.startsWith("Defendant Two: "));
            });
    }

    @Test
    void shouldNotifyRepresentedDefendantInAOneVOneClaim() throws Exception {
        LocalDateTime expectedDeadline = CALLBACK_TIME.plusDays(14)
            .toLocalDate()
            .atTime(END_OF_BUSINESS_DAY);

        startWorkflow(NotifyClaimFixtures.issuedRepresentedOneVOneClaim())
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(caseData.getDefendantSolicitorNotifyClaimOptions()).isNull();
                assertThat(caseData.getClaimNotificationDate()).isEqualTo(CALLBACK_TIME);
                assertThat(caseData.getClaimDetailsNotificationDeadline()).isEqualTo(expectedDeadline);
                assertThat(caseData.getNextDeadline()).isEqualTo(expectedDeadline.toLocalDate());
                assertThat(caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo(RESPONDENT_ONE_ORGANISATION);
                assertState(caseData, CLAIM_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Notification of claim sent", CLAIM_REFERENCE);
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains(
                        "The defendant legal representative's organisation has been notified and granted access to this claim."
                    )
                    .doesNotContain("proceed offline");
            });
    }

    @Test
    void shouldNotifyBothRepresentedDefendantsAndRestoreTheirOrganisationPolicies() throws Exception {
        LocalDateTime expectedDeadline = CALLBACK_TIME.plusDays(14)
            .toLocalDate()
            .atTime(END_OF_BUSINESS_DAY);

        startWorkflow(NotifyClaimFixtures.issuedTwoSolicitorClaim("Both"))
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData()).containsKeys(
                    "businessProcess",
                    "claimNotificationDate",
                    "claimDetailsNotificationDeadline",
                    "nextDeadline",
                    "respondent1OrganisationPolicy",
                    "respondent2OrganisationPolicy"
                );
                assertThat(caseData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY, NOTIFY_DEFENDANT_OF_CLAIM.name());
                assertThat(caseData.getClaimNotificationDate()).isEqualTo(CALLBACK_TIME);
                assertThat(caseData.getClaimDetailsNotificationDeadline()).isEqualTo(expectedDeadline);
                assertThat(caseData.getNextDeadline()).isEqualTo(expectedDeadline.toLocalDate());
                assertThat(caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo(RESPONDENT_ONE_ORGANISATION);
                assertThat(caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo(RESPONDENT_TWO_ORGANISATION);
                assertState(caseData, CLAIM_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Notification of claim sent", CLAIM_REFERENCE);
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains("defendant legal representative's organisation has been notified")
                    .contains("You must notify the defendant with the claim details");
            });
    }

    @Test
    void shouldWarnAndProgressOfflineWhenOnlyOneRepresentedDefendantIsNotified() throws Exception {
        startWorkflow(NotifyClaimFixtures.issuedTwoSolicitorClaim(DEFENDANT_ONE_OPTION))
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM)
            .mid("validateNotificationOption")
            .then(result -> assertThat(result.response().getWarnings())
                .containsExactly(WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR))
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getClaimNotificationDate()).isEqualTo(CALLBACK_TIME);
                assertState(result.caseData(), TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse().path("confirmation_body").asText())
                .contains("1 Defendant legal representative only")
                .contains("proceed offline"));
    }

    @Test
    void shouldUseCertificateOfServiceDateWhenARespondentIsLip() throws Exception {
        LocalDate serviceDate = CALLBACK_TIME.minusDays(3).toLocalDate();
        CertificateOfService certificate = ClaimLifecycleFixtures.certificateOfService(
            "notify-claim.pdf",
            serviceDate,
            serviceDate.plusDays(1)
        );
        LocalDateTime expectedDeadline = serviceDate.plusDays(14).atTime(END_OF_BUSINESS_DAY);

        startWorkflow(NotifyClaimFixtures.issuedMixedRepresentationClaim(certificate))
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData()).containsKeys(
                    "claimNotificationDate",
                    "claimDetailsNotificationDeadline",
                    "cosNotifyClaimDefendant2"
                );
                assertThat(caseData.getClaimDetailsNotificationDeadline()).isEqualTo(expectedDeadline);
                assertThat(caseData.getCosNotifyClaimDefendant2().getCosSenderStatementOfTruthLabel())
                    .containsExactly("CERTIFIED");
                assertThat(caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo(RESPONDENT_ONE_ORGANISATION);
                assertThat(caseData.getDefendant2LIPAtClaimIssued()).isEqualTo(YES);
                assertState(caseData, CLAIM_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Certificate of Service", CLAIM_REFERENCE);
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains(
                        "You must serve the claim details and",
                        "complete the certificate of service notify claim details next step"
                    );
            });
    }

    @Test
    void shouldUseDefendantOneCertificateOfServiceForAOneVOneLipClaim() throws Exception {
        LocalDate serviceDate = CALLBACK_TIME.minusDays(4).toLocalDate();
        CertificateOfService certificate = ClaimLifecycleFixtures.certificateOfService(
            "respondent-one-notify-claim.pdf",
            serviceDate,
            serviceDate.plusDays(1)
        );
        LocalDateTime expectedDeadline = serviceDate.plusDays(14).atTime(END_OF_BUSINESS_DAY);

        startWorkflow(NotifyClaimFixtures.issuedOneVOneLipClaim(certificate))
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(caseData.getClaimDetailsNotificationDeadline()).isEqualTo(expectedDeadline);
                assertThat(caseData.getCosNotifyClaimDefendant1().getCosSenderStatementOfTruthLabel())
                    .containsExactly("CERTIFIED");
                assertThat(caseData.getRespondent1OrganisationIDCopy()).isNull();
                assertThat(caseData.getDefendant1LIPAtClaimIssued()).isEqualTo(YES);
                assertState(caseData, CLAIM_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Certificate of Service", CLAIM_REFERENCE);
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains(
                        "You must serve the claim details and",
                        "complete the certificate of service notify claim details next step"
                    );
            });
    }

    @Test
    void shouldUseEarliestServiceDateAndCertifyBothDefendantsForATwoLipClaim() throws Exception {
        LocalDate respondentOneServiceDate = CALLBACK_TIME.minusDays(2).toLocalDate();
        LocalDate respondentTwoServiceDate = CALLBACK_TIME.minusDays(5).toLocalDate();
        CertificateOfService respondentOneCertificate = ClaimLifecycleFixtures.certificateOfService(
            "respondent-one-notify-claim.pdf",
            respondentOneServiceDate,
            respondentOneServiceDate.plusDays(1)
        );
        CertificateOfService respondentTwoCertificate = ClaimLifecycleFixtures.certificateOfService(
            "respondent-two-notify-claim.pdf",
            respondentTwoServiceDate,
            respondentTwoServiceDate.plusDays(1)
        );
        LocalDateTime expectedDeadline = respondentTwoServiceDate.plusDays(14).atTime(END_OF_BUSINESS_DAY);

        startWorkflow(NotifyClaimFixtures.issuedTwoLipClaim(
            respondentOneCertificate,
            respondentTwoCertificate
        ))
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(caseData.getClaimDetailsNotificationDeadline()).isEqualTo(expectedDeadline);
                assertThat(caseData.getCosNotifyClaimDefendant1().getCosSenderStatementOfTruthLabel())
                    .containsExactly("CERTIFIED");
                assertThat(caseData.getCosNotifyClaimDefendant2().getCosSenderStatementOfTruthLabel())
                    .containsExactly("CERTIFIED");
                assertState(caseData, CLAIM_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Certificate of Service", CLAIM_REFERENCE);
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains(
                        "You must serve the claim details and",
                        "complete the certificate of service notify claim details next step"
                    );
            });
    }

    private void assertState(CaseData caseData, String expectedState) {
        StateFlowDTO stateFlow = stateFlowEngine.getStateFlow(caseData);
        assertThat(stateFlow.getState().getName())
            .as("state history: %s", stateFlow.getStateHistory())
            .isEqualTo(expectedState);
    }
}
