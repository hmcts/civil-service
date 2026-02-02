package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Component
public class OrderDetailsPopulator {

    public JudgeFinalOrderForm populateOrderDetails(JudgeFinalOrderForm form, CaseData caseData) {
        return form.setFreeFormRecordedText(caseData.getFreeFormRecordedTextArea())
            .setFreeFormOrderedText(caseData.getFreeFormOrderedTextArea())
            .setOrderOnCourtsList(caseData.getOrderOnCourtsList())
            .setOnInitiativeSelectionText(nonNull(caseData.getOrderOnCourtInitiative())
                                           ? caseData.getOrderOnCourtInitiative().getOnInitiativeSelectionTextArea() : null)
            .setOnInitiativeSelectionDate(nonNull(caseData.getOrderOnCourtInitiative())
                                           ? caseData.getOrderOnCourtInitiative().getOnInitiativeSelectionDate() : null)
            .setWithoutNoticeSelectionText(nonNull(caseData.getOrderWithoutNotice())
                                            ? caseData.getOrderWithoutNotice().getWithoutNoticeSelectionTextArea() : null)
            .setWithoutNoticeSelectionDate(nonNull(caseData.getOrderWithoutNotice())
                                            ? caseData.getOrderWithoutNotice().getWithoutNoticeSelectionDate() : null);
    }

    public JudgeFinalOrderForm populateAssistedOrderDetails(JudgeFinalOrderForm form, CaseData caseData) {
        return form.setFinalOrderMadeSelection(caseData.getFinalOrderMadeSelection())
            .setOrderMadeDate(orderMadeDateBuilder(caseData))
            .setRecordedToggle(nonNull(caseData.getFinalOrderRecitals()))
            .setRecordedText(nonNull(caseData.getFinalOrderRecitalsRecorded())
                              ? caseData.getFinalOrderRecitalsRecorded().getText() : "")
            .setOrderedText(caseData.getFinalOrderOrderedThatText())
            .setFinalOrderJudgeHeardFrom(nonNull(caseData.getFinalOrderJudgeHeardFrom()));
    }

    public String orderMadeDateBuilder(CaseData caseData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
        if (caseData.getFinalOrderDateHeardComplex() != null) {
            if (caseData.getFinalOrderDateHeardComplex().getSingleDateSelection() != null) {
                LocalDate date1 = caseData.getFinalOrderDateHeardComplex().getSingleDateSelection().getSingleDate();
                return format("on %s", date1.format(formatter));

            }
            if (caseData.getFinalOrderDateHeardComplex().getDateRangeSelection() != null) {
                LocalDate date1 = caseData.getFinalOrderDateHeardComplex().getDateRangeSelection().getDateRangeFrom();
                LocalDate date2 = caseData.getFinalOrderDateHeardComplex().getDateRangeSelection().getDateRangeTo();
                return format("between %s and %s", date1.format(formatter), date2.format(formatter));
            }
            if (caseData.getFinalOrderDateHeardComplex().getBespokeRangeSelection() != null) {
                return format(
                    "on %s",
                    caseData.getFinalOrderDateHeardComplex().getBespokeRangeSelection().getBespokeRangeTextArea()
                );
            }
        }
        return null;
    }

}
