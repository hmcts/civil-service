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

class CaseNotesStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    private CaseNotesStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(5, 6, 7);
        strategy = new CaseNotesStrategy(sequenceGenerator, new RoboticsEventTextFormatter());
    }

    @Test
    void supportsReturnsFalseWhenNoCaseNotes() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(strategy.supports(caseData)).isFalse();
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

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsMiscellaneousEventsForEachCaseNote() {
        LocalDateTime createdOn = LocalDateTime.now();
        CaseNote firstNote = new CaseNote();
        firstNote.setCreatedBy("user");
        firstNote.setCreatedOn(createdOn);
        firstNote.setNote("first   note");

        CaseNote secondNote = new CaseNote();
        secondNote.setCreatedBy("user");
        secondNote.setCreatedOn(createdOn.plusDays(1));

        CaseData caseData = CaseDataBuilder.builder()
            .build();
        caseData.setCaseNotes(wrapElements(of(firstNote, secondNote)));

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

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

    @Test
    void contributeTrimsLongNotesToMaximumLength() {
        String longNote = "x".repeat(300);
        CaseNote note = new CaseNote();
        note.setCreatedOn(LocalDateTime.now());
        note.setNote(longNote);

        CaseData caseData = CaseDataBuilder.builder()
            .build();
        caseData.setCaseNotes(wrapElements(note));

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        String prefix = "case note added: ";
        String expected = prefix + "x".repeat(250 - prefix.length());
        assertThat(builder.build().getMiscellaneous().get(0).getEventDetailsText()).isEqualTo(expected);
    }

    @Test
    void contributeHandlesNullCaseNoteFields() {
        CaseNote emptyNote = new CaseNote();
        CaseData caseData = CaseDataBuilder.builder()
            .build();
        caseData.setCaseNotes(wrapElements(emptyNote));

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText()).isEqualTo("case note added: ");
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isNull();
    }
}
