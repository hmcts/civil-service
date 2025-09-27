package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.submitsdo;

import uk.gov.hmcts.reform.civil.model.CaseData;

public interface SdoCaseDataFieldUpdater {

    void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder);
}
