package uk.gov.hmcts.reform.civil.ga.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GAJudgeRequestMoreInfoOption {

    REQUEST_MORE_INFORMATION("Request more information"),
    SEND_APP_TO_OTHER_PARTY("Send application to other party and request hearing details");

    private final String displayedValue;
}
