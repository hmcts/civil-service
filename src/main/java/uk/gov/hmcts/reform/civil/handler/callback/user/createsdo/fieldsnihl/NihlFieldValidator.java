package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.fieldsnihl;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

public interface NihlFieldValidator {

    void validate(CaseData caseData, List<String> errors);

}
