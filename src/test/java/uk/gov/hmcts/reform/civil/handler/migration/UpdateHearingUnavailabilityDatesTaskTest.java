package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.UnavailableDatesCaseReference;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateHearingUnavailabilityDatesTaskTest {

    private static final String CASE_REFERENCE = "1234567890123456";
    private final UpdateHearingUnavailabilityDatesTask task =
        new UpdateHearingUnavailabilityDatesTask();

    @Test
    void shouldUpdateRespondentSmallClaimHearingDate() {
        UUID elementId = UUID.randomUUID();
        UnavailableDate missingDate = unavailableDate("defendant", UnavailableDateType.DATE_RANGE);
        UnavailableDate hearingMissingDate = unavailableDate("defendant", UnavailableDateType.DATE_RANGE);
        UnavailableDate tabMissingDate = unavailableDate("defendant", UnavailableDateType.DATE_RANGE);
        CaseData caseData = CaseData.builder()
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQHearingSmallClaim(
                                   smallClaimHearing(elementId, List.of(missingDate))
                               )
                               .setRespondent1DQHearing(hearing(List.of(hearingMissingDate))))
            .respondent1UnavailableDatesForTab(elements(List.of(tabMissingDate)))
            .build();

        task.migrateCaseData(caseData, reference(
            "defendant", "DATE_RANGE", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 18)
        ));

        Element<UnavailableDate> updated = caseData.getRespondent1DQ()
            .getRespondent1DQHearingSmallClaim().getSmallClaimUnavailableDate().getFirst();
        assertThat(updated.getId()).isEqualTo(elementId);
        assertThat(updated.getValue().getUnavailableDateType()).isEqualTo(UnavailableDateType.DATE_RANGE);
        assertThat(updated.getValue().getDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(updated.getValue().getFromDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(updated.getValue().getToDate()).isEqualTo(LocalDate.of(2026, 10, 18));
        assertUpdated(hearingMissingDate, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 18));
        assertUpdated(tabMissingDate, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 18));
    }

    @Test
    void shouldUpdateApplicantSmallClaimHearingBasedOnPartyTypeAndDateType() {
        UnavailableDate dateRange = unavailableDate("defendant", UnavailableDateType.DATE_RANGE);
        UnavailableDate singleDate = unavailableDate("defendant", UnavailableDateType.SINGLE_DATE);
        UnavailableDate hearingSingleDate = unavailableDate("claimant", UnavailableDateType.SINGLE_DATE);
        UnavailableDate tabSingleDate = unavailableDate("claimant", UnavailableDateType.SINGLE_DATE);
        CaseData caseData = CaseData.builder()
            .applicant1DQ(new Applicant1DQ()
                              .setApplicant1DQSmallClaimHearing(
                                  smallClaimHearing(UUID.randomUUID(), List.of(dateRange, singleDate))
                              )
                              .setApplicant1DQHearing(hearing(List.of(hearingSingleDate))))
            .applicant1UnavailableDatesForTab(elements(List.of(tabSingleDate)))
            .build();

        task.migrateCaseData(caseData, reference(
            "claimant", "SINGLE_DATE", LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2)
        ));

        assertThat(dateRange.getDate()).isNull();
        assertThat(singleDate.getDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(singleDate.getFromDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(singleDate.getToDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertUpdated(hearingSingleDate, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2));
        assertUpdated(tabSingleDate, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2));
    }

    @Test
    void shouldUseConsecutiveRowsToUpdateConsecutiveChildrenOfSameType() {
        UnavailableDate first = unavailableDate("defendant", UnavailableDateType.SINGLE_DATE);
        UnavailableDate second = unavailableDate("defendant", UnavailableDateType.SINGLE_DATE);
        CaseData caseData = CaseData.builder()
            .applicant1DQ(new Applicant1DQ().setApplicant1DQSmallClaimHearing(
                smallClaimHearing(UUID.randomUUID(), List.of(first, second))
            ))
            .build();

        task.migrateCaseData(caseData, reference(
            "claimant", "SINGLE_DATE", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 5)
        ));
        task.migrateCaseData(caseData, reference(
            "claimant", "SINGLE_DATE", LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 12)
        ));

        assertThat(first.getDate()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(second.getDate()).isEqualTo(LocalDate.of(2026, 10, 12));
    }

    @Test
    void shouldNotFailWhenHearingDatesAreAbsent() {
        CaseData caseData = CaseData.builder().build();
        UnavailableDatesCaseReference reference = reference(
            "defendant",
            "SINGLE_DATE",
            LocalDate.of(2026, 10, 5),
            LocalDate.of(2026, 10, 5)
        );

        CaseData result = task.migrateCaseData(caseData, reference);

        assertThat(result).isSameAs(caseData);
    }

    private SmallClaimHearing smallClaimHearing(
        UUID firstElementId,
        List<UnavailableDate> unavailableDates
    ) {
        List<Element<UnavailableDate>> elements = elements(unavailableDates);
        elements.getFirst().setId(firstElementId);
        return new SmallClaimHearing().setSmallClaimUnavailableDate(elements);
    }

    private Hearing hearing(List<UnavailableDate> unavailableDates) {
        return new Hearing().setUnavailableDates(elements(unavailableDates));
    }

    private List<Element<UnavailableDate>> elements(List<UnavailableDate> unavailableDates) {
        return unavailableDates.stream()
            .map(date -> new Element<>(UUID.randomUUID(), date))
            .toList();
    }

    private void assertUpdated(UnavailableDate unavailableDate, LocalDate fromDate, LocalDate toDate) {
        assertThat(unavailableDate.getDate()).isEqualTo(fromDate);
        assertThat(unavailableDate.getFromDate()).isEqualTo(fromDate);
        assertThat(unavailableDate.getToDate()).isEqualTo(toDate);
    }

    private UnavailableDate unavailableDate(String who, UnavailableDateType unavailableDateType) {
        return new UnavailableDate()
            .setWho(who)
            .setUnavailableDateType(unavailableDateType);
    }

    private UnavailableDatesCaseReference reference(
        String partyType,
        String unavailableDateType,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        UnavailableDatesCaseReference reference = new UnavailableDatesCaseReference()
            .setPartyType(partyType)
            .setUnavailableDateType(unavailableDateType)
            .setFromDate(fromDate)
            .setToDate(toDate);
        reference.setCaseReference(CASE_REFERENCE);
        return reference;
    }
}
