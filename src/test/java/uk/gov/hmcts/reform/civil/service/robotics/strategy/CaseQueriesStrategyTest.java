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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setQmApplicantSolicitorQueries(null);

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenNoQueries() {
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineDate(LocalDateTime.now())
            .build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsUsesPublicQueryToggle() {
        when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);
        CaseQueriesCollection queries = new CaseQueriesCollection();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdState(CaseState.CASE_DISMISSED);
        caseData.setQueries(queries);

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsMiscEventWithFallbackDate() {
        CaseQueriesCollection queries = new CaseQueriesCollection();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdState(CaseState.CASE_DISMISSED);
        caseData.setQmApplicantSolicitorQueries(queries);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(60);
        assertThat(builder.getMiscellaneous().getFirst().getDateReceived())
            .isEqualTo(LocalDateTime.of(2024, 2, 11, 10, 0));
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText())
            .isEqualTo("There has been a query on this case");
    }
}
