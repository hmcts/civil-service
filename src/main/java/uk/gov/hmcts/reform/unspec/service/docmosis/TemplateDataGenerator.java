package uk.gov.hmcts.reform.unspec.service.docmosis;

import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.io.IOException;

public interface TemplateDataGenerator<T> {

    T getTemplateData(CaseData caseData) throws IOException;
}
