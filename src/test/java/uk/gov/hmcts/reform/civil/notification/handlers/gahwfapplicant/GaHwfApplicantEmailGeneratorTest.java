package uk.gov.hmcts.reform.civil.notification.handlers.gahwfapplicant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.HwfNotificationService;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GaHwfApplicantEmailGeneratorTest {

    private HwfNotificationService hwfNotificationService;
    private GaHwfApplicantEmailGenerator generator;

    @BeforeEach
    void setUp() {
        hwfNotificationService = mock(HwfNotificationService.class);
        generator = new GaHwfApplicantEmailGenerator(hwfNotificationService);
    }

    @Test
    void shouldReturnTheSingleApplicantEmailBuiltByTheSharedService() {
        GeneralApplicationCaseData caseData = mock(GeneralApplicationCaseData.class);
        EmailDTO applicant = new EmailDTO();
        applicant.setTargetEmail("applicant-solicitor@example.com");
        when(hwfNotificationService.buildEmail(caseData, null)).thenReturn(applicant);

        Set<EmailDTO> parties = generator.getPartiesToNotify(caseData, GaHwfApplicantNotifier.TASK_ID);

        // Help with Fees notifies the applicant only, so a second recipient here would be a defect
        assertThat(parties).containsExactly(applicant);
    }

    @Test
    void shouldLetTheServiceDeriveTheEventFromCaseData() {
        GeneralApplicationCaseData caseData = mock(GeneralApplicationCaseData.class);
        when(hwfNotificationService.buildEmail(caseData, null)).thenReturn(new EmailDTO());

        generator.getPartiesToNotify(caseData, GaHwfApplicantNotifier.TASK_ID);

        // the bespoke NOTIFY_APPLICANT_LIP_HWF event is gone from the framework path, so the event
        // has to come from the case. Passing one here would reintroduce the per event coupling.
        verify(hwfNotificationService).buildEmail(eq(caseData), isNull());
    }
}
