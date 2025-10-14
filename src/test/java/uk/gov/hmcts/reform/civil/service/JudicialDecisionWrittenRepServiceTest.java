package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeWrittenRepresentationsOptions;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialWrittenRepresentations;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.service.JudicialDecisionWrittenRepService.WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST;

@SpringBootTest(classes = {
    JudicialDecisionWrittenRepService.class,
    JacksonAutoConfiguration.class,
})
public class JudicialDecisionWrittenRepServiceTest {

    @Autowired
    private JudicialDecisionWrittenRepService service;

    @Test
    void shouldReturnErrors_whenWrittenRepresentationSequentialRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = GAJudicialWrittenRepresentations.builder()
            .writtenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .writtenSequentailRepresentationsBy(LocalDate.now().minusDays(1))
            .sequentialApplicantMustRespondWithin(LocalDate.now())
            .build();

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldReturnErrors_whenApplicantWrittenRepresentationSequentialRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = GAJudicialWrittenRepresentations.builder()
            .writtenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .writtenSequentailRepresentationsBy(LocalDate.now())
            .sequentialApplicantMustRespondWithin(LocalDate.now().minusDays(1))
            .build();

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldReturnErrors_whenBothWrittenRepresentationSequentialRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = GAJudicialWrittenRepresentations.builder()
            .writtenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .writtenSequentailRepresentationsBy(LocalDate.now().minusDays(1))
            .sequentialApplicantMustRespondWithin(LocalDate.now().minusDays(1))
            .build();

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldReturnErrors_whenWrittenRepresentationConcurentRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = GAJudicialWrittenRepresentations.builder()
            .writtenOption(GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS)
            .writtenConcurrentRepresentationsBy(LocalDate.now().minusDays(1))
            .build();

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldNotReturnErrors_whenBothWrittenRepresentationSequentialRepresentationsDateIsInFuture() {
        GAJudicialWrittenRepresentations writtenRepresentations = GAJudicialWrittenRepresentations.builder()
            .writtenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .sequentialApplicantMustRespondWithin(LocalDate.now())
            .writtenSequentailRepresentationsBy(LocalDate.now())
            .build();

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenWrittenRepresentationConcurentRepresentationsDateIsInFuture() {
        GAJudicialWrittenRepresentations writtenRepresentations = GAJudicialWrittenRepresentations.builder()
            .writtenOption(GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS)
            .writtenConcurrentRepresentationsBy(LocalDate.now())
            .build();

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isEmpty();
    }
}
