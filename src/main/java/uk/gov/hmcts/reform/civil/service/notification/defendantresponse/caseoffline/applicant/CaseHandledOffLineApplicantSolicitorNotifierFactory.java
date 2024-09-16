package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Component
@RequiredArgsConstructor
public class CaseHandledOffLineApplicantSolicitorNotifierFactory {

    private final CaseHandledOfflineApplicantSolicitorUnspecNotifier caseHandledOfflineApplicantSolicitorUnspecNotifier;
    private final CaseHandledOfflineApplicantSolicitorSpecNotifier caseHandledOfflineApplicantSolicitorSpecNotifier;

    public CaseHandledOfflineApplicantSolicitorNotifier getCaseHandledOfflineSolicitorNotifier(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return caseHandledOfflineApplicantSolicitorSpecNotifier;
        } else {
            return caseHandledOfflineApplicantSolicitorUnspecNotifier;
        }
    }
}
