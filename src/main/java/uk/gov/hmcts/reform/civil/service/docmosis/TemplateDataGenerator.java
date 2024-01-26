package uk.gov.hmcts.reform.civil.service.docmosis;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.io.IOException;

public interface TemplateDataGenerator<T> {

    default T getTemplateData(CaseData caseData) throws IOException {
        return null;
    }

    default T getTemplateData(CaseData caseData, CaseEvent caseEvent) throws IOException {
        return null;
    }
}
