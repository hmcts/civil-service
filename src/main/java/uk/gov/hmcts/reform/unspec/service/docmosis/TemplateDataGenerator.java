package uk.gov.hmcts.reform.unspec.service.docmosis;

import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.io.IOException;

public abstract class TemplateDataGenerator<T> {

    public abstract T getTemplateData(CaseData caseData) throws IOException;
}
