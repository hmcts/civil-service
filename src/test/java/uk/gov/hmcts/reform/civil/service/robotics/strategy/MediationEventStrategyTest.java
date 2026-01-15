package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.prepareEventDetailsText;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;

class MediationEventStrategyTest {

    @Mock
    private RoboticsTimelineHelper timelineHelper;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsEventTextFormatter textFormatter;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    private MediationEventStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new MediationEventStrategy(timelineHelper, sequenceGenerator, textFormatter, stateFlowEngine);
        when(textFormatter.inMediation()).thenReturn("IN MEDIATION");
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.IN_MEDIATION.fullName())));
    }

    @Test
    void supportsReturnsFalseWhenEitherPartyHasNotAgreed() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenBothPartiesAgree() {
        CaseData caseData = baseCaseDataBuilder(CaseCategory.SPEC_CLAIM).build();
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setCaseDataLiP(createCaseDataLiP());

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeDoesNothingWhenUnsupported() {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, CaseDataBuilder.builder().build(), null);
        assertThat(builder.build().getMiscellaneous()).isNullOrEmpty();
    }

    @Test
    void addsDirectionsQuestionnaireAndMiscEventForSpecClaims() {
        FileDirectionsQuestionnaire dq = new FileDirectionsQuestionnaire();
        dq.setOneMonthStayRequested(YesOrNo.YES);
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setResponseCourtCode("432");
        Applicant1DQ applicant1DQ = new Applicant1DQ();
        applicant1DQ.setApplicant1DQFileDirectionsQuestionnaire(dq);
        applicant1DQ.setApplicant1DQRequestedCourt(requestedCourt);

        LocalDateTime responseDate = LocalDateTime.now().plusDays(1);
        CaseData caseData = baseCaseDataBuilder(CaseCategory.SPEC_CLAIM)
            .applicant1ResponseDate(responseDate)
            .applicant1DQ(applicant1DQ)
            .build();
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setCaseDataLiP(createCaseDataLiP());

        when(sequenceGenerator.nextSequence(any())).thenReturn(11, 12);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);
        EventHistory history = builder.build();

        assertThat(history.getDirectionsQuestionnaireFiled()).hasSize(1);
        Event dqEvent = history.getDirectionsQuestionnaireFiled().get(0);
        assertThat(dqEvent.getEventSequence()).isEqualTo(11);
        assertThat(dqEvent.getLitigiousPartyID()).isEqualTo(APPLICANT_ID);
        assertThat(dqEvent.getEventDetails().getPreferredCourtCode()).isEqualTo("432");
        assertThat(dqEvent.getEventDetails().getStayClaim()).isTrue();
        assertThat(dqEvent.getEventDetailsText())
            .isEqualTo(prepareEventDetailsText(caseData.getApplicant1DQ(), "432"));

        assertThat(history.getMiscellaneous()).hasSize(1);
        Event miscEvent = history.getMiscellaneous().get(0);
        assertThat(miscEvent.getEventSequence()).isEqualTo(12);
        assertThat(miscEvent.getDateReceived()).isEqualTo(responseDate);
        assertThat(miscEvent.getEventDetailsText()).isEqualTo("IN MEDIATION");
    }

    @Test
    void addsOnlyMiscEventWhenClaimIsUnspec() {
        LocalDateTime responseDate = LocalDateTime.now().plusDays(1);
        CaseData caseData = baseCaseDataBuilder(CaseCategory.UNSPEC_CLAIM)
            .applicant1ResponseDate(responseDate)
            .build();
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        caseData.setCaseDataLiP(createCaseDataLiP());

        when(sequenceGenerator.nextSequence(any())).thenReturn(5);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDirectionsQuestionnaireFiled()).isNullOrEmpty();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText()).isEqualTo("IN MEDIATION");
    }

    private CaseDataBuilder baseCaseDataBuilder(CaseCategory category) {
        ClaimantMediationLip lip = new ClaimantMediationLip();
        lip.setHasAgreedFreeMediation(MediationDecision.Yes);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1ClaimMediationSpecRequiredLip(lip);

        return CaseDataBuilder.builder()
            .caseAccessCategory(category)
            .applicant1ResponseDate(LocalDate.now().atStartOfDay());
    }

    private CaseDataLiP createCaseDataLiP() {
        ClaimantMediationLip lip = new ClaimantMediationLip();
        lip.setHasAgreedFreeMediation(MediationDecision.Yes);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1ClaimMediationSpecRequiredLip(lip);
        return caseDataLiP;
    }
}
