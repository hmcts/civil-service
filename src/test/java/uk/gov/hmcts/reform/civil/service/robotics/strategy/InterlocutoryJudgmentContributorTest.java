package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class InterlocutoryJudgmentContributorTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private RoboticsTimelineHelper timelineHelper;

    private final RoboticsPartyLookup partyLookup = new RoboticsPartyLookup();

    private InterlocutoryJudgmentContributor contributor;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contributor = new InterlocutoryJudgmentContributor(sequenceGenerator, timelineHelper, partyLookup);

        now = LocalDateTime.of(2024, 6, 1, 12, 0);
        when(timelineHelper.now()).thenReturn(now);
    }

    @Test
    void supportsReturnsFalseWhenHearingSupportMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenSummaryJudgmentAlreadyGrantedForSingleRespondent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build()
            .toBuilder()
            .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YesOrNo.YES)
            .defendantDetails(DynamicList.builder()
                .value(DynamicListElement.builder().label("Defendant 1").build())
                .build())
            .build();

        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsSingleEventWhenOnlyOneRespondent() {
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);

        CaseData caseData = CaseData.builder()
            .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();

        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getInterlocutoryJudgment()).hasSize(1);
        assertThat(history.getInterlocutoryJudgment().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getInterlocutoryJudgment().get(0).getEventCode())
            .isEqualTo(EventType.INTERLOCUTORY_JUDGMENT_GRANTED.getCode());
        assertThat(history.getInterlocutoryJudgment().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(history.getInterlocutoryJudgment().get(0).getDateReceived()).isEqualTo(now);
    }

    @Test
    void contributeAddsEventsForBothRespondentsWhenApplicable() {
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(21, 22);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build()
            .toBuilder()
            .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
            .respondent2(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Alex")
                .individualLastName("Jones")
                .build())
            .addRespondent2(YesOrNo.YES)
            .defendantDetails(DynamicList.builder()
                .value(DynamicListElement.builder().label("Both defendants").build())
                .build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();

        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getInterlocutoryJudgment()).hasSize(2);
        assertThat(history.getInterlocutoryJudgment().get(0).getEventSequence()).isEqualTo(21);
        assertThat(history.getInterlocutoryJudgment().get(0).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT_ID);
        assertThat(history.getInterlocutoryJudgment().get(1).getEventSequence()).isEqualTo(22);
        assertThat(history.getInterlocutoryJudgment().get(1).getLitigiousPartyID())
            .isEqualTo(RoboticsDataUtil.RESPONDENT2_ID);
        assertThat(history.getInterlocutoryJudgment().get(1).getDateReceived()).isEqualTo(now);
    }

    @Test
    void contributeSkipsWhenSupportConditionFails() {
        CaseData caseData = CaseDataBuilder.builder().build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        assertThat(builder.build().getInterlocutoryJudgment()).isNullOrEmpty();
        verifyNoMoreInteractions(sequenceGenerator);
    }
}
