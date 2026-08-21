package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.ObligationReason;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ObligationDataCollection", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObligationData {

    @CCD(label = "Review date")
    private LocalDate obligationDate;
    @CCD(label = "Reason")
    private ObligationReason obligationReason;
    @CCD(label = "Info for other", showCondition = "obligationReason = \"OTHER\"")
    private String otherObligationReason;
    @CCD(label = "Description of review", typeOverride = FieldType.TextArea)
    private String obligationAction;
    @CCD(
            label = " ",
            showCondition = "obligationWATaskRaised = \"DO_NOT_SHOW_IN_UI\"",
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo obligationWATaskRaised;
}
