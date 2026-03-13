package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.NoRemissionDetailsSummary;
import uk.gov.hmcts.reform.civil.model.Fee;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class HelpWithFeesDetails {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal remissionAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal outstandingFee;
    private String noRemissionDetails;
    private NoRemissionDetailsSummary noRemissionDetailsSummary;
    private String hwfReferenceNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private CaseEvent hwfCaseEvent;
    private Fee fee;
    private FeeType hwfFeeType;

    public HelpWithFeesDetails copy() {
        return new HelpWithFeesDetails()
            .setRemissionAmount(remissionAmount)
            .setOutstandingFee(outstandingFee)
            .setNoRemissionDetails(noRemissionDetails)
            .setNoRemissionDetailsSummary(noRemissionDetailsSummary)
            .setHwfReferenceNumber(hwfReferenceNumber)
            .setHwfCaseEvent(hwfCaseEvent)
            .setFee(fee)
            .setHwfFeeType(hwfFeeType);
    }
}
