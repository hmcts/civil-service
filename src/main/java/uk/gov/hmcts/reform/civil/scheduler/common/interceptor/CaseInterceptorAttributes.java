package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BaseCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

public final class CaseInterceptorAttributes {

    private CaseInterceptorAttributes() {
        // Utility class
    }

    public static final String CASE_DATA_NAME = "CaseData";

    public static final AttributeKey<BaseCaseData> BASE_CASE_DATA = AttributeKey.of(CASE_DATA_NAME, BaseCaseData.class);
    public static final AttributeKey<CaseData> CIVIL_CASE_DATA = AttributeKey.of(CASE_DATA_NAME, CaseData.class);
    public static final AttributeKey<GeneralApplicationCaseData> GA_CASE_DATA = AttributeKey.of(CASE_DATA_NAME, GeneralApplicationCaseData.class);
}
