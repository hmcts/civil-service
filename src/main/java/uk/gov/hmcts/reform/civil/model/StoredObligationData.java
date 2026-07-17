package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.ObligationReason;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class StoredObligationData {

    @CCD(label = "Created by", searchable = false)
    private String createdBy;
    @CCD(label = "Created on", searchable = false)
    private LocalDateTime createdOn;
    @CCD(label = "Review date")
    private LocalDate obligationDate;
    @CCD(label = "Reason", showCondition = "reasonText = \" \"", searchable = false)
    private ObligationReason obligationReason;
    @CCD(label = "Info for other", showCondition = "reasonText = \" \"", searchable = false)
    private String otherObligationReason;
    @CCD(label = "Review type", searchable = false)
    private String reasonText;
    @CCD(label = "Description of review", searchable = false, typeOverride = FieldType.TextArea)
    private String obligationAction;
    @CCD(
            label = " ",
            showCondition = "obligationWATaskRaised = \"DO_NOT_SHOW_IN_UI\"",
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo obligationWATaskRaised;
}
