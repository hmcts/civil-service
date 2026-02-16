package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

class GeneralApplicationStrikeOutStrategyTest {

    @Mock private RoboticsSequenceGenerator sequenceGenerator;

    @Mock private IStateFlowEngine stateFlowEngine;

    @Mock private StateFlow stateFlow;

    @InjectMocks private GeneralApplicationStrikeOutStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory())
                .thenReturn(List.of(State.from(FlowState.Main.TAKEN_OFFLINE_BY_STAFF.fullName())));
    }

    @Test
    void supportsReturnsFalseWhenNoStrikeOutApplications() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenStrikeOutApplicationPresent() {
        CaseData caseData =
                CaseDataBuilder.builder()
                        .getGeneralApplicationWithStrikeOut(RESPONDENT_ID)
                        .getGeneralStrikeOutApplicationsDetailsWithCaseState(
                                PROCEEDS_IN_HERITAGE.getDisplayedValue())
                        .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory())
                .thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));

        CaseData caseData =
                CaseDataBuilder.builder()
                        .getGeneralApplicationWithStrikeOut(RESPONDENT_ID)
                        .getGeneralStrikeOutApplicationsDetailsWithCaseState(
                                PROCEEDS_IN_HERITAGE.getDisplayedValue())
                        .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeAddsGeneralFormAndDefenceEvents() {
        CaseData caseData =
                CaseDataBuilder.builder()
                        .getGeneralApplicationWithStrikeOut(RESPONDENT_ID)
                        .getGeneralStrikeOutApplicationsDetailsWithCaseState(
                                PROCEEDS_IN_HERITAGE.getDisplayedValue())
                        .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getGeneralFormOfApplication()).hasSize(1);
        assertThat(builder.getGeneralFormOfApplication().getFirst().getEventCode())
                .isEqualTo(EventType.GENERAL_FORM_OF_APPLICATION.getCode());
        assertThat(builder.getGeneralFormOfApplication().getFirst().getEventDetailsText())
                .isEqualTo("APPLICATION TO Strike Out");

        assertThat(builder.getDefenceStruckOut()).hasSize(1);
        assertThat(builder.getDefenceStruckOut().getFirst().getEventCode())
                .isEqualTo(EventType.DEFENCE_STRUCK_OUT.getCode());
    }

    @Test
    void contributeSkipsWhenNoMatchingJudgeDecision() {
        CaseData caseData =
                CaseDataBuilder.builder().getGeneralApplicationWithStrikeOut(RESPONDENT_ID).build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getGeneralFormOfApplication()).isNullOrEmpty();
        assertThat(builder.getDefenceStruckOut()).isNullOrEmpty();
    }

    @Test
    void contributeHandlesMultipleApplications() {
        CaseData caseData =
                CaseDataBuilder.builder()
                        .getGeneralApplicationWithStrikeOut(RESPONDENT_ID)
                        .getGeneralStrikeOutApplicationsDetailsWithCaseState(
                                PROCEEDS_IN_HERITAGE.getDisplayedValue())
                        .build();
        Element<GeneralApplication> first = caseData.getGeneralApplications().getFirst();
        GeneralApplication secondValue =
                GeneralApplication.builder()
                        .applicantPartyName(first.getValue().getApplicantPartyName())
                        .litigiousPartyID("004")
                        .generalAppDateDeadline(first.getValue().getGeneralAppDateDeadline())
                        .generalAppSubmittedDateGAspec(first.getValue().getGeneralAppSubmittedDateGAspec())
                        .generalAppType(first.getValue().getGeneralAppType())
                        .caseLink(first.getValue().getCaseLink())
                        .businessProcess(first.getValue().getBusinessProcess())
                        .build();
        Element<GeneralApplication> second = new Element<>(UUID.randomUUID(), secondValue);
        caseData.setGeneralApplications(List.of(first, second));

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getGeneralFormOfApplication()).hasSize(2);
        assertThat(builder.getDefenceStruckOut()).hasSize(2);
    }
}
