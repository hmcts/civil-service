package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDateTime;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class CaseNotesContributorTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    private CaseNotesContributor contributor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(5, 6, 7);
        contributor = new CaseNotesContributor(sequenceGenerator, new RoboticsEventTextFormatter());
    }

    @Test
    void supportsReturnsFalseWhenNoCaseNotes() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenCaseNotesPresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseNotes(CaseNote.builder()
                .createdBy("user")
                .createdOn(LocalDateTime.now())
                .note("note")
                .build())
            .build();

        assertThat(contributor.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsMiscellaneousEventsForEachCaseNote() {
        LocalDateTime createdOn = LocalDateTime.now();
        CaseNote firstNote = CaseNote.builder()
            .createdBy("user")
            .createdOn(createdOn)
            .note("first   note")
            .build();
        CaseNote secondNote = CaseNote.builder()
            .createdBy("user")
            .createdOn(createdOn.plusDays(1))
            .note(null)
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .build()
            .toBuilder()
            .caseNotes(wrapElements(of(firstNote, secondNote)))
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(2);

        assertThat(history.getMiscellaneous().get(0).getEventCode())
            .isEqualTo(EventType.MISCELLANEOUS.getCode());
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(5);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("case note added: first note");
        assertThat(history.getMiscellaneous().get(0).getEventDetails().getMiscText())
            .isEqualTo("case note added: first note");
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(createdOn);

        assertThat(history.getMiscellaneous().get(1).getEventSequence()).isEqualTo(6);
        assertThat(history.getMiscellaneous().get(1).getEventDetailsText())
            .isEqualTo("case note added: ");
        assertThat(history.getMiscellaneous().get(1).getDateReceived()).isEqualTo(createdOn.plusDays(1));
    }
}
