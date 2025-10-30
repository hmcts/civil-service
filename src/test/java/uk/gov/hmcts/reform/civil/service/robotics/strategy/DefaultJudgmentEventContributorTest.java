package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.support.CaseDataNormalizer;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFAULT_JUDGMENT_GRANTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;

class DefaultJudgmentEventContributorTest {

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private RoboticsTimelineHelper timelineHelper;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsPartyLookup partyLookup;

    private DefaultJudgmentEventContributor contributor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contributor = new DefaultJudgmentEventContributor(
            featureToggleService,
            timelineHelper,
            sequenceGenerator,
            partyLookup,
            new RoboticsEventTextFormatter()
        );
    }

    @Test
    void supportsReturnsFalseWhenDefendantDetailsMissing() {
        assertThat(contributor.supports(CaseData.builder().build())).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenDefendantDetailsPresent() {
        CaseData caseData = CaseDataBuilder.builder().getDefaultJudgment1v1Case();
        assertThat(contributor.supports(caseData)).isTrue();
    }

    @Test
    void contributesDefaultJudgmentEventsAndMisc() {
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        LocalDateTime now = baseDate.atTime(9, 0);
        when(timelineHelper.now()).thenReturn(now);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any())).thenReturn(1, 2);
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = CaseDataNormalizer.normalise(
            CaseDataBuilder.builder().getDefaultJudgment1v1Case(),
            baseDate
        );

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDefaultJudgment()).hasSize(1);
        assertThat(history.getDefaultJudgment().get(0).getEventCode())
            .isEqualTo(DEFAULT_JUDGMENT_GRANTED.getCode());
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventCode()).isEqualTo(MISCELLANEOUS.getCode());
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: Default Judgment granted and claim moved offline.");
    }

    @Test
    void grantedFlagEmitsRequestedMessageOnly() {
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(false);
        when(sequenceGenerator.nextSequence(any())).thenReturn(1);
        when(timelineHelper.now()).thenReturn(baseDate.atTime(10, 0));

        CaseData caseData = CaseDataNormalizer.normalise(
            CaseDataBuilder.builder().getDefaultJudgment1v2DivergentCase(),
            baseDate
        );

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDefaultJudgment()).isNullOrEmpty();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: Default Judgment requested and claim moved offline.");
    }

    @Test
    void joLiveFeedUsesRecordJudgmentMessage() {
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        when(sequenceGenerator.nextSequence(any())).thenReturn(1, 2);
        when(timelineHelper.now()).thenReturn(baseDate.atTime(12, 0));
        when(partyLookup.respondentId(0)).thenReturn("002");

        CaseData caseData = CaseDataNormalizer.normalise(
            CaseDataBuilder.builder().getDefaultJudgment1v1Case(),
            baseDate
        );

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("Judgment recorded.");
    }
}
