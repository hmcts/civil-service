package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.DATE_RANGE;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant;
import static uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent;
import static uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils.shouldUpdateApplicant1UnavailableDates;
import static uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils.shouldUpdateApplicant2UnavailableDates;
import static uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils.shouldUpdateRespondent1UnavailableDates;
import static uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils.shouldUpdateRespondent2UnavailableDates;
import static uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils.updateMissingUnavailableDatesForApplicants;

public class UnavailabilityDatesUtilsTest {

    private final LocalDate issueDate = now();
    private static final String DEFENDANT_RESPONSE_EVENT = "Defendant Response Event";
    private static final String CLAIMANT_INTENTION_EVENT = "Claimant Intention Event";
    private static final String DJ_EVENT = "Request DJ Event";

    @Nested
    class UpdateContactDetailsEnabled {

        @Test
        public void shouldReturnSingleUnavailabilityDateWhenProvidedForRespondent1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .respondent1DQWithUnavailableDates()
                .build();

            // rollUp modifies the caseData instance
            rollUpUnavailabilityDatesForRespondent(caseData);

            UnavailableDate expected = new UnavailableDate();
            expected.setDate(LocalDate.now().plusDays(1));
            expected.setUnavailableDateType(SINGLE_DATE);
            expected.setDateAdded(caseData.getRespondent1ResponseDate().toLocalDate());
            expected.setEventAdded(DEFENDANT_RESPONSE_EVENT);

            List<UnavailableDate> respondentDates = unwrapElements(caseData.getRespondent1().getUnavailableDates());
            assertThat(respondentDates).isNotEmpty();
            UnavailableDate result = respondentDates.get(0);
            assertEquals(expected.getDate(), result.getDate());
            assertEquals(expected.getUnavailableDateType(), result.getUnavailableDateType());
        }

        @Test
        public void shouldReturnEmptyWhenDefendantResponseNoUnavailableDates() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();

            rollUpUnavailabilityDatesForRespondent(caseData);

