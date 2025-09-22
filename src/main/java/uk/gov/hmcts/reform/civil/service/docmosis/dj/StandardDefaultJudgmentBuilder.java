package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;

public interface StandardDefaultJudgmentBuilder {

    DefaultJudgmentForm getDefaultJudgmentForm(CaseData caseData,
                                                      uk.gov.hmcts.reform.civil.model.Party respondent,
                                                      String event,
                                                      boolean addReferenceOfSecondRes);
}
