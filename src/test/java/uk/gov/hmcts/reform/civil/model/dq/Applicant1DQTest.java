package uk.gov.hmcts.reform.civil.model.dq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class Applicant1DQTest extends DQTest {

    @Test
    void shouldGetVariables_usingInterfaceMethods() {
        Applicant1DQ dq = buildApplicant1Dq();

        assertEquals(disclosureOfElectronicDocuments(), dq.getDisclosureOfElectronicDocuments());
        assertEquals(disclosureOfNonElectronicDocuments(), dq.getDisclosureOfNonElectronicDocuments());
        assertEquals(disclosureReport(), dq.getDisclosureReport());
        assertEquals(draftDirections(), dq.getDraftDirections());
        assertEquals(experts(), dq.getExperts());
        assertEquals(fileDirectionsQuestionnaire(), dq.getFileDirectionQuestionnaire());
        assertEquals(furtherInformation(), dq.getFurtherInformation());
        assertEquals(hearing(), dq.getHearing());
        assertEquals(hearingSupport(), dq.getHearingSupport());
        assertEquals(requestedCourt(), dq.getRequestedCourt());
        assertEquals(statementOfTruth(), dq.getStatementOfTruth());
        assertEquals(witnesses(), dq.getWitnesses());
        assertEquals(welshLanguageRequirements(), dq.getWelshLanguageRequirements());
        assertEquals(vulnerabilityQuestions(), dq.getVulnerabilityQuestions());
    }

    @Test
    void shouldReturnFastClaimHearing_whenHearingNull() {
        HearingLength length = HearingLength.MORE_THAN_DAY;
        String lengthDays = "2";
        String lengthHours = "6";
        YesOrNo hasUnavailableDates = YES;
        List<Element<UnavailableDate>> lrDates = Stream.of(
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.of(2020, 5, 2))
                .who("who 1")
                .build(),
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.of(2020, 5, 2))
                .toDate(LocalDate.of(2020, 6, 2))
                .who("who 2")
                .build()
        ).map(ElementUtils::element).toList();

        Hearing hearing = buildApplicant1Dq().copy()
            .setApplicant1DQHearing(null)
            .setApplicant1DQHearingLRspec(new Hearing()
                                              .setHearingLength(length)
                                              .setHearingLengthDays(lengthDays)
                                              .setHearingLengthHours(lengthHours)
                                              .setUnavailableDatesRequired(hasUnavailableDates)
                                              .setUnavailableDates(lrDates))
            .getHearing();

        assertThat(hearing.getHearingLength()).isEqualTo(length);
        assertThat(hearing.getHearingLengthDays()).isEqualTo(lengthDays);
        assertThat(hearing.getHearingLengthHours()).isEqualTo(lengthHours);
        assertThat(hearing.getUnavailableDatesRequired()).isEqualTo(hasUnavailableDates);
        for (int i = 0; i < hearing.getUnavailableDates().size(); i++) {
            UnavailableDate expected = lrDates.get(i).getValue();
            UnavailableDate actual = hearing.getUnavailableDates().get(i).getValue();
            assertThat(actual.getUnavailableDateType()).isEqualTo(expected.getUnavailableDateType());
            assertThat(actual.getWho()).isEqualTo(expected.getWho());
            assertThat(actual.getDate()).isEqualTo(expected.getDate());
            assertThat(actual.getFromDate()).isEqualTo(expected.getFromDate());
            assertThat(actual.getToDate()).isEqualTo(expected.getToDate());
        }
    }

    @Test
    void shouldReturnSmallClaimHearing_whenHearingNull() {
        YesOrNo hasUnavailableDates = YES;
        List<Element<UnavailableDate>> lrDates = Stream.of(
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.of(2020, 5, 2))
                .who("who 1")
                .build(),
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.of(2020, 5, 2))
                .toDate(LocalDate.of(2020, 6, 2))
                .who("who 2")
                .build()
        ).map(ElementUtils::element).toList();

        Hearing hearing = buildApplicant1Dq().copy()
            .setApplicant1DQHearing(null)
            .setApplicant1DQSmallClaimHearing(new SmallClaimHearing()
                                                  .setUnavailableDatesRequired(hasUnavailableDates)
                                                  .setSmallClaimUnavailableDate(lrDates))
            .getHearing();

        assertThat(hearing.getUnavailableDatesRequired()).isEqualTo(hasUnavailableDates);
        for (int i = 0; i < hearing.getUnavailableDates().size(); i++) {
            UnavailableDate expected = lrDates.get(i).getValue();
            UnavailableDate actual = hearing.getUnavailableDates().get(i).getValue();
            assertThat(actual.getUnavailableDateType()).isEqualTo(expected.getUnavailableDateType());
            assertThat(actual.getWho()).isEqualTo(expected.getWho());
            assertThat(actual.getDate()).isEqualTo(expected.getDate());
            assertThat(actual.getFromDate()).isEqualTo(expected.getFromDate());
            assertThat(actual.getToDate()).isEqualTo(expected.getToDate());
        }
    }

    private Applicant1DQ buildApplicant1Dq() {
        return new Applicant1DQ()
            .setApplicant1DQDisclosureOfElectronicDocuments(disclosureOfElectronicDocuments())
            .setApplicant1DQDisclosureOfNonElectronicDocuments(disclosureOfNonElectronicDocuments())
            .setApplicant1DQDisclosureReport(disclosureReport())
            .setApplicant1DQDraftDirections(draftDirections())
            .setApplicant1DQExperts(experts())
            .setApplicant1DQFileDirectionsQuestionnaire(fileDirectionsQuestionnaire())
            .setApplicant1DQFurtherInformation(furtherInformation())
            .setApplicant1DQHearing(hearing())
            .setApplicant1DQHearingSupport(hearingSupport())
            .setApplicant1DQRequestedCourt(requestedCourt())
            .setApplicant1DQStatementOfTruth(statementOfTruth())
            .setApplicant1DQWitnesses(witnesses())
            .setApplicant1DQLanguage(welshLanguageRequirements())
            .setApplicant1DQVulnerabilityQuestions(vulnerabilityQuestions());
    }

    private Applicant1DQ buildApplicant1DqHearingLRSpec() {
        return new Applicant1DQ()
            .setApplicant1DQHearingLRspec(hearingLRspec());
    }

    @Nested
    class GetExperts {

        @Test
        void shouldRemoveDetails_whenNoExpertsRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.copy()
                .setApplicant1DQExperts(dq.getExperts().copy()
                                            .setExpertRequired(NO));

            assertThat(dq.getExperts().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenExpertsRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.copy()
                .setApplicant1DQExperts(dq.getExperts().copy()
                                            .setExpertRequired(YES));

            assertThat(dq.getExperts()).isEqualTo(experts());
        }

        @Test
        void shouldReturnNull_whenExpertsIsNull() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.copy()
                .setApplicant1DQExperts(null);

            assertThat(dq.getExperts()).isNull();
        }
    }

    @Nested
    class GetWitnesses {

        @Test
        void shouldRemoveDetails_whenNoWitnessesToAppear() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.copy()
                .setApplicant1DQWitnesses(dq.getWitnesses().copy()
                                              .setWitnessesToAppear(NO));

            assertThat(dq.getWitnesses().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenWitnessesToAppear() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.copy()
                .setApplicant1DQWitnesses(dq.getWitnesses().copy()
                                              .setWitnessesToAppear(YES));

            assertThat(dq.getWitnesses()).isEqualTo(witnesses());
        }

        @Test
        void shouldReturnNull_whenWitnessesIsNull() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.copy()
                .setApplicant1DQWitnesses(null);

            assertThat(dq.getWitnesses()).isNull();
        }
    }

    @Nested
    class GetHearing {

        @Test
        void shouldRemoveUnavailableDates_whenNoUnavailableDatesRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.copy()
                .setApplicant1DQHearing(dq.getHearing().copy()
                                            .setUnavailableDatesRequired(NO));

            assertThat(dq.getHearing().getUnavailableDates()).isNull();
        }

        @Test
        void shouldNotRemoveUnavailableDates_whenUnavailableDatesRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.copy()
                .setApplicant1DQHearing(dq.getHearing().copy()
                                            .setUnavailableDatesRequired(YES));

            assertThat(dq.getHearing().getUnavailableDates()).isEqualTo(hearing().getUnavailableDates());
        }

        @Test
        void shouldReturnNull_whenUnavailableDatesIsNull() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.copy()
                .setApplicant1DQHearing(null);

            assertThat(dq.getHearing()).isNull();
        }
    }
}
