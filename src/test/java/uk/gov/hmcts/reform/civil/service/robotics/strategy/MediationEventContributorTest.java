package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.prepareEventDetailsText;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;

class MediationEventContributorTest {

    @Mock
    private RoboticsTimelineHelper timelineHelper;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsEventTextFormatter textFormatter;

    @InjectMocks
    private MediationEventContributor contributor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(textFormatter.inMediation()).thenReturn("IN MEDIATION");
    }

    @Test
    void supportsReturnsFalseWhenEitherPartyHasNotAgreed() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .build();

        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenBothPartiesAgree() {
        CaseData caseData = baseCaseData(CaseCategory.SPEC_CLAIM);

        assertThat(contributor.supports(caseData)).isTrue();
    }

    @Test
    void contributeDoesNothingWhenUnsupported() {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, CaseData.builder().build(), null);
        assertThat(builder.build().getMiscellaneous()).isNullOrEmpty();
    }

    @Test
    void addsDirectionsQuestionnaireAndMiscEventForSpecClaims() {
        LocalDateTime responseDate = LocalDateTime.of(2024, 1, 15, 10, 0);
        CaseData caseData = baseCaseData(CaseCategory.SPEC_CLAIM).toBuilder()
            .applicant1ResponseDate(responseDate)
            .applicant1DQ(Applicant1DQ.builder()
                .applicant1DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                    .oneMonthStayRequested(YesOrNo.YES)
                    .build())
                .applicant1DQRequestedCourt(RequestedCourt.builder()
                    .responseCourtCode("432")
                    .build())
                .build())
            .build();

        when(sequenceGenerator.nextSequence(any())).thenReturn(11, 12);
        when(timelineHelper.ensurePresentOrNow(any())).thenReturn(responseDate);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);
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
        LocalDateTime responseDate = LocalDateTime.of(2024, 2, 1, 9, 30);
        CaseData caseData = baseCaseData(CaseCategory.UNSPEC_CLAIM).toBuilder()
            .applicant1ResponseDate(responseDate)
            .build();

        when(sequenceGenerator.nextSequence(any())).thenReturn(5);
        when(timelineHelper.ensurePresentOrNow(any())).thenReturn(responseDate);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDirectionsQuestionnaireFiled()).isNullOrEmpty();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText()).isEqualTo("IN MEDIATION");
    }

    private CaseData baseCaseData(CaseCategory category) {
        return CaseData.builder()
            .caseAccessCategory(category)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1ClaimMediationSpecRequiredLip(
                    ClaimantMediationLip.builder()
                        .hasAgreedFreeMediation(MediationDecision.Yes)
                        .build())
                .build())
            .applicant1ResponseDate(LocalDate.now().atStartOfDay())
            .build();
    }
}
