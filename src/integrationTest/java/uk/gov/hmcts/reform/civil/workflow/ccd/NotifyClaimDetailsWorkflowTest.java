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
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.NotifyClaimDetailsFixtures;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.NotifyClaimDetailsCallbackHandler.WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_DAY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.CALLBACK_TIME;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.CLAIM_REFERENCE;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ClaimLifecycleFixtures.DEFENDANT_ONE_OPTION;

@SuppressWarnings({"java:S5960", "java:S6813"})
class NotifyClaimDetailsWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private Time time;

    @MockBean
    private WorkingDayIndicator workingDayIndicator;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private IStateFlowEngine stateFlowEngine;

    @BeforeEach
    void setUpNotifyClaimDetailsLifecycle() {
        when(time.now()).thenReturn(CALLBACK_TIME);
        when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);
    }

    @Test
    void shouldPrepareOptionsForARepresentedTwoDefendantClaim() throws Exception {
        startWorkflow(NotifyClaimDetailsFixtures.notifiedTwoSolicitorClaim(null))
            .eventId(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData())
                    .containsKey("defendantSolicitorNotifyClaimDetailsOptions");

                List<String> options = result.caseData().getDefendantSolicitorNotifyClaimDetailsOptions()
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
    void shouldNotifyClaimDetailsForARepresentedOneVOneClaim() throws Exception {
        LocalDateTime expectedResponseDeadline = CALLBACK_TIME.plusDays(14)
            .toLocalDate()
            .atTime(END_OF_BUSINESS_DAY);
        LocalDateTime expectedDismissalDeadline = CALLBACK_TIME.plusMonths(6)
            .toLocalDate()
            .atTime(END_OF_DAY);

        startWorkflow(NotifyClaimDetailsFixtures.notifiedRepresentedOneVOneClaim())
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(caseData.getDefendantSolicitorNotifyClaimDetailsOptions()).isNull();
                assertThat(caseData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY, NOTIFY_DEFENDANT_OF_CLAIM_DETAILS.name());
                assertThat(caseData.getClaimDetailsNotificationDate()).isEqualTo(CALLBACK_TIME);
                assertThat(caseData.getRespondent1ResponseDeadline()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getRespondent2ResponseDeadline()).isNull();
                assertThat(caseData.getAddLegalRepDeadlineRes1()).isNull();
                assertThat(caseData.getAddLegalRepDeadlineRes2()).isNull();
                assertThat(caseData.getClaimDismissedDeadline()).isEqualTo(expectedDismissalDeadline);
                assertThat(caseData.getNextDeadline()).isEqualTo(expectedResponseDeadline.toLocalDate());
                assertState(caseData, CLAIM_DETAILS_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Defendant notified", CLAIM_REFERENCE);
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains(
                        "The defendant legal representative's organisation has been notified of the claim details."
                    )
                    .contains("They must respond by")
                    .doesNotContain("proceed offline");
            });
    }

    @Test
    void shouldNotifyBothRepresentedDefendantsWithAlignedResponseDeadlines() throws Exception {
        LocalDateTime expectedResponseDeadline = CALLBACK_TIME.plusDays(14)
            .toLocalDate()
            .atTime(END_OF_BUSINESS_DAY);
        LocalDateTime expectedDismissalDeadline = CALLBACK_TIME.plusMonths(6)
            .toLocalDate()
            .atTime(END_OF_DAY);

        startWorkflow(NotifyClaimDetailsFixtures.notifiedTwoSolicitorClaim("Both"))
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData()).containsKeys(
                    "businessProcess",
                    "claimDetailsNotificationDate",
                    "claimDismissedDeadline",
                    "respondent1ResponseDeadline",
                    "respondent2ResponseDeadline",
                    "addLegalRepDeadlineRes1",
                    "addLegalRepDeadlineRes2",
                    "nextDeadline"
                );
                assertThat(caseData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY, NOTIFY_DEFENDANT_OF_CLAIM_DETAILS.name());
                assertThat(caseData.getClaimDetailsNotificationDate()).isEqualTo(CALLBACK_TIME);
                assertThat(caseData.getRespondent1ResponseDeadline()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getRespondent2ResponseDeadline()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getAddLegalRepDeadlineRes1()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getAddLegalRepDeadlineRes2()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getClaimDismissedDeadline()).isEqualTo(expectedDismissalDeadline);
                assertThat(caseData.getNextDeadline()).isEqualTo(expectedResponseDeadline.toLocalDate());
                assertState(caseData, CLAIM_DETAILS_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Defendant notified", CLAIM_REFERENCE);
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains("notified of the claim details")
                    .contains("They must respond by");
            });
    }

    @Test
    void shouldUseServiceDateFlowForRepresentedDefendantsWithTheSameSolicitor() throws Exception {
        LocalDateTime expectedResponseDeadline = CALLBACK_TIME.plusDays(14)
            .toLocalDate()
            .atTime(END_OF_BUSINESS_DAY);

        startWorkflow(NotifyClaimDetailsFixtures.notifiedSameSolicitorClaim())
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(caseData.getRespondent1ResponseDeadline()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getRespondent2ResponseDeadline()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getAddLegalRepDeadlineRes1()).isNull();
                assertThat(caseData.getAddLegalRepDeadlineRes2()).isNull();
                assertThat(caseData.getNextDeadline()).isEqualTo(expectedResponseDeadline.toLocalDate());
                assertState(caseData, CLAIM_DETAILS_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Defendant notified", CLAIM_REFERENCE);
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains("notified of the claim details")
                    .doesNotContain("proceed offline");
            });
    }

    @Test
    void shouldWarnAndProgressOfflineWhenOnlyOneRepresentedDefendantGetsClaimDetails() throws Exception {
        startWorkflow(NotifyClaimDetailsFixtures.notifiedTwoSolicitorClaim(DEFENDANT_ONE_OPTION))
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS)
            .mid("validateNotificationOption")
            .then(result -> assertThat(result.response().getWarnings())
                .containsExactly(WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR))
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getClaimDetailsNotificationDate()).isEqualTo(CALLBACK_TIME);
                assertState(result.caseData(), TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse().path("confirmation_body").asText())
                .contains("1 Defendant legal representative only")
                .contains("proceed offline"));
    }

    @Test
    void shouldUseLipServiceDateAndPersistCertificateEvidenceForAMixedClaim() throws Exception {
        LocalDate deemedServiceDate = CALLBACK_TIME.minusDays(2).toLocalDate();
        CertificateOfService certificate = ClaimLifecycleFixtures.certificateOfService(
            "respondent-two-certificate.pdf",
            deemedServiceDate.minusDays(1),
            deemedServiceDate
        );
        LocalDateTime expectedResponseDeadline = deemedServiceDate.plusDays(14)
            .atTime(END_OF_BUSINESS_DAY);
        LocalDateTime expectedDismissalDeadline = deemedServiceDate.plusMonths(6)
            .atTime(END_OF_DAY);

        startWorkflow(NotifyClaimDetailsFixtures.notifiedMixedRepresentationClaim(null, certificate))
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData()).containsKeys(
                    "claimDetailsNotificationDate",
                    "respondent1ResponseDeadline",
                    "respondent2ResponseDeadline",
                    "addLegalRepDeadlineRes2",
                    "servedDocumentFiles",
                    "cosNotifyClaimDetails2"
                );
                assertThat(caseData.getRespondent1ResponseDeadline()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getRespondent2ResponseDeadline()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getAddLegalRepDeadlineRes1()).isNull();
                assertThat(caseData.getAddLegalRepDeadlineRes2()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getClaimDismissedDeadline()).isEqualTo(expectedDismissalDeadline);
                assertThat(caseData.getCosNotifyClaimDetails2().getCosDocSaved()).isEqualTo(YES);
                assertThat(caseData.getCosNotifyClaimDetails2().getCosSenderStatementOfTruthLabel())
                    .containsExactly("CERTIFIED");
                assertThat(caseData.getServedDocumentFiles().getOther())
                    .singleElement()
                    .satisfies(document -> {
                        assertThat(document.getValue().getDocument().getDocumentFileName())
                            .isEqualTo("respondent-two-certificate.pdf");
                        assertThat(document.getValue().getDocument().getCategoryID())
                            .isEqualTo("particularsOfClaim");
                    });
                assertState(caseData, CLAIM_DETAILS_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Certificate of Service", CLAIM_REFERENCE);
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains("The defendant(s) must respond to the claim")
                    .doesNotContain("smartsurvey");
            });
    }

    @Test
    void shouldPersistCertificateAndDeadlinesForAOneVOneLipClaim() throws Exception {
        LocalDate deemedServiceDate = CALLBACK_TIME.minusDays(3).toLocalDate();
        CertificateOfService certificate = ClaimLifecycleFixtures.certificateOfService(
            "respondent-one-certificate.pdf",
            deemedServiceDate.minusDays(1),
            deemedServiceDate
        );
        LocalDateTime expectedResponseDeadline = deemedServiceDate.plusDays(14)
            .atTime(END_OF_BUSINESS_DAY);
        LocalDateTime expectedDismissalDeadline = deemedServiceDate.plusMonths(6)
            .atTime(END_OF_DAY);

        startWorkflow(NotifyClaimDetailsFixtures.notifiedOneVOneLipClaim(certificate))
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(caseData.getRespondent1ResponseDeadline()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getRespondent2ResponseDeadline()).isNull();
                assertThat(caseData.getAddLegalRepDeadlineRes1()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getAddLegalRepDeadlineRes2()).isNull();
                assertThat(caseData.getClaimDismissedDeadline()).isEqualTo(expectedDismissalDeadline);
                assertThat(caseData.getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(YES);
                assertThat(caseData.getCosNotifyClaimDetails1().getCosSenderStatementOfTruthLabel())
                    .containsExactly("CERTIFIED");
                assertThat(caseData.getServedDocumentFiles().getOther())
                    .singleElement()
                    .satisfies(document -> {
                        assertThat(document.getValue().getDocument().getDocumentFileName())
                            .isEqualTo("respondent-one-certificate.pdf");
                        assertThat(document.getValue().getDocument().getCategoryID())
                            .isEqualTo("particularsOfClaim");
                    });
                assertState(caseData, CLAIM_DETAILS_NOTIFIED.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .contains("Certificate of Service", CLAIM_REFERENCE);
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains("The defendant(s) must respond to the claim")
                    .doesNotContain("smartsurvey");
            });
    }

    @Test
    void shouldPersistBothCertificatesAndSetBothLipLegalRepresentativeDeadlines() throws Exception {
        LocalDate deemedServiceDate = CALLBACK_TIME.minusDays(2).toLocalDate();
        CertificateOfService respondentOneCertificate = ClaimLifecycleFixtures.certificateOfService(
            "respondent-one-certificate.pdf",
            deemedServiceDate.minusDays(1),
            deemedServiceDate
        );
        CertificateOfService respondentTwoCertificate = ClaimLifecycleFixtures.certificateOfService(
            "respondent-two-certificate.pdf",
            deemedServiceDate.minusDays(1),
            deemedServiceDate
        );
        LocalDateTime expectedResponseDeadline = deemedServiceDate.plusDays(14)
            .atTime(END_OF_BUSINESS_DAY);

        startWorkflow(NotifyClaimDetailsFixtures.notifiedTwoLipClaim(
            respondentOneCertificate,
            respondentTwoCertificate
        ))
            .eventId(CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS)
            .aboutToSubmit()
            .then(result -> {
                CaseData caseData = result.caseData();

                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(caseData.getCosNotifyClaimDetails1().getCosDocSaved()).isEqualTo(YES);
                assertThat(caseData.getCosNotifyClaimDetails2().getCosDocSaved()).isEqualTo(YES);
                assertThat(caseData.getServedDocumentFiles().getOther())
                    .extracting(document -> document.getValue().getDocument().getDocumentFileName())
                    .containsExactlyInAnyOrder(
                        "respondent-one-certificate.pdf",
                        "respondent-two-certificate.pdf"
                    );
                assertThat(caseData.getAddLegalRepDeadlineRes1()).isEqualTo(expectedResponseDeadline);
                assertThat(caseData.getAddLegalRepDeadlineRes2()).isEqualTo(expectedResponseDeadline);
                assertState(caseData, CLAIM_DETAILS_NOTIFIED.fullName());
            });
    }

    private void assertState(CaseData caseData, String expectedState) {
        StateFlowDTO stateFlow = stateFlowEngine.getStateFlow(caseData);
        assertThat(stateFlow.getState().getName())
            .as("state history: %s", stateFlow.getStateHistory())
            .isEqualTo(expectedState);
    }
}
