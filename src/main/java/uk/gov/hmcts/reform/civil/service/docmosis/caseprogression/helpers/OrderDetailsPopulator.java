package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm.JudgeFinalOrderFormBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Component
public class OrderDetailsPopulator {

    public JudgeFinalOrderForm.JudgeFinalOrderFormBuilder populateOrderDetails(JudgeFinalOrderFormBuilder builder, CaseData caseData) {
        return builder.freeFormRecordedText(caseData.getFreeFormRecordedTextArea())
            .freeFormOrderedText(caseData.getFreeFormOrderedTextArea())
            .orderOnCourtsList(caseData.getOrderOnCourtsList())
            .onInitiativeSelectionText(nonNull(caseData.getOrderOnCourtInitiative())
                                           ? caseData.getOrderOnCourtInitiative().getOnInitiativeSelectionTextArea() : null)
            .onInitiativeSelectionDate(nonNull(caseData.getOrderOnCourtInitiative())
                                           ? caseData.getOrderOnCourtInitiative().getOnInitiativeSelectionDate() : null)
            .withoutNoticeSelectionText(nonNull(caseData.getOrderWithoutNotice())
                                            ? caseData.getOrderWithoutNotice().getWithoutNoticeSelectionTextArea() : null)
            .withoutNoticeSelectionDate(nonNull(caseData.getOrderWithoutNotice())
                                            ? caseData.getOrderWithoutNotice().getWithoutNoticeSelectionDate() : null);
    }

    public JudgeFinalOrderFormBuilder populateAssistedOrderDetails(JudgeFinalOrderFormBuilder builder, CaseData caseData) {
        return builder.finalOrderMadeSelection(caseData.getFinalOrderMadeSelection())
            .orderMadeDate(orderMadeDateBuilder(caseData))
            .recordedToggle(nonNull(caseData.getFinalOrderRecitals()))
            .recordedText(nonNull(caseData.getFinalOrderRecitalsRecorded())
                              ? caseData.getFinalOrderRecitalsRecorded().getText() : "")
            .orderedText(caseData.getFinalOrderOrderedThatText())
            .finalOrderJudgeHeardFrom(nonNull(caseData.getFinalOrderJudgeHeardFrom()));
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
