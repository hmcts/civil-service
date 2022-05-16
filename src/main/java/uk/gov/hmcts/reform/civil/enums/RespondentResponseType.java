package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RespondentResponseType {
    FULL_DEFENCE("rejects all of the claim"),
    FULL_ADMISSION("admits all of the claim"),
    PART_ADMISSION("admits part of the claim"),
    COUNTER_CLAIM("rejects all of the claim and wants to counterclaim"),
    STATES_PAID("admits and pay"),
    BREATHING_SPACE_ENTERED("standard breathing space entered"),
    BREATHING_SPACE_LIFTED("standard breathing space lifted"),
    MENTAL_HEALTH_BREATHING_SPACE_ENTERED("mental health breathing space entered"),
    MENTAL_HEALTH_BREATHING_SPACE_LIFTED("mental health breathing space lifted"),
    INTENTION_TO_PROCEED("intent to proceed"),
    INTENTION_TO_PROCEED_STATES_PAID("intent to proceed after states paid response");

    private final String displayedValue;
}
