package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RoboticsEventSupportTest {

    private EventHistory.EventHistoryBuilder builder;
    private RoboticsSequenceGenerator sequenceGenerator;

    @BeforeEach
    void setUp() {
        builder = EventHistory.builder();
        sequenceGenerator = new RoboticsSequenceGenerator();
    }

    @Test
    void buildMiscEvent_populatesCoreFieldsAndSequence() {
        LocalDateTime timestamp = LocalDateTime.of(2024, 6, 1, 9, 0);
        String message = "RPA Reason: Test message";

        Event event = RoboticsEventSupport.buildMiscEvent(builder, sequenceGenerator, message, timestamp);

        assertThat(event.getEventSequence()).isEqualTo(1);
        assertThat(event.getEventCode()).isEqualTo("999");
        assertThat(event.getDateReceived()).isEqualTo(timestamp);
        assertThat(event.getEventDetailsText()).isEqualTo(message);
        assertThat(event.getEventDetails().getMiscText()).isEqualTo(message);
    }

    @Test
    void buildDirectionsQuestionnaireEvent_keepsNullPreferredCourtOverride() {
        FileDirectionsQuestionnaire fdq = new FileDirectionsQuestionnaire();
        fdq.setOneMonthStayRequested(YesOrNo.YES);
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setResponseCourtCode("123");
        Applicant1DQ dq = buildApplicantDq(fdq, requestedCourt);

        Event event = RoboticsEventSupport.buildDirectionsQuestionnaireEvent(
            builder,
            sequenceGenerator,
            LocalDateTime.of(2024, 6, 1, 10, 0),
            "001",
            dq,
            null,
            "details"
        );

        assertThat(event.getEventSequence()).isEqualTo(1);
        assertThat(event.getEventCode()).isEqualTo("197");
        assertThat(event.getEventDetails().getPreferredCourtCode()).isNull();
        assertThat(event.getEventDetails().getStayClaim()).isTrue();
        assertThat(event.getEventDetailsText()).isEqualTo("details");
    }

    @Test
    void buildDirectionsQuestionnaireEvent_respectsPreferredCourtOverride() {
        FileDirectionsQuestionnaire fdq = new FileDirectionsQuestionnaire();
        fdq.setOneMonthStayRequested(YesOrNo.NO);
        Applicant1DQ dq = buildApplicantDq(fdq, null);

        Event event = RoboticsEventSupport.buildDirectionsQuestionnaireEvent(
            builder,
            sequenceGenerator,
            LocalDateTime.of(2024, 6, 2, 11, 0),
            "002",
            dq,
            "OVERRIDE",
            "details"
        );

        assertThat(event.getEventSequence()).isEqualTo(1);
        assertThat(event.getEventDetails().getPreferredCourtCode()).isEqualTo("OVERRIDE");
        assertThat(event.getEventDetails().getStayClaim()).isFalse();
    }

    private Applicant1DQ buildApplicantDq(FileDirectionsQuestionnaire fileDirectionsQuestionnaire,
                                          RequestedCourt requestedCourt) {
        Applicant1DQ dq = new Applicant1DQ();
        dq.setApplicant1DQFileDirectionsQuestionnaire(fileDirectionsQuestionnaire);
        dq.setApplicant1DQRequestedCourt(requestedCourt);
        return dq;
    }
}
