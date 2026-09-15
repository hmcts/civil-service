package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

public final class DefaultJudgementFixtures {

    private DefaultJudgementFixtures() {
    }

    public static CaseData unspecDj1v1() {
        DynamicListElement locationElement = DynamicListElement.dynamicElementFromCode(
            "uuid-123-456789", "Court Name - Address - Postcode");
        return CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .ccdState(CaseState.CASE_ISSUED)
            .caseAccessCategory(UNSPEC_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .addRespondent2(YesOrNo.NO)
            .defendantDetails(defendantDetails("Mr Defendant"))
            .hearingSupportRequirementsDJ(new HearingSupportRequirementsDJ()
                                              .setHearingTemporaryLocation(new DynamicList()
                                                                               .setValue(locationElement)
                                                                               .setListItems(List.of(locationElement))))
            .build();
    }

    public static CaseData unspecDj1v2SingleDefendant() {
        return CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .ccdState(CaseState.CASE_ISSUED)
            .caseAccessCategory(UNSPEC_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .addRespondent2(YesOrNo.YES)
            .respondent2(new PartyBuilder().individual().build())
            .defendantDetails(defendantDetails("Mr Defendant", "Mrs Defendant Two", "Both Defendants"))
            .build();
    }

    public static CaseData unspecDjDeadlineNotPassed() {
        return CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build()
            .toBuilder()
            .ccdCaseReference(1234567890123456L)
            .ccdState(CaseState.CASE_ISSUED)
            .caseAccessCategory(UNSPEC_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().plusDays(5))
            .addRespondent2(YesOrNo.NO)
            .defendantDetails(defendantDetails("Mr Defendant"))
            .build();
    }

    private static DynamicList defendantDetails(String selected, String... extraLabels) {
        List<String> labels = extraLabels.length == 0
            ? List.of(selected)
            : Stream.concat(Stream.of(selected), Stream.of(extraLabels)).toList();
        return DynamicList.fromList(labels, Function.identity(), selected, false);
    }
}
