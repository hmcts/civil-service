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
            .caseNotes(new CaseNote()
                .setCreatedBy("user")
                .setCreatedOn(LocalDateTime.now())
                .setNote("note"))
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

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).hasSize(2);

        assertThat(builder.getMiscellaneous().getFirst().getEventCode())
            .isEqualTo(EventType.MISCELLANEOUS.getCode());
        assertThat(builder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(5);
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText())
            .isEqualTo("case note added: first note");
        assertThat(builder.getMiscellaneous().getFirst().getEventDetails().getMiscText())
            .isEqualTo("case note added: first note");
        assertThat(builder.getMiscellaneous().getFirst().getDateReceived()).isEqualTo(createdOn);

        assertThat(builder.getMiscellaneous().get(1).getEventSequence()).isEqualTo(6);
        assertThat(builder.getMiscellaneous().get(1).getEventDetailsText())
            .isEqualTo("case note added: ");
        assertThat(builder.getMiscellaneous().get(1).getDateReceived()).isEqualTo(createdOn.plusDays(1));
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

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        String prefix = "case note added: ";
        String expected = prefix + "x".repeat(250 - prefix.length());
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText()).isEqualTo(expected);
    }

    @Test
    void contributeHandlesNullCaseNoteFields() {
        CaseNote emptyNote = new CaseNote();
        CaseData caseData = CaseDataBuilder.builder()
            .build();
        caseData.setCaseNotes(wrapElements(emptyNote));

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText()).isEqualTo("case note added: ");
        assertThat(builder.getMiscellaneous().getFirst().getDateReceived()).isNull();
    }
}
