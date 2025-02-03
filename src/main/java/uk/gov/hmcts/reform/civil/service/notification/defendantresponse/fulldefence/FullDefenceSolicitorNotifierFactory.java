package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;

@Component
@RequiredArgsConstructor
public class FullDefenceSolicitorNotifierFactory {

    private final FullDefenceApplicantSolicitorOneCCSpecNotifier fullDefenceApplicantSolicitorOneCCSpecNotifier;
    private final FullDefenceApplicantSolicitorOneCCUnspecNotifier fullDefenceApplicantSolicitorOneCCUnspecNotifier;
    private final FullDefenceApplicantSolicitorOneSpecNotifier fullDefenceApplicantSolicitorOneSpecNotifier;
    private final FullDefenceApplicantSolicitorOneUnspecNotifier fullDefenceApplicantSolicitorOneUnspecNotifier;
    private final FullDefenceRespondentSolicitorOneCCSpecNotifier fullDefenceRespondentSolicitorOneCCSpecNotifier;
    private final FullDefenceRespondentSolicitorOneCCUnspecNotifier fullDefenceRespondentSolicitorOneCCUnspecNotifier;
    private final FullDefenceRespondentSolicitorTwoCCSpecNotifier fullDefenceRespondentSolicitorTwoCCSpecNotifier;
    private final FullDefenceRespondentSolicitorTwoCCUnspecNotifier fullDefenceRespondentSolicitorTwoUnspecNotifier;

    public FullDefenceSolicitorNotifier getNotifier(FullDefenceNotificationType notificationType, CaseCategory caseCategory) {
        switch (notificationType) {
            case APPLICANT_SOLICITOR_ONE:
                return CaseCategory.SPEC_CLAIM.equals(caseCategory) ? fullDefenceApplicantSolicitorOneSpecNotifier :
                    fullDefenceApplicantSolicitorOneUnspecNotifier;
            case APPLICANT_SOLICITOR_ONE_CC:
                return CaseCategory.SPEC_CLAIM.equals(caseCategory) ? fullDefenceApplicantSolicitorOneCCSpecNotifier :
                    fullDefenceApplicantSolicitorOneCCUnspecNotifier;
            case RESPONDENT_SOLICITOR_ONE_CC:
                return CaseCategory.SPEC_CLAIM.equals(caseCategory) ? fullDefenceRespondentSolicitorOneCCSpecNotifier :
                    fullDefenceRespondentSolicitorOneCCUnspecNotifier;
            case RESPONDENT_SOLICITOR_TWO_CC:
                return CaseCategory.SPEC_CLAIM.equals(caseCategory) ? fullDefenceRespondentSolicitorTwoCCSpecNotifier :
                    fullDefenceRespondentSolicitorTwoUnspecNotifier;
            default:
                throw new IllegalArgumentException("Invalid notification type: " + notificationType);
        }
    }
}
