package uk.gov.hmcts.reform.civil.service.docmosis;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.io.IOException;

public interface TemplateDataGenerator<T> {

    T getTemplateData(CaseData caseData) throws IOException;
}
