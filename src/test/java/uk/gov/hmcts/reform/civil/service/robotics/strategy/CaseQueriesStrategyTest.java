package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CaseQueriesStrategyTest {

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsTimelineHelper timelineHelper;

    @InjectMocks
    private CaseQueriesStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sequenceGenerator.nextSequence(any())).thenReturn(60);
        when(timelineHelper.now()).thenReturn(LocalDateTime.of(2024, 2, 11, 10, 0));
        when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(false);
    }

    @Test
    void supportsReturnsFalseWhenNotOffline() {
        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(null)
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenNoQueries() {
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(LocalDateTime.now())
            .build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsUsesPublicQueryToggle() {
        when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_DISMISSED)
            .queries(CaseQueriesCollection.builder().build())
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsMiscEventWithFallbackDate() {
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_DISMISSED)
            .qmApplicantSolicitorQueries(CaseQueriesCollection.builder().build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(60);
        assertThat(history.getMiscellaneous().get(0).getDateReceived())
            .isEqualTo(LocalDateTime.of(2024, 2, 11, 10, 0));
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("There has been a query on this case");
    }
}
