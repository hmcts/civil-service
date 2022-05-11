package uk.gov.hmcts.reform.civil.model.dq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.UnavailableDateLRspec;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
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
    void shouldGetVariables_usingInterfaceMethodsHearingLRSpec() {
        Applicant1DQ dq = buildApplicant1DqHearingLRSpec();

        assertEquals(hearingLRspec(), dq.getHearing());
    }

    @Test
    void shouldReturnSmallClaimHearing_whenHearingNull() {
        YesOrNo hasUnavailableDates = YES;
        List<Element<UnavailableDateLRspec>> lrDates = Stream.of(
            UnavailableDateLRspec.builder()
                .date(LocalDate.of(2020, 5, 2))
                .who("who 1")
                .build(),
            UnavailableDateLRspec.builder()
                .fromDate(LocalDate.of(2020, 5, 2))
                .toDate(LocalDate.of(2020, 6, 2))
                .who("who 2")
                .build()
        ).map(ElementUtils::element).collect(Collectors.toList());

        Hearing hearing = buildApplicant1Dq().toBuilder()
            .applicant1DQHearing(null)
            .applicant1DQSmallClaimHearing(SmallClaimHearing.builder()
                                                .unavailableDatesRequired(hasUnavailableDates)
                                                .smallClaimUnavailableDate(lrDates)
                                                .build())
            .build().getHearing();

        assertThat(hearing.getUnavailableDatesRequired()).isEqualTo(hasUnavailableDates);
        for (int i = 0; i < hearing.getUnavailableDates().size(); i++) {
            UnavailableDateLRspec expected = lrDates.get(i).getValue();
            UnavailableDate actual = hearing.getUnavailableDates().get(i).getValue();
            assertThat(actual.getWho()).isEqualTo(expected.getWho());
            assertThat(actual.getDate()).isEqualTo(expected.getDate());
            assertThat(actual.getFromDate()).isEqualTo(expected.getFromDate());
            assertThat(actual.getToDate()).isEqualTo(expected.getToDate());
        }
    }

    private Applicant1DQ buildApplicant1Dq() {
        return Applicant1DQ.builder()
            .applicant1DQDisclosureOfElectronicDocuments(disclosureOfElectronicDocuments())
            .applicant1DQDisclosureOfNonElectronicDocuments(disclosureOfNonElectronicDocuments())
            .applicant1DQDisclosureReport(disclosureReport())
            .applicant1DQDraftDirections(draftDirections())
            .applicant1DQExperts(experts())
            .applicant1DQFileDirectionsQuestionnaire(fileDirectionsQuestionnaire())
            .applicant1DQFurtherInformation(furtherInformation())
            .applicant1DQHearing(hearing())
            .applicant1DQHearingSupport(hearingSupport())
            .applicant1DQRequestedCourt(requestedCourt())
            .applicant1DQStatementOfTruth(statementOfTruth())
            .applicant1DQWitnesses(witnesses())
            .applicant1DQLanguage(welshLanguageRequirements())
            .applicant1DQVulnerabilityQuestions(vulnerabilityQuestions())
            .build();
    }

    private Applicant1DQ buildApplicant1DqHearingLRSpec() {
        return Applicant1DQ.builder()
            .applicant1DQHearingLRspec(hearingLRspec())
            .build();
    }

    @Nested
    class GetExperts {

        @Test
        void shouldRemoveDetails_whenNoExpertsRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQExperts(dq.getExperts().toBuilder()
                                          .expertRequired(NO)
                                          .build())
                .build();

            assertThat(dq.getExperts().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenExpertsRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQExperts(dq.getExperts().toBuilder()
                                          .expertRequired(YES)
                                          .build())
                .build();

            assertThat(dq.getExperts()).isEqualTo(experts());
        }

        @Test
        void shouldReturnNull_whenExpertsIsNull() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQExperts(null)
                .build();

            assertThat(dq.getExperts()).isNull();
        }
    }

    @Nested
    class GetWitnesses {

        @Test
        void shouldRemoveDetails_whenNoWitnessesToAppear() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQWitnesses(dq.getWitnesses().toBuilder()
                                            .witnessesToAppear(NO)
                                            .build())
                .build();

            assertThat(dq.getWitnesses().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenWitnessesToAppear() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQWitnesses(dq.getWitnesses().toBuilder()
                                            .witnessesToAppear(YES)
                                            .build())
                .build();

            assertThat(dq.getWitnesses()).isEqualTo(witnesses());
        }

        @Test
        void shouldReturnNull_whenWitnessesIsNull() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQWitnesses(null)
                .build();

            assertThat(dq.getWitnesses()).isNull();
        }
    }

    @Nested
    class GetHearing {

        @Test
        void shouldRemoveUnavailableDates_whenNoUnavailableDatesRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQHearing(dq.getHearing().toBuilder()
                                          .unavailableDatesRequired(NO)
                                          .build())
                .build();

            assertThat(dq.getHearing().getUnavailableDates()).isNull();
        }

        @Test
        void shouldNotRemoveUnavailableDates_whenUnavailableDatesRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQHearing(dq.getHearing().toBuilder()
                                          .unavailableDatesRequired(YES)
                                          .build())
                .build();

            assertThat(dq.getHearing().getUnavailableDates()).isEqualTo(hearing().getUnavailableDates());
        }

        @Test
        void shouldReturnNull_whenUnavailableDatesIsNull() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQHearing(null)
                .build();

            assertThat(dq.getHearing()).isNull();
        }
    }
}
