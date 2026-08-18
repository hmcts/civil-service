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
            .defendantDetails(DynamicList.builder()
                                  .value(DynamicListElement.builder().label("Mr Defendant").build())
                                  .listItems(List.of(DynamicListElement.builder().label("Mr Defendant").build()))
                                  .build())
            .hearingSupportRequirementsDJ(new HearingSupportRequirementsDJ()
                                              .setHearingTemporaryLocation(DynamicList.builder()
                                                                               .value(locationElement)
                                                                               .listItems(List.of(locationElement))
                                                                               .build()))
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
            .respondent2(PartyBuilder.builder().individual().build())
            .defendantDetails(DynamicList.builder()
                                  .value(DynamicListElement.builder().label("Mr Defendant").build())
                                  .listItems(List.of(
                                      DynamicListElement.builder().label("Mr Defendant").build(),
                                      DynamicListElement.builder().label("Mrs Defendant Two").build(),
                                      DynamicListElement.builder().label("Both Defendants").build()
                                  ))
                                  .build())
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
            .defendantDetails(DynamicList.builder()
                                  .value(DynamicListElement.builder().label("Mr Defendant").build())
                                  .listItems(List.of(DynamicListElement.builder().label("Mr Defendant").build()))
                                  .build())
            .build();
    }
}
