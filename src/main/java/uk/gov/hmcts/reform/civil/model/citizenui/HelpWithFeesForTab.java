package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class HelpWithFeesForTab {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal remissionAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal applicantMustPay;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal claimFee;
    private String feeCode;
    private String hwfReferenceNumber;
    private String hwfType;

}