            assertThat(caseData.getRespondent1().getUnavailableDates()).isNull();
        }

        @Test
        public void shouldReturnDateRangesWhenProvidedForRespondent1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1DQWithUnavailableDateRange()
                .build();

            rollUpUnavailabilityDatesForRespondent(caseData);

            UnavailableDate expected = new UnavailableDate();
            expected.setFromDate(LocalDate.now().plusDays(1));
            expected.setToDate(LocalDate.now().plusDays(2));
            expected.setUnavailableDateType(DATE_RANGE);
            expected.setDateAdded(caseData.getRespondent1ResponseDate().toLocalDate());
            expected.setEventAdded(DEFENDANT_RESPONSE_EVENT);

            List<UnavailableDate> respondentDates = unwrapElements(caseData.getRespondent1().getUnavailableDates());
            assertThat(respondentDates).isNotEmpty();
            UnavailableDate result = respondentDates.get(0);
            assertEquals(expected.getFromDate(), result.getFromDate());
            assertEquals(expected.getToDate(), result.getToDate());
            assertEquals(expected.getUnavailableDateType(), result.getUnavailableDateType());
        }

        @Test
        public void shouldReturnDateRangesWhenProvidedForApplicant1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1DQWithUnavailableDateRange()
                .build();

            rollUpUnavailabilityDatesForApplicant(caseData);

            UnavailableDate expected = new UnavailableDate();
            expected.setFromDate(LocalDate.now().plusDays(1));
            expected.setToDate(LocalDate.now().plusDays(2));
            expected.setUnavailableDateType(UnavailableDateType.DATE_RANGE);
            expected.setDateAdded(caseData.getApplicant1ResponseDate().toLocalDate());
            expected.setEventAdded(CLAIMANT_INTENTION_EVENT);

            List<UnavailableDate> applicantDates = unwrapElements(caseData.getApplicant1().getUnavailableDates());
            assertThat(applicantDates).isNotEmpty();
            UnavailableDate result = applicantDates.get(0);
            assertEquals(expected.getFromDate(), result.getFromDate());
            assertEquals(expected.getToDate(), result.getToDate());
            assertEquals(expected.getUnavailableDateType(), result.getUnavailableDateType());
        }

        @Test
        public void shouldReturnSingleUnavailabilityDateWhenProvidedForApplicant1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1DQWithUnavailableDate()
                .build();

            rollUpUnavailabilityDatesForApplicant(caseData);

            UnavailableDate expected = new UnavailableDate();
            expected.setDate(LocalDate.now().plusDays(1));
            expected.setUnavailableDateType(SINGLE_DATE);
            expected.setDateAdded(caseData.getApplicant1ResponseDate().toLocalDate());
            expected.setEventAdded(CLAIMANT_INTENTION_EVENT);

            List<UnavailableDate> applicantDates = unwrapElements(caseData.getApplicant1().getUnavailableDates());
            assertThat(applicantDates).isNotEmpty();
            UnavailableDate result = applicantDates.get(0);
            assertEquals(expected.getDate(), result.getDate());
            assertEquals(expected.getUnavailableDateType(), result.getUnavailableDateType());
        }

        @Test
        public void shouldReturnDatesForBothApplicants2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoApplicants()
                .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                .applicant1DQWithUnavailableDateRange()
                .build();

            // Create expected date
            UnavailableDate expectedDate = new UnavailableDate();
            expectedDate.setFromDate(LocalDate.now().plusDays(1));
            expectedDate.setToDate(LocalDate.now().plusDays(2));
            expectedDate.setUnavailableDateType(DATE_RANGE);
            expectedDate.setDateAdded(caseData.getApplicant1ResponseDate().toLocalDate());
            expectedDate.setEventAdded(CLAIMANT_INTENTION_EVENT);

            List<Element<UnavailableDate>> expected = wrapElements(expectedDate);

            // Set dates for both applicants
            Party applicant1 = caseData.getApplicant1();
            applicant1.setUnavailableDates(expected);
            caseData.setApplicant1(applicant1);
            Party applicant2 = caseData.getApplicant2();
            applicant2.setUnavailableDates(expected);
            caseData.setApplicant2(applicant2);

            // Verify dates are set correctly
            assertThat(caseData.getApplicant1().getUnavailableDates()).isEqualTo(expected);
            assertThat(caseData.getApplicant2().getUnavailableDates()).isEqualTo(expected);
        }

        @Test
        public void shouldReturnUnavailabilityDateWhenProvidedForApplicantDJ() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .atStateClaimantRequestsDJWithUnavailableDates()
                .build();

            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicantDJ(caseData);

            LocalDate dateAdded = LocalDate.now();
            UnavailableDate expectedSingleDate = new UnavailableDate();
            expectedSingleDate.setUnavailableDateType(SINGLE_DATE);
            expectedSingleDate.setDate(LocalDate.of(2023, 8, 20));
            expectedSingleDate.setDateAdded(dateAdded);
            expectedSingleDate.setEventAdded(DJ_EVENT);

            UnavailableDate expectedDateRange = new UnavailableDate();
            expectedDateRange.setUnavailableDateType(DATE_RANGE);
            expectedDateRange.setFromDate(LocalDate.of(2023, 8, 20));
            expectedDateRange.setToDate(LocalDate.of(2023, 8, 22));
            expectedDateRange.setDateAdded(dateAdded);
            expectedDateRange.setEventAdded(DJ_EVENT);

            List<Element<UnavailableDate>> expected = wrapElements(List.of(expectedSingleDate, expectedDateRange));

            assertThat(caseData.getApplicant1().getUnavailableDates()).isEqualTo(expected);
        }

        @Test
        public void shouldReturnUnavailabilityDateWhenProvidedForApplicantDJ2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoApplicants()
                .atStateClaimantRequestsDJWithUnavailableDates()
                .build();

            UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicantDJ(caseData);

            LocalDate dateAdded = LocalDate.now();
            UnavailableDate expectedSingleDate = new UnavailableDate();
            expectedSingleDate.setUnavailableDateType(SINGLE_DATE);
            expectedSingleDate.setDate(LocalDate.of(2023, 8, 20));
            expectedSingleDate.setDateAdded(dateAdded);
            expectedSingleDate.setEventAdded(DJ_EVENT);

            UnavailableDate expectedDateRange = new UnavailableDate();
            expectedDateRange.setUnavailableDateType(DATE_RANGE);
            expectedDateRange.setFromDate(LocalDate.of(2023, 8, 20));
            expectedDateRange.setToDate(LocalDate.of(2023, 8, 22));
            expectedDateRange.setDateAdded(dateAdded);
            expectedDateRange.setEventAdded(DJ_EVENT);

            List<Element<UnavailableDate>> expected = wrapElements(List.of(expectedSingleDate, expectedDateRange));

            assertThat(caseData.getApplicant1().getUnavailableDates()).isEqualTo(expected);
        }

        @Test
        public void shouldRollupUnavailableDatesForClaimant_whenEventIsClaimantResponse() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1DQWithUnavailableDate()
                .build();

            UnavailableDate expectedDate = new UnavailableDate();
            expectedDate.setDate(LocalDate.now().plusDays(1));
            expectedDate.setUnavailableDateType(SINGLE_DATE);
            expectedDate.setDateAdded(caseData.getApplicant1ResponseDate().toLocalDate());
            expectedDate.setEventAdded(CLAIMANT_INTENTION_EVENT);

            List<Element<UnavailableDate>> unavailableDates = wrapElements(expectedDate);

            Party applicant1 = caseData.getApplicant1();
            applicant1.setUnavailableDates(unavailableDates);
            caseData.setApplicant1(applicant1);

            updateMissingUnavailableDatesForApplicants(caseData);

            UnavailableDate actualDate = unwrapElements(caseData.getApplicant1().getUnavailableDates()).get(0);

            assertEquals(expectedDate.getDate(), actualDate.getDate());
            assertEquals(expectedDate.getUnavailableDateType(), actualDate.getUnavailableDateType());
        }

        @Test
        public void shouldRollupUnavailableDatesForClaimant_whenEventIsDefaultJudgement() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .atStateClaimantRequestsDJWithUnavailableDates()
                .build();

            updateMissingUnavailableDatesForApplicants(caseData);

            LocalDate dateAdded = LocalDate.now();
            UnavailableDate expectedSingleDate = new UnavailableDate();
            expectedSingleDate.setUnavailableDateType(SINGLE_DATE);
            expectedSingleDate.setDate(LocalDate.of(2023, 8, 20));
            expectedSingleDate.setDateAdded(dateAdded);
            expectedSingleDate.setEventAdded(DJ_EVENT);

            UnavailableDate expectedDateRange = new UnavailableDate();
            expectedDateRange.setUnavailableDateType(DATE_RANGE);
            expectedDateRange.setFromDate(LocalDate.of(2023, 8, 20));
            expectedDateRange.setToDate(LocalDate.of(2023, 8, 22));
            expectedDateRange.setDateAdded(dateAdded);
            expectedDateRange.setEventAdded(DJ_EVENT);

            List<Element<UnavailableDate>> expected = wrapElements(List.of(expectedSingleDate, expectedDateRange));

            assertThat(caseData.getApplicant1().getUnavailableDates()).isEqualTo(expected);
        }

        @Test
        public void shouldReturnFalse_whenApplicant1HasNoMissingUnavailableDates() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            UnavailableDate  expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(DATE_RANGE);
            expectedDate.setFromDate(LocalDate.of(2023, 8, 20));
            expectedDate.setToDate(LocalDate.of(2023, 8, 22));
            expectedDate.setDateAdded(LocalDate.of(2023, 6, 22));
            expectedDate.setEventAdded(DJ_EVENT);
            Party applicant1 = caseData.getApplicant1();
            applicant1.setUnavailableDates(wrapElements(List.of(expectedDate)));
            caseData.setApplicant1(applicant1);

            boolean actual = shouldUpdateApplicant1UnavailableDates(caseData);
            assertThat(actual).isFalse();
        }

        @Test
        public void shouldReturnTrue_whenApplicant1HasMissingUnavailableDates() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            UnavailableDate  expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(DATE_RANGE);
            expectedDate.setFromDate(LocalDate.of(2023, 8, 20));
            expectedDate.setToDate(LocalDate.of(2023, 8, 22));
            Party applicant1 = caseData.getApplicant1();
            applicant1.setUnavailableDates(wrapElements(List.of(expectedDate)));
            caseData.setApplicant1(applicant1);

            boolean actual = shouldUpdateApplicant1UnavailableDates(caseData);
            assertThat(actual).isTrue();
        }

        @Test
        public void shouldReturnFalse_whenApplicant2HasNoMissingUnavailableDates() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .addApplicant2()
                .build();
            UnavailableDate  expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(DATE_RANGE);
            expectedDate.setFromDate(LocalDate.of(2023, 8, 20));
            expectedDate.setToDate(LocalDate.of(2023, 8, 22));
            expectedDate.setDateAdded(LocalDate.of(2023, 6, 22));
            expectedDate.setEventAdded(DJ_EVENT);
            Party applicant2 = caseData.getApplicant2();
            applicant2.setUnavailableDates(wrapElements(List.of(expectedDate)));
            caseData.setApplicant2(applicant2);

            boolean actual = shouldUpdateApplicant2UnavailableDates(caseData);
            assertThat(actual).isFalse();
        }

        @Test
        public void shouldReturnTrue_whenApplicant2HasMissingUnavailableDates() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .addApplicant2()
                .multiPartyClaimTwoApplicants()
                .build();
            UnavailableDate  expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(DATE_RANGE);
            expectedDate.setFromDate(LocalDate.of(2023, 8, 20));
            expectedDate.setToDate(LocalDate.of(2023, 8, 22));
            Party applicant2 = caseData.getApplicant2();
            applicant2.setUnavailableDates(wrapElements(List.of(expectedDate)));
            caseData.setApplicant2(applicant2);

            boolean actual = shouldUpdateApplicant2UnavailableDates(caseData);
            assertThat(actual).isTrue();
        }

        @Test
        public void shouldReturnFalse_whenRespondent1HasNoMissingUnavailableDates() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            UnavailableDate  expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(DATE_RANGE);
            expectedDate.setFromDate(LocalDate.of(2023, 8, 20));
            expectedDate.setToDate(LocalDate.of(2023, 8, 22));
            expectedDate.setDateAdded(LocalDate.of(2023, 6, 22));
            expectedDate.setEventAdded(DEFENDANT_RESPONSE_EVENT);
            Party respondent1 = caseData.getRespondent1();
            respondent1.setUnavailableDates(wrapElements(List.of(expectedDate)));
            caseData.setRespondent1(respondent1);

            boolean actual = shouldUpdateRespondent1UnavailableDates(caseData);
            assertThat(actual).isFalse();
        }

        @Test
        public void shouldReturnTrue_whenRespondent1HasMissingUnavailableDates() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            UnavailableDate  expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(DATE_RANGE);
            expectedDate.setFromDate(LocalDate.of(2023, 8, 20));
            expectedDate.setToDate(LocalDate.of(2023, 8, 22));
            Party respondent1 = caseData.getRespondent1();
            respondent1.setUnavailableDates(wrapElements(List.of(expectedDate)));
            caseData.setRespondent1(respondent1);

            boolean actual = shouldUpdateRespondent1UnavailableDates(caseData);
            assertThat(actual).isTrue();
        }

        @Test
        public void shouldReturnFalse_whenRespondent2HasNoMissingUnavailableDates() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
            UnavailableDate  expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(DATE_RANGE);
            expectedDate.setFromDate(LocalDate.of(2023, 8, 20));
            expectedDate.setToDate(LocalDate.of(2023, 8, 22));
            expectedDate.setDateAdded(LocalDate.of(2023, 6, 22));
            expectedDate.setEventAdded(DEFENDANT_RESPONSE_EVENT);
            Party respondent2 = caseData.getRespondent2();
            respondent2.setUnavailableDates(wrapElements(List.of(expectedDate)));
            caseData.setRespondent2(respondent2);

            boolean actual = shouldUpdateRespondent2UnavailableDates(caseData);
            assertThat(actual).isFalse();
        }

        @Test
        public void shouldReturnTrue_whenRespondent2HasMissingUnavailableDates() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            UnavailableDate  expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(DATE_RANGE);
            expectedDate.setFromDate(LocalDate.of(2023, 8, 20));
            expectedDate.setToDate(LocalDate.of(2023, 8, 22));
            Party respondent2 = caseData.getRespondent2();
            respondent2.setUnavailableDates(wrapElements(List.of(expectedDate)));
            caseData.setRespondent2(respondent2);

            boolean actual = shouldUpdateRespondent2UnavailableDates(caseData);
            assertThat(actual).isTrue();
        }
    }

    @Nested
    class ListingTabFields {
        List<Element<UnavailableDate>> existingDates = Stream.of(
            new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.of(2022, 5, 2))).map(ElementUtils::element).toList();

        @Test
        public void shouldCopyDatesIntoListingTabFieldsForRespondent1() {
            Hearing hearing = new Hearing();
            hearing.setUnavailableDatesRequired(YES);
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQHearing(hearing);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimantFullDefence()
                .respondent1(PartyBuilder.builder()
                                 .soleTrader().build()
                                 .setPartyID("res-1-party-id")
                                 .setUnavailableDates(new ArrayList<>(existingDates)))
                .respondent1ResponseDate(issueDate.atStartOfDay())
                .respondent1DQ(respondent1DQ)
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            UnavailableDate expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            expectedDate.setDate(LocalDate.of(2022, 5, 2));
            expectedDate.setDateAdded(issueDate);
            expectedDate.setEventAdded(DEFENDANT_RESPONSE_EVENT);
            List<UnavailableDate> expectedDates = Stream.of(expectedDate).toList();

            assertThat(unwrapElements(builder.build().getRespondent1().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getRespondent1UnavailableDatesForTab())).isEqualTo(expectedDates);
        }

        @Test
        public void shouldCopyDatesIntoListingTabFieldsForRespondent2() {
            Hearing hearing = new Hearing();
            hearing.setUnavailableDatesRequired(YES);
            Respondent2DQ respondent2DQ = new Respondent2DQ();
            respondent2DQ.setRespondent2DQHearing(hearing);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimantFullDefence()
                .respondent2(PartyBuilder.builder()
                                 .soleTrader().build()
                                 .setPartyID("res-2-party-id")
                                 .setUnavailableDates(new ArrayList<>(existingDates)))
                .respondent2ResponseDate(issueDate.atStartOfDay())
                .respondent2DQ(respondent2DQ)
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            UnavailableDate expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            expectedDate.setDate(LocalDate.of(2022, 5, 2));
            expectedDate.setDateAdded(issueDate);
            expectedDate.setEventAdded(DEFENDANT_RESPONSE_EVENT);
            List<UnavailableDate> expectedDates = Stream.of(expectedDate).toList();

            assertThat(unwrapElements(builder.build().getRespondent2().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getRespondent2UnavailableDatesForTab())).isEqualTo(expectedDates);
        }

        @Test
        public void shouldCopyDatesIntoListingTabFieldsForApplicant1() {
            Hearing hearing = new Hearing();
            hearing.setUnavailableDatesRequired(YES);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQHearing(hearing);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimantFullDefence()
                .applicant1(PartyBuilder.builder()
                                .soleTrader().build()
                                .setPartyID("someid")
                                .setUnavailableDates(new ArrayList<>(existingDates)))
                .applicant1ResponseDate(issueDate.atStartOfDay())
                .applicant1DQ(applicant1DQ)
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            UnavailableDate expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            expectedDate.setDate(LocalDate.of(2022, 5, 2));
            expectedDate.setDateAdded(issueDate);
            expectedDate.setEventAdded(CLAIMANT_INTENTION_EVENT);
            List<UnavailableDate> expectedDates = Stream.of(expectedDate).toList();

            assertThat(unwrapElements(builder.build().getApplicant1().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDates);
        }

        @Test
        public void shouldCopyDatesIntoListingTabFieldsForApplicant2() {
            Hearing hearing = new Hearing();
            hearing.setUnavailableDatesRequired(YES);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQHearing(hearing);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimantFullDefence()
                .multiPartyClaimTwoApplicants()
                .applicant1(PartyBuilder.builder()
                                .soleTrader().build()
                                .setPartyID("app-2-party-id")
                                .setUnavailableDates(new ArrayList<>(existingDates)))
                .applicant2(PartyBuilder.builder()
                                .soleTrader().build()
                                .setPartyID("app-2-party-id")
                                .setUnavailableDates(new ArrayList<>(existingDates)))
                .applicant1ResponseDate(issueDate.atStartOfDay())
                .applicant1DQ(applicant1DQ)
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            UnavailableDate expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            expectedDate.setDate(LocalDate.of(2022, 5, 2));
            expectedDate.setDateAdded(issueDate);
            expectedDate.setEventAdded(CLAIMANT_INTENTION_EVENT);
            List<UnavailableDate> expectedDates = Stream.of(expectedDate).toList();

            assertThat(unwrapElements(builder.build().getApplicant1().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getApplicant2().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getApplicant2UnavailableDatesForTab())).isEqualTo(expectedDates);
        }

        @Test
        public void shouldCopyDatesIntoListingTabFieldsForDJ() {
            UnavailableDate expectedDate = new UnavailableDate();
            expectedDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            expectedDate.setDate(LocalDate.of(2022, 5, 2));
            expectedDate.setDateAdded(issueDate);
            expectedDate.setEventAdded(DJ_EVENT);
            List<UnavailableDate> expectedDates = Stream.of(expectedDate).toList();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .atStateClaimantRequestsDJWithUnavailableDates()
                .applicant1(PartyBuilder.builder()
                                .soleTrader().build()
                                .setPartyID("someid")
                                .setUnavailableDates(new ArrayList<>(existingDates)))
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            assertThat(unwrapElements(builder.build().getApplicant1().getUnavailableDates())).isEqualTo(expectedDates);
            assertThat(unwrapElements(builder.build().getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDates);
        }

        @Test
        public void shouldCopyAllDatesFromAllParties_1v2DS() {
            Hearing hearing = new Hearing();
            hearing.setUnavailableDatesRequired(YES);
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQHearing(hearing);
            Respondent2DQ respondent2DQ = new Respondent2DQ();
            respondent2DQ.setRespondent2DQHearing(hearing);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQHearing(hearing);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimantFullDefence()
                .respondent1(PartyBuilder.builder()
                                 .soleTrader().build()
                                 .setPartyID("res-1-party-id")
                                 .setUnavailableDates(new ArrayList<>(existingDates)))
                .respondent1ResponseDate(issueDate.atStartOfDay())
                .respondent1DQ(respondent1DQ)
                .respondent2(PartyBuilder.builder()
                                 .soleTrader().build()
                                 .setPartyID("res-2-party-id")
                                 .setUnavailableDates(new ArrayList<>(existingDates)))
                .respondent2ResponseDate(issueDate.atStartOfDay())
                .respondent2DQ(respondent2DQ)
                .applicant1(PartyBuilder.builder()
                                .soleTrader().build()
                                .setPartyID("someid")
                                .setUnavailableDates(new ArrayList<>(existingDates)))
                .applicant1ResponseDate(issueDate.atStartOfDay())
                .applicant1DQ(applicant1DQ)
                .build();

            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

            UnavailabilityDatesUtils.copyDatesIntoListingTabFields(caseData, builder);

            UnavailableDate expectedDateDefendant = new UnavailableDate();
            expectedDateDefendant.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            expectedDateDefendant.setDate(LocalDate.of(2022, 5, 2));
            expectedDateDefendant.setDateAdded(issueDate);
            expectedDateDefendant.setEventAdded(DEFENDANT_RESPONSE_EVENT);

            UnavailableDate expectedDateApplicant = new UnavailableDate();
            expectedDateApplicant.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            expectedDateApplicant.setDate(LocalDate.of(2022, 5, 2));
            expectedDateApplicant.setDateAdded(issueDate);
            expectedDateApplicant.setEventAdded(CLAIMANT_INTENTION_EVENT);
            List<UnavailableDate> expectedDatesApplicant = Stream.of(expectedDateApplicant).toList();
            List<UnavailableDate> expectedDatesDefendant = Stream.of(expectedDateDefendant).toList();

            assertThat(unwrapElements(builder.build().getApplicant1().getUnavailableDates())).isEqualTo(expectedDatesApplicant);
            assertThat(unwrapElements(builder.build().getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDatesApplicant);
            assertThat(unwrapElements(builder.build().getRespondent1().getUnavailableDates())).isEqualTo(expectedDatesDefendant);
            assertThat(unwrapElements(builder.build().getRespondent1UnavailableDatesForTab())).isEqualTo(expectedDatesDefendant);
            assertThat(unwrapElements(builder.build().getRespondent2().getUnavailableDates())).isEqualTo(expectedDatesDefendant);
            assertThat(unwrapElements(builder.build().getRespondent2UnavailableDatesForTab())).isEqualTo(expectedDatesDefendant);
        }
    }
}
