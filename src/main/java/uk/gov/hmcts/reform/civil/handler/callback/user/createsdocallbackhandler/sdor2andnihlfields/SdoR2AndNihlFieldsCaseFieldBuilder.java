package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.sdor2andnihlfields;

import uk.gov.hmcts.reform.civil.model.CaseData;

public interface SdoR2AndNihlFieldsCaseFieldBuilder {

    void build(CaseData.CaseDataBuilder<?, ?> updatedData);
}
