package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionWrittenRepService.WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST;
import static uk.gov.hmcts.reform.civil.helpers.LocalDateTimeHelper.nowInLocalZone;

@ExtendWith(MockitoExtension.class)
public class JudicialDecisionWrittenRepServiceTest {

    @InjectMocks
    private JudicialDecisionWrittenRepService service;

    @Test
    void shouldReturnErrors_whenWrittenRepresentationSequentialRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .setWrittenSequentailRepresentationsBy(nowInLocalZone().toLocalDate().minusDays(1))
            .setSequentialApplicantMustRespondWithin(nowInLocalZone().toLocalDate())
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldReturnErrors_whenApplicantWrittenRepresentationSequentialRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .setWrittenSequentailRepresentationsBy(nowInLocalZone().toLocalDate())
            .setSequentialApplicantMustRespondWithin(nowInLocalZone().toLocalDate().minusDays(1))
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldReturnErrors_whenBothWrittenRepresentationSequentialRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .setWrittenSequentailRepresentationsBy(nowInLocalZone().toLocalDate().minusDays(1))
            .setSequentialApplicantMustRespondWithin(nowInLocalZone().toLocalDate().minusDays(1))
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldReturnErrors_whenWrittenRepresentationConcurentRepresentationsDateIsInPast() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS)
            .setWrittenConcurrentRepresentationsBy(nowInLocalZone().toLocalDate().minusDays(1))
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldNotReturnErrors_whenBothWrittenRepresentationSequentialRepresentationsDateIsInFuture() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
            .setSequentialApplicantMustRespondWithin(nowInLocalZone().toLocalDate())
            .setWrittenSequentailRepresentationsBy(nowInLocalZone().toLocalDate())
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenWrittenRepresentationConcurentRepresentationsDateIsInFuture() {
        GAJudicialWrittenRepresentations writtenRepresentations = new GAJudicialWrittenRepresentations()
            .setWrittenOption(GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS)
            .setWrittenConcurrentRepresentationsBy(nowInLocalZone().toLocalDate())
            ;

        List<String> errors = service.validateWrittenRepresentationsDates(writtenRepresentations);

        assertThat(errors).isEmpty();
    }
}
