package uk.gov.hmcts.reform.civil.model.transferonlinecase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TocTransferCaseReason {

    @CCD(
            label = "Give details <br> State the court location name where the case will be transferred and the "
                    + "reason as to why the case needs to be transferred to another hearing court centre (i.e. "
                    + "Transfer the case to Leicester County Court. This case is not on our court jurisdiction to "
                    + "draw an order)",
            searchable = false,
            typeOverride = FieldType.Label
    )
    private String txtTransferCaseReason;

    private String reasonForCaseTransferJudgeTxt;
}
