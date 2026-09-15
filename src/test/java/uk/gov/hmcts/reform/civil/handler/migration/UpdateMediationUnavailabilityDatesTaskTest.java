package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.UnavailableDatesCaseReference;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiPCarm;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateMediationUnavailabilityDatesTaskTest {

    private static final String CASE_REFERENCE = "1234567890123456";
    private final UpdateMediationUnavailabilityDatesTask task =
        new UpdateMediationUnavailabilityDatesTask();

    @Test
    void shouldUpdateNextMissingDefendantDateAndPreserveElementDetails() {
        UUID existingId = UUID.randomUUID();
        UnavailableDate alreadyPopulated = unavailableDate(
            "defendant",
            UnavailableDateType.SINGLE_DATE,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 1)
        );
        UnavailableDate missingDates = unavailableDate(
            "defendant", UnavailableDateType.DATE_RANGE, null, null, null
        );
        CaseData caseData = caseDataWithResponses(
            List.of(new Element<>(UUID.randomUUID(), alreadyPopulated), new Element<>(existingId, missingDates)),
            null
        );
        UnavailableDatesCaseReference reference = reference(
            "Defendant", "date_Range", LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 14)
        );

        CaseData result = task.migrateCaseData(caseData, reference);

        List<Element<UnavailableDate>> dates = result.getCaseDataLiP()
            .getRespondent1MediationLiPResponseCarm().getUnavailableDatesForMediation();
        assertThat(dates.get(1).getId()).isEqualTo(existingId);
        assertThat(dates.get(1).getValue().getUnavailableDateType())
            .isEqualTo(UnavailableDateType.DATE_RANGE);
        assertThat(dates.get(1).getValue().getDate()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(dates.get(1).getValue().getFromDate()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(dates.get(1).getValue().getToDate()).isEqualTo(LocalDate.of(2026, 9, 14));
        assertThat(dates.get(0).getValue()).isEqualTo(alreadyPopulated);
    }

    @Test
    void shouldUpdateClaimantResponseWhenPartyTypeIsClaimant() {
        UnavailableDate claimantDate = unavailableDate(
            "claimant", UnavailableDateType.SINGLE_DATE, null, null, null
        );
        CaseData caseData = caseDataWithResponses(
            null,
            List.of(new Element<>(UUID.randomUUID(), claimantDate))
        );
        UnavailableDatesCaseReference reference = reference(
            "claimant", "single date", LocalDate.of(2026, 10, 5), null
        );

        task.migrateCaseData(caseData, reference);

        assertThat(claimantDate.getDate()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(claimantDate.getFromDate()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(claimantDate.getToDate()).isNull();
    }

    @Test
    void shouldUseConsecutiveRowsToUpdateConsecutiveMissingDates() {
        UnavailableDate first = unavailableDate(
            "defendant", UnavailableDateType.SINGLE_DATE, null, null, null
        );
        UnavailableDate second = unavailableDate(
            "defendant", UnavailableDateType.SINGLE_DATE, null, null, null
        );
        CaseData caseData = caseDataWithResponses(
            List.of(new Element<>(UUID.randomUUID(), first), new Element<>(UUID.randomUUID(), second)),
            null
        );

        task.migrateCaseData(caseData, reference(
            "defendant", "SINGLE_DATE", LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 14)
        ));
        task.migrateCaseData(caseData, reference(
            "defendant", "SINGLE_DATE", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 5)
        ));

        assertThat(first.getDate()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(first.getToDate()).isEqualTo(LocalDate.of(2026, 9, 14));
        assertThat(second.getDate()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(second.getToDate()).isEqualTo(LocalDate.of(2026, 10, 5));
    }

    @Test
    void shouldRejectInvalidPartyType() {
        CaseData caseData = CaseData.builder().build();
        UnavailableDatesCaseReference reference = reference(
            "respondent", "DATE_RANGE", LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 14)
        );

        assertThatThrownBy(() -> task.migrateCaseData(caseData, reference))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Party type must be claimant or defendant");
    }

    @Test
    void shouldFailWhenNoMatchingDateRequiresUpdate() {
        UnavailableDate populated = unavailableDate(
            "defendant",
            UnavailableDateType.SINGLE_DATE,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 1)
        );
        CaseData caseData = caseDataWithResponses(
            List.of(new Element<>(UUID.randomUUID(), populated)), null
        );
        UnavailableDatesCaseReference reference = reference(
            "defendant",
            "SINGLE_DATE",
            LocalDate.of(2026, 9, 7),
            LocalDate.of(2026, 9, 14)
        );

        assertThatThrownBy(() -> task.migrateCaseData(caseData, reference))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No SINGLE_DATE unavailable date requires an update");
    }

    private CaseData caseDataWithResponses(
        List<Element<UnavailableDate>> defendantDates,
        List<Element<UnavailableDate>> claimantDates
    ) {
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        if (defendantDates != null) {
            caseDataLiP.setRespondent1MediationLiPResponseCarm(
                new MediationLiPCarm().setUnavailableDatesForMediation(defendantDates)
            );
        }
        if (claimantDates != null) {
            caseDataLiP.setApplicant1LiPResponseCarm(
                new MediationLiPCarm().setUnavailableDatesForMediation(claimantDates)
            );
        }
        return CaseData.builder().caseDataLiP(caseDataLiP).build();
    }

    private UnavailableDatesCaseReference reference(
        String partyType,
        String unavailableDateType,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        UnavailableDatesCaseReference reference =
            new UnavailableDatesCaseReference()
            .setPartyType(partyType)
            .setUnavailableDateType(unavailableDateType)
            .setFromDate(fromDate)
            .setToDate(toDate);
        reference.setCaseReference(CASE_REFERENCE);
        return reference;
    }

    private UnavailableDate unavailableDate(
        String who,
        UnavailableDateType type,
        LocalDate date,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        return new UnavailableDate()
            .setWho(who)
            .setUnavailableDateType(type)
            .setDate(date)
            .setFromDate(fromDate)
            .setToDate(toDate);
    }
}
