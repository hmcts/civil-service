package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.disposalhearing;

import uk.gov.hmcts.reform.civil.model.CaseData;

public interface DisposalHearingCaseFieldBuilder {

    void build(CaseData.CaseDataBuilder<?, ?> updatedData);
}
