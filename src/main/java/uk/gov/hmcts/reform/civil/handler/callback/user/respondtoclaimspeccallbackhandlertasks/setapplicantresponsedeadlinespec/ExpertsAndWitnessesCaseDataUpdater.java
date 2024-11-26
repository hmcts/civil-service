package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import uk.gov.hmcts.reform.civil.model.CaseData;

public interface ExpertsAndWitnessesCaseDataUpdater {

    void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData);

}
