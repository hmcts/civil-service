package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder;

import uk.gov.hmcts.reform.civil.model.CaseData;

public interface GenerateSdoOrderCaseDataMapper {

    void mapHearingMethodFields(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData);
}
