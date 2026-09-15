package uk.gov.hmcts.reform.civil.handler.migration;

import uk.gov.hmcts.reform.civil.bulkupdate.csv.UnavailableDatesCaseReference;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

abstract class UpdateUnavailableDatesTask extends MigrationTaskWithMetadata<UnavailableDatesCaseReference> {

    private static final String CLAIMANT = "claimant";
    private static final String DEFENDANT = "defendant";

    protected UpdateUnavailableDatesTask(
        String taskName,
        String eventSummary,
        String eventDescription
    ) {
        super(UnavailableDatesCaseReference.class, taskName, eventSummary, eventDescription);
    }

    @Override
    protected final CaseData migrateCaseData(CaseData caseData, UnavailableDatesCaseReference caseReference) {
        validate(caseData, caseReference);

        String partyType = caseReference.getPartyType().trim().toLowerCase(Locale.ENGLISH);
        UnavailableDateType unavailableDateType = parseUnavailableDateType(caseReference.getUnavailableDateType());
        updateNextMissingDate(
            getUnavailableDates(caseData, partyType),
            caseReference,
            unavailableDateType
        );

        return caseData;
    }

    protected abstract List<Element<UnavailableDate>> getUnavailableDates(CaseData caseData, String partyType);

    protected final boolean isDefendant(String partyType) {
        return DEFENDANT.equals(partyType);
    }

    protected final List<Element<UnavailableDate>> requireUnavailableDates(
        List<Element<UnavailableDate>> unavailableDates,
        String fieldDescription
    ) {
        if (unavailableDates == null || unavailableDates.isEmpty()) {
            throw new IllegalStateException(fieldDescription + " must not be null or empty");
        }
        return unavailableDates;
    }

    private void validate(CaseData caseData, UnavailableDatesCaseReference caseReference) {
        if (caseData == null) {
            throw new IllegalArgumentException("CaseData must not be null");
        }
        if (caseReference == null || caseReference.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseReference fields must not be null");
        }
        if (caseReference.getPartyType() == null
            || caseReference.getUnavailableDateType() == null
            || caseReference.getFromDate() == null) {
            throw new IllegalArgumentException(
                "Party type, unavailable date type and from date must not be null"
            );
        }
        String partyType = caseReference.getPartyType().trim().toLowerCase(Locale.ENGLISH);
        if (!CLAIMANT.equals(partyType) && !DEFENDANT.equals(partyType)) {
            throw new IllegalArgumentException("Party type must be claimant or defendant");
        }
        parseUnavailableDateType(caseReference.getUnavailableDateType());
    }

    private void updateNextMissingDate(
        List<Element<UnavailableDate>> unavailableDates,
        UnavailableDatesCaseReference caseReference,
        UnavailableDateType unavailableDateType
    ) {
        UnavailableDate unavailableDate = unavailableDates.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .filter(value -> unavailableDateType == value.getUnavailableDateType())
            .filter(value -> value.getDate() == null)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "No " + unavailableDateType + " unavailable date requires an update"
            ));

        unavailableDate.setDate(caseReference.getFromDate());
        unavailableDate.setFromDate(caseReference.getFromDate());
        if (caseReference.getToDate() != null) {
            unavailableDate.setToDate(caseReference.getToDate());
        }
    }

    private UnavailableDateType parseUnavailableDateType(String value) {
        try {
            return UnavailableDateType.valueOf(
                value.trim().replace(' ', '_').replace('-', '_').toUpperCase(Locale.ENGLISH)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                "Unavailable date type must be DATE_RANGE or SINGLE_DATE",
                exception
            );
        }
    }
}
