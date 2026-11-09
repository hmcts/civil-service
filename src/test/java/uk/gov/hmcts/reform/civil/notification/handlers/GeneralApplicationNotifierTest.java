package uk.gov.hmcts.reform.civil.notification.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The framework used to be typed to {@code CaseData}, so a General Application notifier could not
 * exist at all. These tests are the proof that it now can: they would not compile before
 * DTSCCI-6726, which is the point of the change rather than an incidental assertion.
 *
 * <p>They also pin the two things {@link Notifier} actually needs from a case, so a future change
 * that widens that dependency has to come past a failing test here.</p>
 */
class GeneralApplicationNotifierTest {

    private static final Long CASE_ID = 1234567890123456L;
    private static final String TASK_ID = "GaTaskId";
    private static final String EVENT_ID = "NOTIFY_EVENT";

    private NotificationService notificationService;
    private CaseTaskTrackingService caseTaskTrackingService;
    private PartiesEmailGenerator<GeneralApplicationCaseData> emailGenerator;
    private GeneralApplicationCaseData caseData;
    private TestGaNotifier notifier;

    /** A notifier parameterised on GA case data rather than CaseData. */
    private static class TestGaNotifier extends Notifier<GeneralApplicationCaseData> {
        TestGaNotifier(NotificationService notificationService,
                       CaseTaskTrackingService caseTaskTrackingService,
                       PartiesEmailGenerator<GeneralApplicationCaseData> emailGenerator) {
            super(notificationService, caseTaskTrackingService, emailGenerator);
        }

        @Override
        protected String getTaskId() {
            return TASK_ID;
        }
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        caseTaskTrackingService = mock(CaseTaskTrackingService.class);
        emailGenerator = mock(PartiesEmailGenerator.class);
        notifier = new TestGaNotifier(notificationService, caseTaskTrackingService, emailGenerator);

        caseData = mock(GeneralApplicationCaseData.class);
        when(caseData.getCcdCaseReference()).thenReturn(CASE_ID);
    }

    private EmailDTO party(String email, String reference, String template) {
        EmailDTO dto = new EmailDTO();
        dto.setTargetEmail(email);
        dto.setReference(reference);
        dto.setEmailTemplate(template);
        dto.setParameters(Map.of("claimReferenceNumber", String.valueOf(CASE_ID)));
        return dto;
    }

    @Test
    void shouldNotifyGeneralApplicationPartiesAndReportThemInTheSummary() {
        EmailDTO applicant = party("ga-applicant@example.com", "ga-ref-1", "template-a");
        EmailDTO respondent = party("ga-respondent@example.com", "ga-ref-2", "template-b");
        when(emailGenerator.getPartiesToNotify(caseData, TASK_ID)).thenReturn(Set.of(applicant, respondent));

        String summary = notifier.notifyParties(caseData, EVENT_ID, TASK_ID);

        verify(notificationService).sendMail(
            eq("ga-applicant@example.com"), eq("template-a"), anyMap(), eq("ga-ref-1"));
        verify(notificationService).sendMail(
            eq("ga-respondent@example.com"), eq("template-b"), anyMap(), eq("ga-ref-2"));

        // the summary feeds the GA history tab in DTSCCI-4296, so its shape is asserted not assumed
        assertThat(summary).startsWith("Attempted: ");
        assertThat(summary).contains("ga-applicant@example.com : ga-ref-1 : template-a");
        assertThat(summary).contains("ga-respondent@example.com : ga-ref-2 : template-b");
        assertThat(summary).endsWith("|| Errors: ");
    }

    @Test
    void shouldNotTrackErrorsWhenEveryGeneralApplicationEmailSucceeds() {
        when(emailGenerator.getPartiesToNotify(caseData, TASK_ID))
            .thenReturn(Set.of(party("ga-applicant@example.com", "ga-ref-1", "template-a")));

        notifier.notifyParties(caseData, EVENT_ID, TASK_ID);

        verifyNoInteractions(caseTaskTrackingService);
    }

    @Test
    void shouldReadTheCaseReferenceFromGeneralApplicationCaseData() {
        when(emailGenerator.getPartiesToNotify(caseData, TASK_ID)).thenReturn(Set.of());

        notifier.notifyParties(caseData, EVENT_ID, TASK_ID);

        // the only thing Notifier needs from the case, and the reason NotifiableCaseData exists
        verify(caseData).getCcdCaseReference();
    }
}
