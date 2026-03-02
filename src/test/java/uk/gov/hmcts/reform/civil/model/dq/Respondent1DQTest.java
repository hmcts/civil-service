package uk.gov.hmcts.reform.civil.model.dq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
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
        assertEquals(vulnerabilityQuestions(), dq.getVulnerabilityQuestions());
    }

    private Respondent1DQ buildRespondent1Dq() {
        return new Respondent1DQ()
            .setRespondent1DQDisclosureOfElectronicDocuments(disclosureOfElectronicDocuments())
            .setRespondent1DQDisclosureOfNonElectronicDocuments(disclosureOfNonElectronicDocuments())
            .setRespondent1DQDisclosureReport(disclosureReport())
            .setRespondent1DQDraftDirections(draftDirections())
            .setRespondent1DQExperts(experts())
            .setRespondent1DQFileDirectionsQuestionnaire(fileDirectionsQuestionnaire())
            .setRespondent1DQFurtherInformation(furtherInformation())
            .setRespondent1DQHearing(hearing())
            .setRespondent1DQHearingSupport(hearingSupport())
            .setRespondent1DQRequestedCourt(requestedCourt())
            .setRespondent1DQStatementOfTruth(statementOfTruth())
            .setRespondent1DQWitnesses(witnesses())
            .setRespondent1DQLanguage(welshLanguageRequirements())
            .setRespondent1DQVulnerabilityQuestions(vulnerabilityQuestions());
    }

    @Nested
    class GetExperts {

        @Test
        void shouldRemoveDetails_whenNoExpertsRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.copy()
                .setRespondent1DQExperts(dq.getExperts().copy()
                                             .setExpertRequired(NO));

            assertThat(dq.getExperts().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenExpertsRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.copy()
                .setRespondent1DQExperts(dq.getExperts().copy()
                                             .setExpertRequired(YES));

            assertThat(dq.getExperts()).isEqualTo(experts());
        }

        @Test
        void shouldReturnNull_whenExpertsIsNull() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.copy()
                .setRespondent1DQExperts(null);

            assertThat(dq.getExperts()).isNull();
        }
    }

    @Nested
    class GetWitnesses {

        @Test
        void shouldRemoveDetails_whenNoWitnessesToAppear() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.copy()
                .setRespondent1DQWitnesses(dq.getWitnesses().copy()
                                               .setWitnessesToAppear(NO));

            assertThat(dq.getWitnesses().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenWitnessesToAppear() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.copy()
                .setRespondent1DQWitnesses(dq.getWitnesses().copy()
                                               .setWitnessesToAppear(YES));

            assertThat(dq.getWitnesses()).isEqualTo(witnesses());
        }

        @Test
        void shouldReturnNull_whenWitnessesIsNull() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.copy()
                .setRespondent1DQWitnesses(null);

            assertThat(dq.getWitnesses()).isNull();
        }
    }

    @Nested
    class GetHearing {

        @Test
        void shouldRemoveUnavailableDates_whenNoUnavailableDatesRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.copy()
                .setRespondent1DQHearing(dq.getHearing().copy()
                                             .setUnavailableDatesRequired(NO));

            assertThat(dq.getHearing().getUnavailableDates()).isNull();
        }

        @Test
        void shouldNotRemoveUnavailableDates_whenUnavailableDatesRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.copy()
                .setRespondent1DQHearing(dq.getHearing().copy()
                                             .setUnavailableDatesRequired(YES));

            assertThat(dq.getHearing().getUnavailableDates()).isEqualTo(hearing().getUnavailableDates());
        }

        @Test
        void shouldReturnNull_whenUnavailableDatesIsNull() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.copy()
                .setRespondent1DQHearing(null);

            assertThat(dq.getHearing()).isNull();
        }

        @Test
        void shouldReturnFastClaimHearing_whenHearingNull() {
            HearingLength length = HearingLength.MORE_THAN_DAY;
            String lengthDays = "2";
            String lengthHours = "6";
            YesOrNo hasUnavailableDates = YES;
            List<Element<UnavailableDate>> lrDates = Stream.of(
                new UnavailableDate()
                    .setDate(LocalDate.of(2020, 5, 2))
                    .setWho("who 1"),
                new UnavailableDate()
                    .setFromDate(LocalDate.of(2020, 5, 2))
                    .setToDate(LocalDate.of(2020, 6, 2))
                    .setWho("who 2")).map(ElementUtils::element).toList();

            Hearing hearing = buildRespondent1Dq().copy()
                .setRespondent1DQHearing(null)
                .setRespondent1DQHearingFastClaim(new Hearing()
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
                new UnavailableDate()
                    .setDate(LocalDate.of(2020, 5, 2))
                    .setWho("who 1"),
                new UnavailableDate()
                    .setFromDate(LocalDate.of(2020, 5, 2))
                    .setToDate(LocalDate.of(2020, 6, 2))
                    .setWho("who 2")).map(ElementUtils::element).toList();

            Hearing hearing = buildRespondent1Dq().copy()
                .setRespondent1DQHearing(null)
                .setRespondent1DQHearingSmallClaim(new SmallClaimHearing()
                                                       .setUnavailableDatesRequired(hasUnavailableDates)
                                                       .setSmallClaimUnavailableDate(lrDates))
                .getHearing();

            assertThat(hearing.getUnavailableDatesRequired()).isEqualTo(hasUnavailableDates);
            for (int i = 0; i < hearing.getUnavailableDates().size(); i++) {
                UnavailableDate expected = lrDates.get(i).getValue();
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
        void build_whenRespondToCourtLocation() {
            String reason = "reason";
            String courtCode = "123";
            RequestedCourt court = buildRespondent1Dq().copy()
                .setRespondent1DQRequestedCourt(null)
                .setRespondToCourtLocation(new RequestedCourt()
                                               .setResponseCourtCode(courtCode)
                                               .setReasonForHearingAtSpecificCourt(reason))
                .getRequestedCourt();

            assertThat(court.getResponseCourtCode()).isEqualTo(courtCode);
            assertThat(court.getReasonForHearingAtSpecificCourt()).isEqualTo(reason);
        }

    }
}
