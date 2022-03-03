package uk.gov.hmcts.reform.civil.model.dq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
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

class Respondent1DQTest extends DQTest {

    @Test
    void shouldGetVariables_usingInterfaceMethods() {
        Respondent1DQ dq = buildRespondent1Dq();

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
    }

    private Respondent1DQ buildRespondent1Dq() {
        return Respondent1DQ.builder()
            .respondent1DQDisclosureOfElectronicDocuments(disclosureOfElectronicDocuments())
            .respondent1DQDisclosureOfNonElectronicDocuments(disclosureOfNonElectronicDocuments())
            .respondent1DQDisclosureReport(disclosureReport())
            .respondent1DQDraftDirections(draftDirections())
            .respondent1DQExperts(experts())
            .respondent1DQFileDirectionsQuestionnaire(fileDirectionsQuestionnaire())
            .respondent1DQFurtherInformation(furtherInformation())
            .respondent1DQHearing(hearing())
            .respondent1DQHearingSupport(hearingSupport())
            .respondent1DQRequestedCourt(requestedCourt())
            .respondent1DQStatementOfTruth(statementOfTruth())
            .respondent1DQWitnesses(witnesses())
            .respondent1DQLanguage(welshLanguageRequirements())
            .build();
    }

    @Nested
    class GetExperts {

        @Test
        void shouldRemoveDetails_whenNoExpertsRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQExperts(dq.getExperts().toBuilder()
                                          .expertRequired(NO)
                                          .build())
                .build();

            assertThat(dq.getExperts().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenExpertsRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQExperts(dq.getExperts().toBuilder()
                                          .expertRequired(YES)
                                          .build())
                .build();

            assertThat(dq.getExperts()).isEqualTo(experts());
        }

        @Test
        void shouldReturnNull_whenExpertsIsNull() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQExperts(null)
                .build();

            assertThat(dq.getExperts()).isNull();
        }
    }

    @Nested
    class GetWitnesses {

        @Test
        void shouldRemoveDetails_whenNoWitnessesToAppear() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQWitnesses(dq.getWitnesses().toBuilder()
                                            .witnessesToAppear(NO)
                                            .build())
                .build();

            assertThat(dq.getWitnesses().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenWitnessesToAppear() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQWitnesses(dq.getWitnesses().toBuilder()
                                            .witnessesToAppear(YES)
                                            .build())
                .build();

            assertThat(dq.getWitnesses()).isEqualTo(witnesses());
        }

        @Test
        void shouldReturnNull_whenWitnessesIsNull() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQWitnesses(null)
                .build();

            assertThat(dq.getWitnesses()).isNull();
        }
    }

    @Nested
    class GetHearing {

        @Test
        void shouldRemoveUnavailableDates_whenNoUnavailableDatesRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQHearing(dq.getHearing().toBuilder()
                                          .unavailableDatesRequired(NO)
                                          .build())
                .build();

            assertThat(dq.getHearing().getUnavailableDates()).isNull();
        }

        @Test
        void shouldNotRemoveUnavailableDates_whenUnavailableDatesRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQHearing(dq.getHearing().toBuilder()
                                          .unavailableDatesRequired(YES)
                                          .build())
                .build();

            assertThat(dq.getHearing().getUnavailableDates()).isEqualTo(hearing().getUnavailableDates());
        }

        @Test
        void shouldReturnNull_whenUnavailableDatesIsNull() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQHearing(null)
                .build();

            assertThat(dq.getHearing()).isNull();
        }

        @Test
        void shouldReturnFastClaimHearing_whenHearingNull() {
            HearingLength length = HearingLength.MORE_THAN_DAY;
            String lengthDays = "2";
            String lengthHours = "6";
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

            Hearing hearing = buildRespondent1Dq().toBuilder()
                .respondent1DQHearing(null)
                .respondent1DQHearingFastClaim(HearingLRspec.builder()
                                                   .hearingLength(length)
                                                   .hearingLengthDays(lengthDays)
                                                   .hearingLengthHours(lengthHours)
                                                   .unavailableDatesRequired(hasUnavailableDates)
                                                   .unavailableDatesLRspec(lrDates)
                                                   .build())
                .build().getHearing();

            assertThat(hearing.getHearingLength()).isEqualTo(length);
            assertThat(hearing.getHearingLengthDays()).isEqualTo(lengthDays);
            assertThat(hearing.getHearingLengthHours()).isEqualTo(lengthHours);
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

            Hearing hearing = buildRespondent1Dq().toBuilder()
                .respondent1DQHearing(null)
                .respondent1DQHearingSmallClaim(SmallClaimHearing.builder()
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
    }

    @Nested
    class GetCourtLocation {

        @Test
        void build_whenYesRequired() {
            RequestedCourt court = buildRespondent1Dq().toBuilder()
                .respondent1DQRequestedCourt(null)
                .responseClaimCourtLocationRequired(YES)
                .build().getRequestedCourt();

            assertThat(court.getRequestHearingAtSpecificCourt()).isEqualTo(YES);
        }

        @Test
        void build_whenRespondToCourtLocation() {
            String reason = "reason";
            String courtCode = "123";
            RequestedCourt court = buildRespondent1Dq().toBuilder()
                .respondent1DQRequestedCourt(null)
                .respondToCourtLocation(RequestedCourt.builder()
                                            .responseCourtCode(courtCode)
                                            .reasonForHearingAtSpecificCourt(reason)
                                            .requestHearingAtSpecificCourt(YES)
                                            .build())
                .build().getRequestedCourt();

            assertThat(court.getRequestHearingAtSpecificCourt()).isEqualTo(YES);
            assertThat(court.getResponseCourtCode()).isEqualTo(courtCode);
            assertThat(court.getReasonForHearingAtSpecificCourt()).isEqualTo(reason);
        }

    }
}
