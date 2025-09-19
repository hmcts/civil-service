package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.generatesdoorder;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

public interface GenerateSdoOrderValidator {

    void validate(CaseData caseData, List<String> errors);

}
