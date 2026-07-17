package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.NoRemissionDetailsSummary;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class HelpWithFeesDetails {

    @CCD(label = "Remission Amount", searchable = false, typeOverride = FieldType.MoneyGBP)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal remissionAmount;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.Text)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal outstandingFeeInPounds;
    @CCD(label = "Details", searchable = false)
    private String noRemissionDetails;
    @CCD(
            label = "Summary",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "HWFFeesSummaryType"
    )
    private NoRemissionDetailsSummary noRemissionDetailsSummary;
    @CCD(label = "HwF reference number", searchable = false)
    private String hwfReferenceNumber;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.Text)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private CaseEvent hwfCaseEvent;
}
