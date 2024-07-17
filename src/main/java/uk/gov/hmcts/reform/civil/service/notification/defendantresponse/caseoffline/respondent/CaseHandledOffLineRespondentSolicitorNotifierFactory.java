package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.respondent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Component
@RequiredArgsConstructor
public class CaseHandledOffLineRespondentSolicitorNotifierFactory {

    private final CaseHandledOfflineRespondentSolicitorUnspecNotifier caseHandledOfflineRespondentSolicitorUnspecNotifier;
    private final CaseHandledOfflineRespondentSolicitorSpecNotifier caseHandledOfflineRespondentSolicitorSpecNotifier;

    public CaseHandledOfflineRespondentSolicitorNotifier getCaseHandledOfflineSolicitorNotifier(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return caseHandledOfflineRespondentSolicitorSpecNotifier;
        } else {
            return caseHandledOfflineRespondentSolicitorUnspecNotifier;
        }
    }
}
