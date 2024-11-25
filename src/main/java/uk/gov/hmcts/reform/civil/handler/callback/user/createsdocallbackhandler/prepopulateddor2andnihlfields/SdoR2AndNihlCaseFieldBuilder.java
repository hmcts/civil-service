package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import uk.gov.hmcts.reform.civil.model.CaseData;

public interface SdoR2AndNihlCaseFieldBuilder {

    void build(CaseData.CaseDataBuilder<?, ?> updatedData);
}
