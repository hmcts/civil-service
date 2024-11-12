package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.orderdetailspages;

import uk.gov.hmcts.reform.civil.model.CaseData;

public interface OrderDetailsPagesCaseFieldBuilder {

    void build(CaseData.CaseDataBuilder<?, ?> updatedData);
}
