package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler;

import uk.gov.hmcts.reform.civil.model.CaseData;

public interface SdoCaseFieldBuilder {

    void build(CaseData.CaseDataBuilder<?, ?> updatedData);
}
