package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import uk.gov.hmcts.reform.civil.model.CaseData;

public interface HandleAdmitPartOfClaimCaseUpdater {

    void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData);
}
