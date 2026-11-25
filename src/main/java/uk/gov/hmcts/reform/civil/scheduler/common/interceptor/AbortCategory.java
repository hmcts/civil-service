package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

public enum AbortCategory {

    ONGOING_BUSINESS_PROCESS,
    FLOW_STATE_DISALLOWED,
    BUSINESS_RULE_NOT_MET,
    SILENT_ABORTION,
    OTHER
}
