package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.NoRemissionDetailsSummary;
import uk.gov.hmcts.reform.civil.model.Fee;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
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
}
