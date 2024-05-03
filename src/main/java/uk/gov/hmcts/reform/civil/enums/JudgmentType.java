package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JudgmentType {
    DEFAULT_JUDGMENT,
    JUDGMENT_BY_ADMISSION,
    JUDGMENT_FOLLOWING_HEARING,
    INTERLOCUTORY_JUDGMENT;
}
