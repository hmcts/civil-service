package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class HelpWithFeesForTab {

    @CCD(label = "Remission amount:", searchable = false, typeOverride = FieldType.Text)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal remissionAmount;
    @CCD(label = "Applicant must pay:", searchable = false, typeOverride = FieldType.Text)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal applicantMustPay;
    @CCD(label = "Fee amount:", searchable = false, typeOverride = FieldType.Text)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal claimFee;
    @CCD(label = "Fee code:", searchable = false)
    private String feeCode;
    @CCD(label = "HWF reference number:", searchable = false)
    private String hwfReferenceNumber;
    @CCD(label = "Type:", searchable = false)
    private String hwfType;

}

