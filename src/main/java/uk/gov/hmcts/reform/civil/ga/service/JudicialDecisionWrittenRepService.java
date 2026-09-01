package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.helpers.LocalDateTimeHelper.nowInLocalZone;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS;

@Service
@RequiredArgsConstructor
public class JudicialDecisionWrittenRepService {

    public static final String WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST =
        "The date entered cannot be in the past.";

    public List<String> validateWrittenRepresentationsDates(GAJudicialWrittenRepresentations
                                                                judicialWrittenRepresentationsDate) {
        List<String> errors = new ArrayList<>();
        if (judicialWrittenRepresentationsDate.getWrittenOption() == SEQUENTIAL_REPRESENTATIONS
            && judicialWrittenRepresentationsDate.getWrittenSequentailRepresentationsBy() != null) {
            LocalDate writtenSequentailRepresentationDate = judicialWrittenRepresentationsDate
                .getWrittenSequentailRepresentationsBy();
            LocalDate sequentialApplicantMustRespondWithinDate = judicialWrittenRepresentationsDate
                .getSequentialApplicantMustRespondWithin();
            if (nowInLocalZone().toLocalDate().isAfter(writtenSequentailRepresentationDate)
                || nowInLocalZone().toLocalDate().isAfter(sequentialApplicantMustRespondWithinDate)) {
                errors.add(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
            }
        }
        if (judicialWrittenRepresentationsDate.getWrittenOption() == CONCURRENT_REPRESENTATIONS
            && judicialWrittenRepresentationsDate.getWrittenConcurrentRepresentationsBy() != null) {
            LocalDate writtenConcurrentRepresentationDate = judicialWrittenRepresentationsDate
                .getWrittenConcurrentRepresentationsBy();
            if (nowInLocalZone().toLocalDate().isAfter(writtenConcurrentRepresentationDate)) {
                errors.add(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
            }
        }
        return errors;
    }
}

