package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionWrittenRepService.WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST;

@ExtendWith(MockitoExtension.class)
public class JudicialDecisionWrittenRepServiceTest {

    @InjectMocks
    private JudicialDecisionWrittenRepService service;

    @Test
    void shouldReturnErrors_whenWrittenRepresentationSequentialRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .setWrittenSequentailRepresentationsBy(LocalDate.now().minusDays(1))
            .setSequentialApplicantMustRespondWithin(LocalDate.now())
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldReturnErrors_whenApplicantWrittenRepresentationSequentialRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .setWrittenSequentailRepresentationsBy(LocalDate.now())
            .setSequentialApplicantMustRespondWithin(LocalDate.now().minusDays(1))
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldReturnErrors_whenBothWrittenRepresentationSequentialRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .setWrittenSequentailRepresentationsBy(LocalDate.now().minusDays(1))
            .setSequentialApplicantMustRespondWithin(LocalDate.now().minusDays(1))
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldReturnErrors_whenWrittenRepresentationConcurentRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS)
            .setWrittenConcurrentRepresentationsBy(LocalDate.now().minusDays(1))
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldNotReturnErrors_whenBothWrittenRepresentationSequentialRepresentationsDateIsInFuture() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .setSequentialApplicantMustRespondWithin(LocalDate.now())
            .setWrittenSequentailRepresentationsBy(LocalDate.now())
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenWrittenRepresentationConcurentRepresentationsDateIsInFuture() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS)
            .setWrittenConcurrentRepresentationsBy(LocalDate.now())
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isEmpty();
    }
}
