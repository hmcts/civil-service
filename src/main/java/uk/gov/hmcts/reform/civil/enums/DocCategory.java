package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocCategory {
    BUNDLES("bundles"),
    DEF1_DEFENSE_DQ("defendant1DefenseDirectionsQuestionnaire"),
    DEF2_DEFENSE_DQ("defendant2DefenseDirectionsQuestionnaire"),
    APP1_DQ("directionsQuestionnaire"),
    DQ_APP1("DQApplicant"),
    DQ_DEF1("DQRespondent"),
    DQ_DEF2("DQRespondentTwo"),
    HEARING_NOTICES("hearingNotices");

    private final String value;
}
