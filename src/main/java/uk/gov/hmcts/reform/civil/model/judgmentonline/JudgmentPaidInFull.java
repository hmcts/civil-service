package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.model.ConfirmJudgmentPaidInFull;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "MarkJudgmentPaid", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgmentPaidInFull {

    @CCD(label = "Enter the date the judgment was paid in full", hint = "For example, 16 04 2021", searchable = false)
    private LocalDate dateOfFullPaymentMade;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ConfirmJudgmentPaidInFull",
            typeParameterClass = ConfirmJudgmentPaidInFull.class
    )
    private List<String> confirmFullPaymentMade;
}
