package uk.gov.hmcts.reform.civil.service.docmosis;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.io.IOException;

public interface TemplateDataGeneratorWithAuth<T> {

    T getTemplateData(CaseData caseData, String authorisation) throws IOException;
}
