package uk.gov.hmcts.reform.civil.model.judgmentonline;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum JudgmentType {
    @CCD(label = "Default Judgment")
    DEFAULT_JUDGMENT,
    @CCD(label = "Judgment by admission")
    JUDGMENT_BY_ADMISSION,
    @CCD(label = "Judgment following hearing")
    JUDGMENT_FOLLOWING_HEARING,
    @CCD(label = "Interlocutory Judgment")
    INTERLOCUTORY_JUDGMENT
}
