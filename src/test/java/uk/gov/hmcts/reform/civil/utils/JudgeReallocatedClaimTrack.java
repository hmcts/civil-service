package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.JudgeReallocatedClaimTrack.hasJudgeReallocatedTrack;

class JudgeReallocatedClaimTrackTest {

    @Test
    void hasJudgeReallocatedTrackTest_shouldBeFalseWhenNot() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        assertThat(hasJudgeReallocatedTrack(caseData)).isFalse();
    }

    @Test
    void hasJudgeReallocatedTrackTest_shouldBeTrueWhenJudgeReallocatedSpec() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .finalOrderAllocateToTrack(YesOrNo.YES)
            .build();

        assertThat(hasJudgeReallocatedTrack(caseData)).isTrue();
    }

    @Test
    void hasJudgeReallocatedTrackTest_shouldBeTrueWhenJudgeReallocatedUnSpec() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .finalOrderAllocateToTrack(YesOrNo.YES)
            .build();

        assertThat(hasJudgeReallocatedTrack(caseData)).isTrue();
    }
}
