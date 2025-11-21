package uk.gov.hmcts.reform.civil.service.docmosis;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.io.IOException;

public interface TemplateDataGenerator<T> {

    default T getTemplateData(CaseData caseData) throws IOException {
        return null;
    }

    default T getTemplateData(CaseData caseData, CaseEvent caseEvent) throws IOException {
        return null;
    }

    default T getTemplateData(GeneralApplicationCaseData caseData) throws IOException {
        return null;
    }

    default T getTemplateData(GeneralApplicationCaseData caseData, String authorisation) throws IOException {
        return null;
    }

    default T getTemplateData(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData, String authorisation, FlowFlag userType) throws IOException {
        return null;
    }
}
