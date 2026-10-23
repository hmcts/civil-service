package uk.gov.hmcts.reform.civil.notification.handlers.gahwfapplicant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.HwfNotificationService;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.Set;

/**
 * Help with Fees outcome notifies the applicant solicitor only, so this returns a single recipient.
 *
 * <p>The choice of recipient, template and properties still comes from
 * {@link HwfNotificationService#buildEmail}, which is the same code the retired handler used. That
 * is deliberate: the migration moves who sends the email, not which email is sent.</p>
 */
@Component
@RequiredArgsConstructor
public class GaHwfApplicantEmailGenerator implements PartiesEmailGenerator<GeneralApplicationCaseData> {

    private final HwfNotificationService hwfNotificationService;

    @Override
    public Set<EmailDTO> getPartiesToNotify(GeneralApplicationCaseData caseData, String taskId) {
        return Set.of(hwfNotificationService.buildEmail(caseData, null));
    }
}
