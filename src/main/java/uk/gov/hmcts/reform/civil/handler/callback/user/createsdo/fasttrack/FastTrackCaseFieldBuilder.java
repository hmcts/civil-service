package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.fasttrack;

import uk.gov.hmcts.reform.civil.model.CaseData;

public interface FastTrackCaseFieldBuilder {

    void build(CaseData.CaseDataBuilder<?, ?> updatedData);
}
