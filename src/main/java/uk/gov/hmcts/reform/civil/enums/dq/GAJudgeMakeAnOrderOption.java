package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GAJudgeMakeAnOrderOption {

    APPROVE_OR_EDIT("Approve or edit the order requested by the applicant"),
    DISMISS_THE_APPLICATION("Dismiss the application"),
    GIVE_DIRECTIONS_WITHOUT_HEARING("Give directions without listing for hearing");

    private final String displayedValue;
}
