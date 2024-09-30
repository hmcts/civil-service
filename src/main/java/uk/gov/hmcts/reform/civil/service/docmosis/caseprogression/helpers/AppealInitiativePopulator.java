package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm.JudgeFinalOrderFormBuilder;

import java.time.LocalDate;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.finalorders.AppealList.OTHER;
import static uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList.CIRCUIT_COURT;
import static uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList.GRANTED;
import static uk.gov.hmcts.reform.civil.enums.finalorders.ApplicationAppealList.REFUSED;

@Component
public class AppealInitiativePopulator {

    public JudgeFinalOrderForm.JudgeFinalOrderFormBuilder populateAppealDetails(JudgeFinalOrderFormBuilder builder, CaseData caseData) {
        return builder.claimantOrDefendantAppeal(getAppealFor(caseData))
            .appealGranted(isAppealGranted(caseData))
            .tableAorB(circuitOrHighCourt(caseData))
            .appealDate(getAppealDate(caseData));
    }

    public JudgeFinalOrderFormBuilder populateInitiativeOrWithoutNoticeDetails(JudgeFinalOrderFormBuilder builder, CaseData caseData) {
        return builder.showInitiativeOrWithoutNotice(getInitiativeOrWithoutNotice(caseData))
            .initiativeDate(getInitiativeDate(caseData))
            .withoutNoticeDate(getWithoutNoticeDate(caseData))
            .reasonsText(getReasonsText(caseData));
    }

    public String getAppealFor(CaseData caseData) {
        if (caseData.getFinalOrderAppealComplex() != null && caseData.getFinalOrderAppealComplex().getList() != null) {
            if (caseData.getFinalOrderAppealComplex().getList().name().equals(OTHER.name())) {
                return caseData.getFinalOrderAppealComplex().getOtherText();
            } else {
                return caseData.getFinalOrderAppealComplex().getList().name().toLowerCase() + "'s";
            }
        }
        return "";
    }

    public String isAppealGranted(CaseData caseData) {
        return nonNull(caseData.getFinalOrderAppealComplex())
            && caseData.getFinalOrderAppealComplex().getApplicationList().name().equals(ApplicationAppealList.GRANTED.name())
            ? "true" : null;
    }

    public String circuitOrHighCourt(CaseData caseData) {
        if (caseData.getFinalOrderAppealComplex() != null
            && caseData.getFinalOrderAppealComplex().getApplicationList() == GRANTED
            && caseData.getFinalOrderAppealComplex().getAppealGrantedDropdown().getCircuitOrHighCourtList().equals(CIRCUIT_COURT)) {
            return "a";
        }
        if (caseData.getFinalOrderAppealComplex() != null
            && caseData.getFinalOrderAppealComplex().getApplicationList() == REFUSED
            && caseData.getFinalOrderAppealComplex().getAppealRefusedDropdown().getCircuitOrHighCourtListRefuse().equals(CIRCUIT_COURT)) {
            return "a";
        } else {
            return "b";
        }
    }

    public LocalDate getAppealDate(CaseData caseData) {
        if (caseData.getFinalOrderAppealComplex() != null
            && caseData.getFinalOrderAppealComplex().getApplicationList() == GRANTED) {
            if (caseData.getFinalOrderAppealComplex().getAppealGrantedDropdown().getCircuitOrHighCourtList().equals(
                CIRCUIT_COURT)) {
                return caseData.getFinalOrderAppealComplex().getAppealGrantedDropdown().getAppealChoiceSecondDropdownA().getAppealGrantedRefusedDate();
            } else {
                return caseData.getFinalOrderAppealComplex().getAppealGrantedDropdown().getAppealChoiceSecondDropdownB().getAppealGrantedRefusedDate();
            }
        }
        if (caseData.getFinalOrderAppealComplex() != null
            && caseData.getFinalOrderAppealComplex().getApplicationList() == REFUSED) {
            if (caseData.getFinalOrderAppealComplex().getAppealRefusedDropdown().getCircuitOrHighCourtListRefuse().equals(CIRCUIT_COURT)) {
                return caseData.getFinalOrderAppealComplex().getAppealRefusedDropdown().getAppealChoiceSecondDropdownA().getAppealGrantedRefusedDate();
            } else {
                return caseData.getFinalOrderAppealComplex().getAppealRefusedDropdown().getAppealChoiceSecondDropdownB().getAppealGrantedRefusedDate();
            }
        }
        return null;
    }

    public String getInitiativeOrWithoutNotice(CaseData caseData) {
        if (caseData.getOrderMadeOnDetailsList().equals(OrderMadeOnTypes.COURTS_INITIATIVE)) {
            return caseData.getOrderMadeOnDetailsOrderCourt().getOwnInitiativeText();
        }
        if (caseData.getOrderMadeOnDetailsList().equals(OrderMadeOnTypes.WITHOUT_NOTICE)) {
            return caseData.getOrderMadeOnDetailsOrderWithoutNotice().getWithOutNoticeText();
        }
        return null;
    }

    private LocalDate getInitiativeDate(CaseData caseData) {
        return caseData.getOrderMadeOnDetailsList().equals(OrderMadeOnTypes.COURTS_INITIATIVE)
            ? caseData.getOrderMadeOnDetailsOrderCourt().getOwnInitiativeDate() : null;
    }

    private LocalDate getWithoutNoticeDate(CaseData caseData) {
        return caseData.getOrderMadeOnDetailsList().equals(OrderMadeOnTypes.WITHOUT_NOTICE)
            ? caseData.getOrderMadeOnDetailsOrderWithoutNotice().getWithOutNoticeDate() : null;
    }

    private String getReasonsText(CaseData caseData) {
        return nonNull(caseData.getFinalOrderGiveReasonsComplex())
            ? caseData.getFinalOrderGiveReasonsComplex().getReasonsText() : null;
    }

}
