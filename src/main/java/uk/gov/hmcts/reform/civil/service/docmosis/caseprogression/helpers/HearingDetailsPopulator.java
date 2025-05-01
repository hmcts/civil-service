package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm.JudgeFinalOrderFormBuilder;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.time.LocalDate;
import java.util.Objects;

import static java.util.Objects.nonNull;

@Component
public class HearingDetailsPopulator {

    private final LocationReferenceDataService locationRefDataService;

    public HearingDetailsPopulator(LocationReferenceDataService locationRefDataService) {
        this.locationRefDataService = locationRefDataService;
    }

    public JudgeFinalOrderForm.JudgeFinalOrderFormBuilder populateHearingDetails(JudgeFinalOrderFormBuilder builder, CaseData caseData,
                                                                                 LocationRefData caseManagementLocationDetails) {
        return builder.furtherHearingToggle(nonNull(caseData.getFinalOrderFurtherHearingToggle()))
            .furtherHearingToToggle(nonNull(getFurtherHearingDate(caseData, false)))
            .furtherHearingFromDate(getFurtherHearingDate(caseData, true))
            .furtherHearingToDate(getFurtherHearingDate(caseData, false))
            .furtherHearingLength(getFurtherHearingLength(caseData))
            .datesToAvoid(getDatesToAvoid(caseData))
            .showFurtherHearingLocationAlt(isDefaultCourt(caseData))
            .furtherHearingLocationDefault(LocationReferenceDataService.getDisplayEntry(caseManagementLocationDetails))
            .furtherHearingLocationAlt(getFurtherHearingLocationAlt(caseData))
            .furtherHearingMethod(getFurtherHearingMethod(caseData))
            .hearingNotes(getHearingNotes(caseData));
    }

    public LocalDate getFurtherHearingDate(CaseData caseData, boolean isFromDate) {
        if (caseData.getFinalOrderFurtherHearingToggle() != null
            && caseData.getFinalOrderFurtherHearingToggle().stream().anyMatch(finalOrderToggle -> finalOrderToggle.equals(
            FinalOrderToggle.SHOW)) && caseData.getFinalOrderFurtherHearingComplex() != null) {
            if (isFromDate) {
                return caseData.getFinalOrderFurtherHearingComplex().getListFromDate();
            } else {
                return caseData.getFinalOrderFurtherHearingComplex().getDateToDate();
            }
        }
        return null;
    }

    public String getFurtherHearingLength(CaseData caseData) {
        if (caseData.getFinalOrderFurtherHearingComplex() != null && caseData.getFinalOrderFurtherHearingComplex().getLengthList() != null) {
            switch (caseData.getFinalOrderFurtherHearingComplex().getLengthList()) {
                case MINUTES_15:
                    return "15 minutes";
                case MINUTES_30:
                    return "30 minutes";
                case HOUR_1:
                    return "1 hour";
                case HOUR_1_5:
                    return "1.5 hours";
                case HOUR_2:
                    return "2 hours";
                case OTHER:
                    return getOtherLength(caseData);
                default:
                    return "";
            }
        }
        return "";
    }

    private LocalDate getDatesToAvoid(CaseData caseData) {
        return nonNull(caseData.getFinalOrderFurtherHearingToggle())
            && nonNull(caseData.getFinalOrderFurtherHearingComplex().getDatesToAvoidDateDropdown())
            ? caseData.getFinalOrderFurtherHearingComplex().getDatesToAvoidDateDropdown().getDatesToAvoidDates() : null;
    }

    public Boolean isDefaultCourt(CaseData caseData) {
        if (caseData.getFinalOrderFurtherHearingToggle() != null
            && caseData.getFinalOrderFurtherHearingComplex() != null
            && caseData.getFinalOrderFurtherHearingComplex().getHearingLocationList() != null) {
            return caseData.getFinalOrderFurtherHearingComplex()
                .getHearingLocationList().getValue().getCode().equals("LOCATION_LIST");
        }
        return false;
    }

    private String getFurtherHearingLocationAlt(CaseData caseData) {
        return nonNull(caseData.getFinalOrderFurtherHearingToggle())
            && nonNull(caseData.getFinalOrderFurtherHearingComplex().getAlternativeHearingList())
            ? caseData.getFinalOrderFurtherHearingComplex().getAlternativeHearingList().getValue().getLabel() : null;
    }

    private String getFurtherHearingMethod(CaseData caseData) {
        return nonNull(caseData.getFinalOrderFurtherHearingComplex()) && nonNull(caseData.getFinalOrderFurtherHearingComplex().getHearingMethodList())
            ? caseData.getFinalOrderFurtherHearingComplex().getHearingMethodList().name() : "";
    }

    private String getHearingNotes(CaseData caseData) {
        return nonNull(caseData.getFinalOrderFurtherHearingToggle())
            && nonNull(caseData.getFinalOrderFurtherHearingComplex().getHearingNotesText())
            ? caseData.getFinalOrderFurtherHearingComplex().getHearingNotesText() : null;
    }

    private String getOtherLength(CaseData caseData) {
        StringBuilder otherLength = new StringBuilder();
        if (Objects.nonNull(caseData.getFinalOrderFurtherHearingComplex().getLengthListOther())) {
            String otherDay = caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherDays();
            String otherHour = caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherHours();
            String otherMinute = caseData.getFinalOrderFurtherHearingComplex().getLengthListOther().getLengthListOtherMinutes();
            otherLength.append(Objects.nonNull(otherDay) ? (otherDay + " days ") : "")
                .append(Objects.nonNull(otherHour) ? (otherHour + " hours ") : "")
                .append(Objects.nonNull(otherMinute) ? (otherMinute + " minutes") : "");
        }
        return otherLength.toString();
    }
}
